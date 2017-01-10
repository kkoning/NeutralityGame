package neutrality;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import agency.Agent;
import agency.AgentModel;
import agency.Fitness;
import agency.Individual;
import agency.SimpleFirm;
import agency.SimpleFitness;
import agency.data.AgencyData;
import agency.util.Statistics;
import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

public class NeutralityModel implements AgentModel {

public Double alpha;
public Double beta;

public Double psi;
public Double tau;
public Double capitalCost;
public Double theta;

public Integer numConsumers;
public Double  topIncome;

public Boolean forceZeroPriceIC;
public Boolean bundlingAllowed;
public Boolean zeroRatingAllowed;

public Boolean integratedContentAllowed;

public Integer maxSteps;

List<NetworkOperator<?>> networkOperators;
List<ContentProvider<?>> videoContentProviders;
List<ContentProvider<?>> otherContentProviders;
Consumers                consumers;

boolean firstStep = true;
boolean debug = false;
PrintStream debugOut = null;

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
public boolean step() {

  if (firstStep) {
    if (debug)
      debugOut.println("Executing first step of model");

    if (debug)
      debugOut.println("Initializing Consumers");
    consumers = new Consumers(numConsumers, topIncome, this);
    if (debug) {
      debugOut.println("Consumer properties follow:");
      debugOut.println(consumers.printConsumerProperties());
    }

    if (debug)
      debugOut.println("Spacing preferences of provider agents");
    spacePreferences();
    if (debug) {
      debugOut.println("Content Provider Properties Follow:");
      List<ContentProvider<?>> vidCPs = new ArrayList<>();
      vidCPs.addAll(videoContentProviders);
      for (NetworkOperator<?> netOp : networkOperators) {
        ContentProvider<?> netOpCP = netOp.integratedContentProvider;
        vidCPs.add(netOpCP);
      }
      for (ContentProvider<?> cp : vidCPs)
        debugOut.println(cp);
      for (ContentProvider<?> cp : otherContentProviders)
        debugOut.println(cp);
    }

    firstStep = false;
    if (debug)
      debugOut.println("First step initialization completed.");
  }

  if (debug)
    debugOut.println("Stepping Network Operators");
  // Step each agent; allow them to generate and update offers.
  for (NetworkOperator<?> no : networkOperators)
    no.step(); // Network Operators
  if (debug)
    debugOut.println("Stepping Independent Video Content Providers");
  for (ContentProvider<?> cp : videoContentProviders)
    cp.step(); // Video Content Providers
  if (debug)
    debugOut.println("Stepping Independent Other Content Providers");
  for (ContentProvider<?> cp : otherContentProviders)
    cp.step(); // Other Content Providers
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
    if (cp.getContentOffer() != null)
      videoContentOffers.add(cp.getContentOffer());
  }

  // Other Content Providers
  for (ContentProvider<?> cp : otherContentProviders) {
    if (cp.getContentOffer() != null)
      otherContentOffers.add(cp.getContentOffer());
  }

  /*
   * Print details of offers made by the individual agents
   */
  if (debug) {
    debugOut.println("Details of offers follows:");

    debugOut.println("Network Only Offers:");
    for (NetworkOffer no : networkOnlyOffers)
      debugOut.println(no);

    debugOut.println("Unbundled Video Content Offers:");
    for (ContentOffer co : videoContentOffers)
      debugOut.println(co);

    debugOut.println("Other Content Offers:");
    for (ContentOffer co : otherContentOffers)
      debugOut.println(co);

    debugOut.println("Bundled Network and Video Offers:");
    for (BundledOffer bo : bundledOffers)
      debugOut.println(bo);

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
    for (ConsumptionOption co : options)
      debugOut.println(co);
  }

  /*
   * Consumers consider and consume offers
   */

  // Consumers consider and consume offers.
  // Details are specified in Consumers.procurementProcess
  consumers.procurementProcess(options);

  // Don't terminate early.
  return false;
}

/**
 *
 */
private void spacePreferences() {
  List<ContentProvider<?>> vidCPs = new ArrayList<>();
  vidCPs.addAll(videoContentProviders);
  for (NetworkOperator<?> netOp : networkOperators) {
    ContentProvider<?> netOpCP = netOp.integratedContentProvider;
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

  /* Data on Consumers
   */
  o.consumerSurplus = consumers.getTotalSurplus();


  /*
   * Data on network operators
	 */
  // How many network operators were there in the simulation?
  o.numNetworkOperators = networkOperators.size();

  // What was their total surplus
  o.networkOperatorSurplus = networkOperators.stream().mapToDouble(no -> no.getFitness().getAverageFitness()).sum();

  // Total investment & HHI
  o.networkOperatorInvestment = networkOperators.stream()
          .mapToDouble(no -> no.networkInvestment).sum();

  /*
   * Standalone Network offers
   */
  // Num of offers
  o.numStandaloneNetworkOffersAccepted = networkOperators.stream()
          .mapToInt(no -> no.numStandaloneNetworkOffersAccepted).sum();
  // Revenue
  o.totalStandaloneNetworkRevenue = networkOperators.stream().mapToDouble(
          no -> no.totalStandaloneNetworkRevenue).sum();

  /*
   * Bundled Offers
   */
  o.numBundledNetworkOffersAccepted = networkOperators.stream()
          .mapToInt(no -> no.numBundledOffersAccepted).sum();
  o.numBundledZeroRatedOffersAccepted = networkOperators.stream()
          .mapToInt(no -> no.numBundledZeroRatedOffersAccepted).sum();
  o.totalBundledRevenue = networkOperators.stream().mapToDouble(
          no -> no.totalBundledRevenue).sum();
  o.totalBundledZeroRatedRevenue = networkOperators.stream().mapToDouble(
          no -> no.totalBundledZeroRatedRevenue).sum();


  // NSP revenues from Video IC
  o.totalICFeesFromVideo = networkOperators.stream().mapToDouble(no -> no.totalInterconnectionPaymentsFromVideo).sum();

  // NSP revenues from Other IC
  o.totalICFeesFromOther = networkOperators.stream().mapToDouble(no -> no.totalInterconnectionPaymentsFromOther).sum();



  /*
   * Data on Content Markets, in which both NSPs and some CPs participate.
   */

  // NSP Investment in Video Content
  o.nspVideoContentInvestment = 0.0;
  for (NetworkOperator no : networkOperators) {
    ContentProvider cp = no.integratedContentProvider;
    o.nspVideoContentInvestment += cp.contentInvestment;
  }
  o.cpVideoContentInvestment = videoContentProviders.stream().mapToDouble(cp -> cp.getInvestment()).sum();
  o.cpOtherContentInvestment = otherContentProviders.stream().mapToDouble(cp -> cp.getInvestment()).sum();
  o.totalContentInvestment = o.nspVideoContentInvestment + o.cpVideoContentInvestment + o.cpOtherContentInvestment;


  // Standalone Content Offers & HHI
  // These are by both NSPs and others
  o.numNSPStandaloneVideoOffersAccepted = networkOperators.stream()
          .mapToInt(no -> no.getNumStandaloneContentOffersAccepted()).sum();
  o.numThirdPartyStandaloneVideoOffersAccepted = videoContentProviders.stream()
          .mapToInt(vcp -> vcp.numAcceptedOffers).sum();
  o.numStandaloneNetworkOffersAccepted = o.numNSPStandaloneVideoOffersAccepted
          + o.numThirdPartyStandaloneVideoOffersAccepted;
  o.numStandaloneVideoOffersHHI = Statistics.HHI(
          Stream.concat(
                  networkOperators.stream()
                          .map(no -> no.getNumStandaloneContentOffersAccepted()),
                  videoContentProviders.stream().map(vcp -> vcp.numAcceptedOffers))
                  .toArray(Integer[]::new));

		/*
     * Data on standalone video content providers
		 */
  o.numVideoContentProviders = videoContentProviders.size();
  o.cpVideoProvderSurplus = videoContentProviders.stream().mapToDouble(cp -> cp.getFitness().getAverageFitness()).sum();
  o.cpOtherProviderSurplus = otherContentProviders.stream().mapToDouble(cp -> cp.getFitness().getAverageFitness()).sum();

		/*
     * Data on other content providers
		 */
  o.numOtherContentProvides = otherContentProviders.size();


  /*
   * Market Concentration Figures
   */
  // Network Market
  List<Integer> sales = new ArrayList<>(networkOperators.size());
  for (NetworkOperator<?> no : networkOperators) {
    int totalSold = 0;
    totalSold += no.numStandaloneNetworkOffersAccepted;
    totalSold += no.numBundledOffersAccepted;
    totalSold += no.numBundledZeroRatedOffersAccepted;
    sales.add(totalSold);
  }
  Integer[] salesArray = new Integer[sales.size()];
  salesArray = sales.toArray(salesArray);
  o.networkHHI = Statistics.HHI(salesArray);

  // Video Content Market
  sales = new ArrayList<>(networkOperators.size() + videoContentProviders.size());
  for (NetworkOperator<?> no : networkOperators) {
    int totalSold = 0;
    totalSold += no.getNumStandaloneContentOffersAccepted();
    totalSold += no.numBundledOffersAccepted;
    totalSold += no.numBundledZeroRatedOffersAccepted;
    sales.add(totalSold);
  }
  for (ContentProvider<?> cp : videoContentProviders) {
    int totalSold = 0;
    totalSold += cp.numAcceptedOffers;
    sales.add(totalSold);
  }
  salesArray = new Integer[sales.size()];
  salesArray = sales.toArray(salesArray);
  o.videoHHI = Statistics.HHI(salesArray);

  // Other Content Market
  sales = new ArrayList<>(otherContentProviders.size());
  for (ContentProvider<?> cp : otherContentProviders) {
    int totalSold = 0;
    totalSold += cp.numAcceptedOffers;
    sales.add(totalSold);
  }
  salesArray = new Integer[sales.size()];
  salesArray = sales.toArray(salesArray);
  o.otherHHI = Statistics.HHI(salesArray);

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
 * @return the consumer valuation of video content.
 */
public static final double videoContentValue(double alpha) {
  // Make calculations of sector value based on alpha
  double videoContentValue;
  videoContentValue = alpha / (1.0 + alpha);
  return videoContentValue;
}

public final double videoContentValue() {
  return videoContentValue(this.alpha);
}

/**
 * @return the consumer valuation of other content.
 */
public static final double otherContentValue(double alpha) {
  return (1 - videoContentValue(alpha));
}

public final double otherContentValue() {
  return otherContentValue(this.alpha);
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

public final double videoBWIntensity() {
  return videoBWIntensity(this.beta);
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

public final double otherBWIntensity() {
  return otherBWIntensity(this.beta);
}


public static class OutputData {
    /*
     * This object gets translated directly into column headers and data
		 * values in output files. For this reason, it needs to be flattened and
		 * not hierarchical. Also, it will necessarily be somewhat repetitive
		 * because fields from other object cannot be re-used directly.
		 *
		 * Notes to self:
		 *
		 * (1) Use first class types to distinguish between null and default
		 * values. (2) Don't add values here faster than populating them.
		 *
		 */

  // Use first class

  /*
   * Consumer Variables
   */
  Double consumerSurplus;

  /*
   * Network Operator Variables
   */
  Integer numNetworkOperators;
  Double  networkOperatorSurplus;

  Double networkOperatorInvestment;

  Double totalICFeesFromVideo;
  Double totalICFeesFromOther;


  /*
   * NSP Standalone Network Offers
   */
  Integer numStandaloneNetworkOffersAccepted;
  Double  totalStandaloneNetworkRevenue;


  // TODO
  Integer numBundledNetworkOffersAccepted;
  Integer numBundledZeroRatedOffersAccepted;
  Double  totalBundledRevenue;
  Double  totalBundledZeroRatedRevenue;


  Integer numNSPStandaloneVideoOffersAccepted;
  Integer numThirdPartyStandaloneVideoOffersAccepted;
  Double  numStandaloneVideoOffersHHI;

  /*
   * Video Content Providers
   */
  Integer numVideoContentProviders;
  Double  cpVideoProvderSurplus;


  /*
   * Other Content Providers
   */
  Integer numOtherContentProvides;
  Double  cpOtherProviderSurplus;

  /*
   * Content Market Variables
   */
  Double nspVideoContentInvestment;
  Double cpVideoContentInvestment;
  Double cpOtherContentInvestment;
  Double totalContentInvestment;


  /*
   * Market Concentration
   */
  Double networkHHI;
  Double videoHHI;
  Double otherHHI;

//
//  @Override
//  public List<String> getHeaders() {
//    List<String> headers = new ArrayList<>();
//    headers.add("consumerSurplus");
//    headers.add("numNetworkOperators");
//    headers.add("networkOperatorSurplus");
//    headers.add("networkOperatorInvestment");
//    headers.add("totalICFeesFromVideo");
//    headers.add("totalICFeesFromOther");
//    headers.add("numStandaloneNetworkOffersAccepted");
//    headers.add("numNSPStandaloneVideoOffersAccepted");
//    headers.add("numThirdPartyStandaloneVideoOffersAccepted");
//    headers.add("numStandaloneVideoOffersHHI");
//    headers.add("numVideoContentProviders");
//    headers.add("cpVideoProvderSurplus");
//    headers.add("numOtherContentProvides");
//    headers.add("cpOtherProviderSurplus");
//    headers.add("nspVideoContentInvestment");
//    headers.add("cpVideoContentInvestment");
//    headers.add("cpOtherContentInvestment");
//    headers.add("totalContentInvestment");
//    headers.add("totalStandaloneNetworkRevenue");
//    headers.add("numBundledNetworkOffersAccepted");
//    headers.add("numBundledZeroRatedOffersAccepted");
//    headers.add("totalBundledRevenue");
//    headers.add("totalBundledZeroRatedRevenue");
//    headers.add("networkHHI");
//    headers.add("videoHHI");
//    headers.add("otherHHI");
//
//    return headers;
//  }

//  @Override
//  public List<Object> getValues() {
//    List<Object> values = new ArrayList<>();
//    values.add(consumerSurplus);
//    values.add(numNetworkOperators);
//    values.add(networkOperatorSurplus);
//    values.add(networkOperatorInvestment);
//    values.add(totalICFeesFromVideo);
//    values.add(totalICFeesFromOther);
//    values.add(numStandaloneNetworkOffersAccepted);
//    values.add(numNSPStandaloneVideoOffersAccepted);
//    values.add(numThirdPartyStandaloneVideoOffersAccepted);
//    values.add(numStandaloneVideoOffersHHI);
//    values.add(numVideoContentProviders);
//    values.add(cpVideoProvderSurplus);
//    values.add(numOtherContentProvides);
//    values.add(cpOtherProviderSurplus);
//    values.add(nspVideoContentInvestment);
//    values.add(cpVideoContentInvestment);
//    values.add(cpOtherContentInvestment);
//    values.add(totalContentInvestment);
//    values.add(totalStandaloneNetworkRevenue);
//    values.add(numBundledNetworkOffersAccepted);
//    values.add(numBundledZeroRatedOffersAccepted);
//    values.add(totalBundledRevenue);
//    values.add(totalBundledZeroRatedRevenue);
//    values.add(networkHHI);
//    values.add(videoHHI);
//    values.add(otherHHI);
//
//
//    return values;
//  }
}


}
