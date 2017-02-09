package neutrality;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import agency.Agent;
import agency.AgentModel;
import agency.Fitness;
import agency.Individual;
import agency.SimpleFirm;
import agency.SimpleFitness;
import agency.data.AgencyData;
import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

import static agency.util.Statistics.HHI;

public class NeutralityModel
        implements AgentModel {

public Double alpha;
public Double beta;

public Double psi;
public Double tau;
public Double capitalCost;
public Double theta;
public Double gamma;
public Boolean analyticalConsumers;
public Double analyticalIncome;

public Double networkCapitalCostExponent;
public Double contentCapitalCostExponent;
public Double requiredReturnOnCapital;

public Integer numConsumers;
public Double  topIncome;

public Boolean forceZeroPriceIC;
public Boolean bundlingAllowed;
public Boolean zeroRatingAllowed;

public Boolean integratedContentAllowed;
public Boolean bankruptcyEnforced;
public Boolean punishIfAllBankrupt;

public Integer maxSteps;

List<NetworkOperator<?>> networkOperators;
List<ContentProvider<?>> videoContentProviders;
List<ContentProvider<?>> otherContentProviders;
Consumers                consumers;

boolean     debug     = false;
PrintStream debugOut  = null;

int currentStep = 0;

public NeutralityModel() {
  networkOperators = new ArrayList<>();
  videoContentProviders = new ArrayList<>();
  otherContentProviders = new ArrayList<>();
}

@Override
public void addAgent(Agent<? extends Individual> agent) {
  // Classify and track each agent into proper role within model
  if (agent instanceof NetworkOperator)
    networkOperators.add((NetworkOperator<?>) agent);
  else if (agent instanceof ContentProvider) {
    ContentProvider<?> cp = (ContentProvider<?>) agent;
    if (cp.isVideoProvider)
      videoContentProviders.add(cp);
    else
      otherContentProviders.add(cp);
  } else
    // We don't know what to do with this agent type
    throw new RuntimeException("Unsupported agent type: " + agent);
}

@Override
public Fitness getFitness(Agent<? extends Individual> agent) {
  // They should all be instances of SimpleFirm
  SimpleFirm<?> firm = (SimpleFirm<?>) agent;
  SimpleFitness fitness = firm.getFitness();
  return fitness;
}

@Override
public void init() {
  // Print notice, if debug enabled
  if (debug)
    debugOut.println("Executing first step of model");

  // Initialize consumers, print information for debug
  if (debug)
    debugOut.println("Initializing Consumers");

  if (analyticalConsumers) {
    consumers = new AnalyticalConsumers(analyticalIncome,gamma,this);
  } else {
    consumers = new Consumers(numConsumers, topIncome, this);
  }

  if (debug) {
    debugOut.println("Consumer properties follow:");
    debugOut.println(consumers.printConsumerProperties());
  }

  // Space preferences of provider agents
  if (debug)
    debugOut.println("Spacing preferences of provider agents");
  spacePreferences();

  // If debug, print some information about
  if (debug) {
    debugOut.println("Content Provider Properties Follow:");
    List<ContentProvider<?>> vidCPs = new ArrayList<>();
    vidCPs.addAll(videoContentProviders);
    for (NetworkOperator<?> netOp : networkOperators) {
      ContentProvider<?> netOpCP = netOp.icp;
      vidCPs.add(netOpCP);
    }
    for (ContentProvider<?> cp : vidCPs) {
      debugOut.println(cp);
    }
    for (ContentProvider<?> cp : otherContentProviders) {
      debugOut.println(cp);
    }
  }

  if (debug)
    debugOut.println("First step initialization completed.");
}

@Override
public boolean step() {

  if (debug)
    debugOut.println("Stepping Network Operators");
  // Step each agent; allow them to generate and update offers.
  for (NetworkOperator<?> no : networkOperators) {
    no.step(); // Network Operators
  }
  if (debug)
    debugOut.println("Stepping Independent Video Content Providers");
  for (ContentProvider<?> cp : videoContentProviders) {
    cp.step(); // Video Content Providers
  }
  if (debug)
    debugOut.println("Stepping Independent Other Content Providers");
  for (ContentProvider<?> cp : otherContentProviders) {
    cp.step(); // Other Content Providers
  }
  // Consumers do not need to be stepped; behavior is specified.

  if (debug)
    debugOut.println("Collecting offers from networks and providers");

  // Agents generate offers, add them to these lists.
  List<NetworkOffer> networkOnlyOffers = new ArrayList<>();
  List<ContentOffer> videoContentOffers = new ArrayList<>();
  List<ContentOffer> otherContentOffers = new ArrayList<>();
  List<BundledOffer> bundledOffers = new ArrayList<>();

  // Network Operators
  for (NetworkOperator<?> no : networkOperators) {
    // Ignore bankrupt network operators.
    if (bankruptcyEnforced)
      if (no.bankrupt)
        continue;

    if (no.getNetworkOffer() != null)
      networkOnlyOffers.add(no.getNetworkOffer());

    if (integratedContentAllowed) {
      if (no.getVideoContentOffer() != null)
        videoContentOffers.add(no.getVideoContentOffer());

      if (bundlingAllowed) {
        if (no.getBundledOffer() != null) {
          bundledOffers.add(no.getBundledOffer());
        }

        // zero rating only works with bundling?
        if (zeroRatingAllowed) {
          BundledOffer zrbo = no.getBundledZeroRatedOffer();
          if (zrbo != null)
            bundledOffers.add(zrbo);
        }
      }
    }

  }

  // Video Content Providers
  for (ContentProvider<?> cp : videoContentProviders) {
    // Ignore bankrupt content providers
    if (bankruptcyEnforced)
      if (cp.bankrupt)
        continue;

    if (cp.getContentOffer() != null)
      videoContentOffers.add(cp.getContentOffer());
  }

  // Other Content Providers
  for (ContentProvider<?> cp : otherContentProviders) {
    // Ignore bankrupt content providers
    if (bankruptcyEnforced)
      if (cp.bankrupt)
        continue;

    if (cp.getContentOffer() != null)
      otherContentOffers.add(cp.getContentOffer());
  }

  /*
   * Print details of offers made by the individual agents
   */
  if (debug) {
    debugOut.println("Details of offers follows:");

    debugOut.println("Network Only Offers:");
    for (NetworkOffer no : networkOnlyOffers) {
      debugOut.println(no);
    }

    debugOut.println("Unbundled Video Content Offers:");
    for (ContentOffer co : videoContentOffers) {
      debugOut.println(co);
    }

    debugOut.println("Other Content Offers:");
    for (ContentOffer co : otherContentOffers) {
      debugOut.println(co);
    }

    debugOut.println("Bundled Network and Video Offers:");
    for (BundledOffer bo : bundledOffers) {
      debugOut.println(bo);
    }

  }




	/*
   * Now that we have a list of all the offers made by network operators
   * and content providers, we need to generate a list of possible
	 * consumption options for consumers to consider.
	 */
  if (debug)
    debugOut.println("Calculating possible consumption options of consumers");
  List<ConsumptionOption> options = Consumers.determineOptions(
          this,
          networkOnlyOffers,
          videoContentOffers,
          otherContentOffers,
          bundledOffers);
  // Debug for completed offers
  if (debug) {
    debugOut.println("Details of offers follows:");
    for (ConsumptionOption co : options) {
      debugOut.println(co);
    }
  }

  /*
   * Consumers consider and consume offers
   */

  // Consumers consider and consume offers.
  // Details are specified in Consumers.procurementProcess
  consumers.procurementProcess(options);


  // Finally, increment the step
  currentStep++;

  // Don't terminate early.
  return false;
}

@Override
public void finish() {

  if (!punishIfAllBankrupt)
    return;

  /*
  Initial bankruptcy procedure:  If all of the network providers go bankrupt,
  all of the content providers also go bankrupt, with fitness equal to their
  total investment * -1.  If all of the content providers go bankrupt, then
  all of the network providers go bankrupt too, with their fitness also equal
  to their total investment * -1.
   */

  // Detect if any agents have negative fitness.
  boolean allNSPsBankrupt = true;
  boolean allVCPsBankrupt = true;
  boolean allOCPsBankrupt = true;
  for (NetworkOperator networkOperator : networkOperators) {
    if (networkOperator.account.getBalance() >= 0) {
      allNSPsBankrupt = false;
      if (networkOperator.icp != null)
        allVCPsBankrupt = false;
    }
  }
  for (ContentProvider vcp : videoContentProviders) {
    if (vcp.account.getBalance() >= 0)
      allVCPsBankrupt = false;
  }
  for (ContentProvider ocp : otherContentProviders) {
    if (ocp.account.getBalance() >= 0)
      allOCPsBankrupt = false;
  }

  if (allNSPsBankrupt) {
    for (ContentProvider vcp : videoContentProviders) {
      double totalInvested = 0.0;
      totalInvested += vcp.contentInvestment;
      vcp.account.forceBalance(-totalInvested);
    }
    for (ContentProvider ocp : otherContentProviders) {
      double totalInvested = 0.0;
      totalInvested += ocp.contentInvestment;
      ocp.account.forceBalance(-totalInvested);
    }
  }

  if (allVCPsBankrupt | allOCPsBankrupt) {
    for (NetworkOperator no : networkOperators) {
      double totalInvested = 0.0;
      totalInvested += no.networkInvestment;
      if (no.icp != null)
        totalInvested += no.icp.contentInvestment;
      no.account.forceBalance(-totalInvested);
    }
  }


}

@Override
public int getMaxSteps() {
  return maxSteps;
}

/*
 * TODO: In progress...
 *
 * (non-Javadoc)
 *
 * @see agency.AgentModel#getSummaryData()
 */
@Override
public Object getSummaryData() {
  OutputData o = new OutputData();

  /*
  Consumption Data
   */
  // Standalone Offers
  o.qty_NetworkOnly =
          networkOperators.stream()
                          .mapToDouble(no -> no.qty_NetworkOnly)
                          .sum();
  o.qty_VideoOnlyNSP =
          networkOperators
                  .stream()
                  .mapToDouble(no -> no.getNumStandaloneContentOffersAccepted())
                  .sum();
  o.qty_VideoOnlyCP =
          videoContentProviders.stream()
                               .mapToDouble(vcp -> vcp.numAcceptedOffers)
                               .sum();
  o.qty_OtherContent =
          otherContentProviders.stream()
                               .mapToDouble(ocp -> ocp.numAcceptedOffers)
                               .sum();
  // Bundled offers
  o.qty_Bundled = networkOperators.stream()
                                  .mapToDouble(no -> no.qty_Bundled)
                                  .sum();
  o.qty_BundledZeroRated =
          networkOperators.stream()
                          .mapToDouble(no -> no.qty_BundledZeroRated)
                          .sum();

  // Other
  o.consumerSurplus = consumers.getTotalSurplus();




  /*
  Network Operator Variables
   */
  // Investment
  o.nsp_InvestmentNetwork =
          networkOperators.stream()
                          .mapToDouble(no -> no.networkInvestment)
                          .sum();
  o.nsp_InvestmentVideo =
          networkOperators.stream()
                          .mapToDouble(no -> no.icp
                                  .contentInvestment)
                          .sum();
  // Revenue
  o.nsp_RevenueNetworkOnly =
          networkOperators.stream()
                          .mapToDouble(no -> no.rev_NetworkOnly)
                          .sum();
  o.nsp_RevenueBundled =
          networkOperators.stream()
                          .mapToDouble(no -> no.rev_Bundled)
                          .sum();
  o.nsp_RevenueBundledZeroRated =
          networkOperators.stream()
                          .mapToDouble(no -> no.rev_BundledZeroRated)
                          .sum();
  o.nsp_RevenueVideoOnly =
          networkOperators.stream()
                          .mapToDouble(no -> no.icp.totalRevenue)
                          .sum();

  // Other
  o.nsp_Surplus =
          networkOperators.stream()
                          .mapToDouble(no -> no.account.getBalance())
                          .sum();

  /*
  Content Provider Variables
   */
  // Investment
  o.cp_InvestmentVideo =
          videoContentProviders.stream()
                               .mapToDouble(cp -> cp.getInvestment())
                               .sum();
  o.cp_InvestmentOther =
          otherContentProviders.stream()
                               .mapToDouble(cp -> cp.getInvestment())
                               .sum();
  // Revenue
  o.cp_RevenueVideo =
          videoContentProviders.stream()
                               .mapToDouble(vcp -> vcp.totalRevenue)
                               .sum();
  o.cp_RevenueOther =
          otherContentProviders.stream()
                               .mapToDouble(ocp -> ocp.totalRevenue)
                               .sum();
  // Other/Surplus
  o.cp_SurplusVideo =
          videoContentProviders.stream()
                               .mapToDouble(cp -> cp.account.getBalance())
                               .sum();
  o.cp_SurplusOther =
          otherContentProviders.stream()
                               .mapToDouble(cp -> cp.account.getBalance())
                               .sum();


  /*
  Market Variables
   */
  // Interconnection
  o.ixc_VideoFees =
          networkOperators.stream()
                          .mapToDouble(no -> no.ixc_RevenueFromVideo)
                          .sum();
  o.ixc_OtherFees =
          networkOperators.stream()
                          .mapToDouble(no -> no.ixc_RevenueFromOther)
                          .sum();

  // Concentration
  // Network HHI
  double[] networkSales = new double[networkOperators.size()];
  for (int i = 0; i < networkOperators.size(); i++) {
    NetworkOperator no = networkOperators.get(i);
    networkSales[i] = no.qty_NetworkOnly +
                      no.qty_Bundled +
                      no.qty_BundledZeroRated;
  }
  o.hhi_Network = HHI(networkSales);

  // Video Content HHI
  List<Double> videoSales = new ArrayList<>();
  for (ContentProvider cp : videoContentProviders) {
    videoSales.add(cp.numAcceptedOffers);
  }
  if (integratedContentAllowed) {
    for (NetworkOperator no : networkOperators) {
      videoSales.add(no.getNumStandaloneContentOffersAccepted() +
                     no.qty_Bundled +
                     no.qty_BundledZeroRated);
    }
  }
  o.hhi_Video = HHI(videoSales);

  // Other Content HHI
  double[] otherSales = new double[otherContentProviders.size()];
  for (int i = 0; i < otherContentProviders.size(); i++) {
    otherSales[i] = otherContentProviders.get(i).numAcceptedOffers;
  }
  o.hhi_Other = HHI(otherSales);


  return o;
}

@Override
public AgencyData getStepData() {
  // Do not output any per-step data.
  return null;
}

@Override
public void enableDebug(PrintStream out) {
  debug = true;
  debugOut = out;
}

/**
 *
 */
private void spacePreferences() {
  List<ContentProvider<?>> vidCPs = new ArrayList<>();
  vidCPs.addAll(videoContentProviders);
  for (NetworkOperator<?> netOp : networkOperators) {
    ContentProvider<?> netOpCP = netOp.icp;
    vidCPs.add(netOpCP);
  }
  spacePreferences(vidCPs);
  spacePreferences(otherContentProviders);
}

private void spacePreferences(List<ContentProvider<?>> providers) {
  int numProviders = providers.size();
  double interval = 1.0 / numProviders;
  double startValue = interval / 2.0;
  double accVal = startValue;
  for (ContentProvider<?> provider : providers) {
    provider.preference = accVal;
    accVal += interval;
  }
}

public final double videoContentValue() {
  return videoContentValue(this.alpha);
}

/**
 * @return the consumer valuation of video content.
 */
public static final double videoContentValue(double alpha) {
  // Make calculations of sector value based on alpha
  double videoContentValue;
  videoContentValue = alpha / (1.0 + alpha);
  return videoContentValue;
}

public final double otherContentValue() {
  return otherContentValue(this.alpha);
}

/**
 * @return the consumer valuation of other content.
 */
public static final double otherContentValue(double alpha) {
  return (1 - videoContentValue(alpha));
}

public final double videoBWIntensity() {
  return videoBWIntensity(this.beta);
}

/**
 * @return the relative intensity of bandwidth usage for video content.
 */
public static final double videoBWIntensity(double beta) {
  // Make calculations of bw intensity based on beta
  double videoBWIntensity;
  videoBWIntensity = beta / (1.0 + beta);
  return videoBWIntensity;
}

public final double otherBWIntensity() {
  return otherBWIntensity(this.beta);
}

/**
 * This value depends on the model parameter beta.
 *
 * @return the relative intensity of bandwidth usage for other content.
 */
public static final double otherBWIntensity(double beta) {
  // Calculations of bw intensity based on beta, both always sum to 1.
  return (1 - videoBWIntensity(beta));
}

/**
 * This object gets translated directly into column headers and data
 * values in output files. For this reason, it needs to be flattened and
 * not hierarchical. Also, it will necessarily be somewhat repetitive
 * because fields from other object cannot be re-used directly.
 * <p>
 * Notes to self:
 * <p>
 * (1) Use first class types to distinguish between null and default
 * values. (2) Don't add values here faster than writing the code that
 * calculates them, this helps to make sure nothing gets missed!  ;)
 */
public static class OutputData {
  /*
  Consumption Variables
   */
  // Standalone offers
  Double qty_NetworkOnly; // Network
  Double qty_VideoOnlyNSP; // Content
  Double qty_VideoOnlyCP;
  Double qty_OtherContent;
  // Bundled offers
  Double qty_Bundled;
  Double qty_BundledZeroRated;
  // Other
  Double  consumerSurplus;


  /*
  Network Operator Variables
   */
  // Investment
  Double nsp_InvestmentNetwork;
  Double nsp_InvestmentVideo;
  // Revenue from various type of offers
  Double nsp_RevenueNetworkOnly;
  Double nsp_RevenueBundled;
  Double nsp_RevenueBundledZeroRated;
  Double nsp_RevenueVideoOnly;
  // Other
  Double nsp_Surplus;


  /*
  Content Provider Variables
   */
  // Investment
  Double cp_InvestmentVideo;
  Double cp_InvestmentOther;
  // Revenue
  Double cp_RevenueVideo;
  Double cp_RevenueOther;
  // Other/Surplus
  Double cp_SurplusVideo;
  Double cp_SurplusOther;

  /*
  Market variables
   */
  // Interconnection
  Double ixc_VideoFees;
  Double ixc_OtherFees;
  // Concentration
  Double hhi_Network;
  Double hhi_Video;
  Double hhi_Other;

}


}
