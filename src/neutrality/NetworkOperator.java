package neutrality;

import agency.Individual;
import agency.NullIndividual;
import agency.SimpleFirm;
import agency.SimpleFitness;
import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

public abstract class NetworkOperator<T extends Individual>
        extends SimpleFirm<T> {

double networkInvestment;

// Track the # of different purchases of each type
int qty_NetworkOnly;
int qty_Bundled;
int qty_BundledZeroRated;

// Track the total income from different purchases of each type
double rev_NetworkOnly;
double rev_Bundled;
double rev_BundledZeroRated;

// Track the income from consumer bandwidth
double rev_BandwidthVideo;
double rev_BandwidthOther;

// Track the income from interconnection
double ixc_RevenueFromVideo;
double ixc_RevenueFromOther;

/**
 * The vertically integrated video Content Provider, if any.
 */
ContentProvider<NullIndividual> icp;

boolean bankrupt = false;

boolean onTrack() {
  NeutralityModel nm = (NeutralityModel) getModel();
  // The first step is step 0, requiring the addition here for a ratio.
  double perStepRevenue = totalRevenueAllSources()  / (nm.currentStep+1);
  double revenueProjection = perStepRevenue * nm.maxSteps;
  double totalInvestment = networkInvestment;
  if (icp != null) {
    totalInvestment += icp.contentInvestment;
  }
  double requiredTarget = totalInvestment * nm.requiredReturnOnCapital;

  if (revenueProjection < requiredTarget)
    return false;
  else
    return true;
}

double totalRevenueAllSources() {
  double toReturn = 0.0d;
  // Connection Sales
  toReturn += rev_NetworkOnly + rev_Bundled + rev_BundledZeroRated;
  // Bandwidth Sales
  toReturn += rev_BandwidthVideo + rev_BandwidthOther;
  // Content Sales
  if (icp != null)
    toReturn += icp.totalRevenue;
  return toReturn;
}

void step() {
  NeutralityModel nm = (NeutralityModel) getModel();
  if (nm.currentStep > 2) {
    bankrupt = !onTrack();
  }
}

double getInvestment() {
  return networkInvestment;
}

void makeNetworkInvestment(double amount) {
  NeutralityModel model = (NeutralityModel) getModel();
  this.networkInvestment += amount;
  double cost = Math.pow(amount * model.capitalCost,
                         model.networkCapitalCostExponent);
  this.account.pay(cost);
}

void makeContentInvestment(double amount) {
  NeutralityModel model = (NeutralityModel) getModel();
  this.icp.contentInvestment += amount;
  double cost = Math.pow(amount * model.capitalCost,
                         model.contentCapitalCostExponent);
  this.icp.account.pay(cost);
}

public abstract NetworkOffer getNetworkOffer();

public abstract ContentOffer getVideoContentOffer();

public abstract BundledOffer getBundledOffer();

public abstract BundledOffer getBundledZeroRatedOffer();

public void receiveInterconnectionPayment(
        ContentProvider<?> cp,
        double amount) {
  // Credit the payment to our account
  account.receive(amount);

  // Track the sources of ixc payments.
  if (cp.isVideoProvider)
    ixc_RevenueFromVideo += amount;
  else
    ixc_RevenueFromOther += amount;
}

/**
 * Called when a consumer accepts an unbundled network offer. Credits the
 * payment to the Network Operator's account, both for the connection and
 * bandwidth usage.
 *
 * @param networkOffer
 *         the network offer that the consumer accepted.
 * @param videoUsed
 *         true if the consumer also purchased video content.
 * @param otherUsed
 *         true if the consumer also purchased other content.
 */
public void processAcceptedNetworkOffer(
        NetworkOffer networkOffer,
        boolean videoUsed,
        boolean otherUsed) {

  // Track # of accepted standalone offers
  qty_NetworkOnly++;

  // Track total revenue received from connection fees
  account.receive(networkOffer.connectionPrice);
  rev_NetworkOnly += networkOffer.connectionPrice;

  // Track consumer bandwidth usage.
  consumerBandwidthUsage(networkOffer.bandwidthPrice, videoUsed, otherUsed);

}

/**
 * Track income from _consumer_ bandwidth usage. Note that this is in
 * addition to the interconnection bandwidth charged to content providers.
 *
 * @param bandwidthPrice
 *         the price of bandwidth usage
 * @param chargeVideoBandwidth
 *         whether to charge for the use of video content
 * @param chargeOtherBandwidth
 *         whether to charge for the use of other content
 */
private void consumerBandwidthUsage(
        double bandwidthPrice,
        boolean chargeVideoBandwidth,
        boolean chargeOtherBandwidth) {

  double amount = 0;
  if (chargeVideoBandwidth) {
    amount = bandwidthPrice * ((NeutralityModel) getModel()).videoBWIntensity();
    rev_BandwidthVideo += amount;
    account.receive(amount);
  }
  if (chargeOtherBandwidth) {
    amount = bandwidthPrice * ((NeutralityModel) getModel()).otherBWIntensity();
    rev_BandwidthOther += amount;
    account.receive(amount);
  }
}

/**
 * Called when a consumer accepts a bundled offer of both network access and
 * video content. Credits the payment to the network Operator's account. If
 * the offer was for zero rated content, then the consumer will not be
 * charged for video bandwidth. Otherwise, it will (video is always included
 * in bundled offers). If other content was also used, the consumer will be
 * charged for that as well.
 *
 * @param bundledOffer
 *         the bundled offer that the consumer accepted
 * @param otherUsed
 *         true if the consumer also purchased other content.
 */
public void processAcceptedBundledOffer(
        BundledOffer bundledOffer,
        boolean otherUsed) {

  // Earn the $
  account.receive(bundledOffer.bundlePrice);

  // Track # of accepted bundled offers accepted
  if (bundledOffer.contentZeroRated) {
    qty_BundledZeroRated++;
    rev_BundledZeroRated += bundledOffer.bundlePrice;
  } else {
    qty_Bundled++;
    rev_Bundled += bundledOffer.bundlePrice;
  }

  // Track consumer bandwidth usage.
  consumerBandwidthUsage(
          bundledOffer.bandwidthPrice,
          !bundledOffer.contentZeroRated, // if zero rated, don't charge
          otherUsed);

}

@Override
public SimpleFitness getFitness() {
    /*
     * In this case, the total fitness of the agent is equal to the sum of
		 * the fitness for the network operator and content provider aspects of
		 * the agent's business.
		 */
  double fitness = account.getBalance();
  if (icp != null)
    fitness += icp.account.getBalance();

  return new SimpleFitness(fitness);
}

public abstract double getInterconnectionBandwidthPrice();

@Override
public String toString() {
  return "NetworkOperator{" +
         "networkInvestment=" + networkInvestment +
         ", qty_NetworkOnly=" + qty_NetworkOnly +
         ", qty_Bundled=" + qty_Bundled +
         ", qty_BundledZeroRated=" + qty_BundledZeroRated +
         ", rev_NetworkOnly=" + rev_NetworkOnly +
         ", rev_Bundled=" + rev_Bundled +
         ", rev_BundledZeroRated=" + rev_BundledZeroRated +
         ", rev_BandwidthVideo=" + rev_BandwidthVideo +
         ", rev_BandwidthOther=" + rev_BandwidthOther +
         ", ixc_RevenueFromVideo=" + ixc_RevenueFromVideo +
         ", ixc_RevenueFromOther=" + ixc_RevenueFromOther +
         ", icp=" + icp +
         ", bankrupt=" + bankrupt +
         '}';
}

public int getNumStandaloneContentOffersAccepted() {
  if (icp == null)
    return 0;
  else
    return icp.numAcceptedOffers;
}

public double getTotalStandaloneContentRevenue() {
  if (icp == null)
    return 0;
  else
    return icp.totalRevenue;
}

}
