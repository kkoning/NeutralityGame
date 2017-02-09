package neutrality;

import agency.Individual;
import agency.SimpleFirm;
import neutrality.Offers.ContentOffer;

public abstract class ContentProvider<T extends Individual> extends SimpleFirm<T> {

/**
 * True if this content provider is in the video market, false if it in the
 * other content market.
 */
public Boolean isVideoProvider;
// Parameters relevant to consumer value; investment and preference.
Double contentInvestment = 0.0;
Double preference        = 0.0;
// Track the # of units sold, revenue, and $ for interconnection
double numAcceptedOffers;
double totalRevenue;
double totalPaidForInterconnection;

boolean bankrupt = false;

// TODO: Constructor
public ContentProvider() {
  super();
}


double getOperatingProfit() {
  return totalRevenue - totalPaidForInterconnection;
}

boolean onTrack() {
  NeutralityModel nm = (NeutralityModel) getModel();
  // The first step is step 0, requiring the addition here for a ratio.
  double perStepProfit = getOperatingProfit() / (nm.currentStep + 1);
  double revenueProjection = perStepProfit * nm.maxSteps;
  double totalInvestment = contentInvestment;
  double requiredTarget = totalInvestment * nm.requiredReturnOnCapital;
  if (revenueProjection < requiredTarget)
    return false;
  else
    return true;
}


public void step() {
  NeutralityModel nm = (NeutralityModel) getModel();
  if (nm.currentStep > 2) {
    bankrupt = !onTrack();
  }
}


@Override
public String toString() {
  return super.toString() + "[contentInvestment=" + contentInvestment + ", preference="
          + preference + ", isVideoProvider=" + isVideoProvider + ", numAcceptedOffers="
          + numAcceptedOffers + ", totalRevenue=" + totalRevenue
          + ", totalPaidForInterconnection=" + totalPaidForInterconnection + ", account="
          + account + "]";
}

public double getInvestment() {
  return contentInvestment;
}

public double getPreference() {
  return preference;
}

public abstract ContentOffer getContentOffer();

public void makeContentInvestment(double amount) {
  NeutralityModel model = (NeutralityModel) getModel();
  this.contentInvestment += amount;
  this.account.pay(amount * model.capitalCost);
}

/**
 * Is called whenever a content provider's offer is accepted by a consumer.
 *
 * @param acceptedOffer
 */
public void processAcceptedContentOffer(
        double qty,
        ContentOffer acceptedOffer,
        NetworkOperator<?> onNetwork) {
  // Earn $
  account.receive(acceptedOffer.contentPrice * qty);

  // Track total revenue
  totalRevenue += acceptedOffer.contentPrice * qty;

  // Track total # of offers accepted.
  numAcceptedOffers += qty;

  // Pay network provider for bandwidth use.
  payInterconnectionBandwidth(qty, onNetwork);
}

/**
 * Pay interconnection bandwidth to the consumer's network operator. This is
 * left as an abstract function
 *
 * @param toNetwork
 *         the consumer's network operator.
 */
void payInterconnectionBandwidth(double qty,
                                 NetworkOperator<?> toNetwork) {

  NeutralityModel nm = (NeutralityModel) this.getModel();
  if (nm.forceZeroPriceIC)
    return;

  // How much to pay depends on BW usage of sector apps.
  double bwIntensity;
  if (isVideoProvider)
    bwIntensity = ((NeutralityModel) getModel()).videoBWIntensity();
  else
    bwIntensity = ((NeutralityModel) getModel()).otherBWIntensity();

  double paymentAmount =
          qty * bwIntensity * toNetwork.getInterconnectionBandwidthPrice();

  // Pay and track.
  // payment amount already includes quantity.
  account.pay(paymentAmount); // from us
  toNetwork.receiveInterconnectionPayment(this, paymentAmount); // to nsp
  totalPaidForInterconnection += paymentAmount;

}


}
