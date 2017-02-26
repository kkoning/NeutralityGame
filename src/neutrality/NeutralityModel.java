package neutrality;

import java.util.ArrayList;
import java.util.List;

import agency.*;
import agency.data.AgencyData;
import com.sun.istack.internal.NotNull;
import neutrality.Offers.NetworkAndVideoBundleOffer;
import neutrality.Offers.NetworkOnlyOffer;

import static agency.util.Misc.BUG;
import static agency.util.Statistics.HHI;
import static neutrality.ContentProvider.ContentData.*;
import static neutrality.NetworkOperator.NetOpData.*;

public class NeutralityModel
        extends AbstractAgentModel {

public Double alpha;
public Double beta;
public Double psi;
public Double tau;
public Double gamma;
public Double demandPriceCoefficient;
public Double income;
public Double firmEndowment;

public Boolean policy0PriceIXC;
public Boolean policyBundlingAllowed;
public Boolean policyZeroRated;
public Boolean policyNSPContentAllowed;

public Integer maxSteps;

// Some pre-computed intermediate variables
public double videoContentValue;
public double otherContentValue;
public double videoBWIntensity;
public double otherBWIntensity;

public double incomeVideoOnly;
public double incomeOtherOnly;

int nspBankruptcies;
int vcpBankruptcies;
int ocpBankruptcies;

// Operational Variables
ArrayList<NetworkOperator> networkOperators;
ArrayList<ContentProvider> videoContentProviders;
ArrayList<ContentProvider> otherContentProviders;
Consumers                  consumersVideoOnly;
Consumers                  consumersOtherOnly;
Consumers                  consumersBoth;

List<Agent> bankruptAgents;


public NeutralityModel() {
}

@Override
public void addAgent(Agent agent) {
  // Classify and track each agent into proper role within model
  if (agent instanceof AbstractNetworkOperator)
    networkOperators.add((AbstractNetworkOperator<?>) agent);
  else if (agent instanceof AbstractContentProvider) {
    AbstractContentProvider<?> cp = (AbstractContentProvider<?>) agent;
    if (cp.isVideoProvider)
      videoContentProviders.add(cp);
    else
      otherContentProviders.add(cp);
  } else
    // We don't know what to do with this agent type
    throw new RuntimeException("Unsupported agent type: " + agent);

  SimpleFirm sf = (SimpleFirm) agent;
  sf.account.receive(firmEndowment);
}

@Override
public Fitness getFitness(Agent agent) {
  // They should all be instances of SimpleFirm
  SimpleFirm firm = (SimpleFirm) agent;
  SimpleFitness fitness = firm.getFitness();
  return fitness;
}

/**
 * General order of events:
 * (1) Step agents.  Most won't need this, but in case some do...
 * (2) Collect Offers from Agents.
 * (3) Generate consumption options.
 * (4) Consume
 * (5) Repeat from 1 until maxSteps is reached.
 */
@Override
public boolean step() {
  //
  // (1) Step agents.
  //
  if (isDebugEnabled())
    debugOut.println("Stepping Network Operators");
  // Step each agent; allow them to generate and update offers.
  for (NetworkOperator no : networkOperators) {
    no.step(this, currentStep, null); // Network Operators
  }
  if (isDebugEnabled())
    debugOut.println("Stepping Independent Video Content Providers");
  for (ContentProvider cp : videoContentProviders) {
    cp.step(this, currentStep, null); // Video Content Providers
  }
  if (isDebugEnabled())
    debugOut.println("Stepping Independent Other Content Providers");
  for (ContentProvider cp : otherContentProviders) {
    cp.step(this, currentStep, null); // Other Content Providers
  }

  //
  // (1.5) Check for bankruptcy & remove bankrupt agents.
  //
  List<NetworkOperator> tmp4 =
          (List<NetworkOperator>) networkOperators.clone();
  for (NetworkOperator no : tmp4) {
    if (no.isBankrupt()) {
      if (isDebugEnabled())
        debugOut.println(no + " is bankrupt, removing");
      networkOperators.remove(no);
      nspBankruptcies++;
      bankruptAgents.add(no);
    }
  }
  List<ContentProvider> tmp5 =
          (List<ContentProvider>) videoContentProviders.clone();
  for (ContentProvider cp : tmp5) {
    if (cp.isBankrupt()) {
      if (isDebugEnabled())
        debugOut.println(cp + " is bankrupt, removing");
      videoContentProviders.remove(cp);
      vcpBankruptcies++;
      bankruptAgents.add(cp);
    }
  }
  tmp5 = (List<ContentProvider>) otherContentProviders.clone();
  for (ContentProvider cp : tmp5) {
    if (cp.isBankrupt()) {
      if (isDebugEnabled())
        debugOut.println(cp + " is bankrupt, removing");
      otherContentProviders.remove(cp);
      ocpBankruptcies++;
      bankruptAgents.add(cp);
    }
  }

  // Consumers do not need to be stepped; behavior is specified.
  if (isDebugEnabled())
    debugOut.println("Collecting offers from networks and providers");

  //
  // (2) Collect offers from Agents
  //
  // Agents generate offers, add them to these lists.
  ArrayList<NetworkOnlyOffer> networkOnlyOffers = new ArrayList<>();
  ArrayList<Offers.ContentOffer> videoContentOffers = new ArrayList<>();
  ArrayList<Offers.ContentOffer> otherContentOffers = new ArrayList<>();
  ArrayList<NetworkAndVideoBundleOffer> bundledOffers = new ArrayList<>();

  // Network Operators
  for (NetworkOperator no : networkOperators) {
    NetworkOnlyOffer noo = no.getNetworkOffer(currentStep);
    if (noo != null)
      networkOnlyOffers.add(noo);

    if (policyNSPContentAllowed) {
      if (policyBundlingAllowed) {
        NetworkAndVideoBundleOffer navbo = no.getBundledOffer(currentStep);
        if (navbo != null)
          bundledOffers.add(navbo);
      } else {
        // NSP decoupled video content offering only allowed when bundling
        // disabled.
        Offers.ContentOffer vco = no.getContentOffer(currentStep);
        if (vco != null)
          videoContentOffers.add(vco);
      }
    }
  }

  // 3rd Party Video Content
  for (ContentProvider vcp : videoContentProviders) {
    Offers.ContentOffer vco = vcp.getContentOffer(currentStep);
    videoContentOffers.add(vco);
  }

  // 3rd Party Other Content
  for (ContentProvider ocp : otherContentProviders) {
    Offers.ContentOffer oco = ocp.getContentOffer(currentStep);
    otherContentOffers.add(oco);
  }

  // Remove offers with zero capital
  List<NetworkOnlyOffer> tmp1 =
          (List<NetworkOnlyOffer>) networkOnlyOffers.clone();
  for (NetworkOnlyOffer noo : tmp1) {
    if (noo.network.getKn(currentStep) <= 0) {
      if (isDebugEnabled())
        debugOut.println("Removing offer " + noo + " for zero capital");
      networkOnlyOffers.remove(noo);
    }
  }
  List<NetworkAndVideoBundleOffer> tmp2 =
          (List<NetworkAndVideoBundleOffer>) bundledOffers.clone();
  for (NetworkAndVideoBundleOffer nvbo : tmp2) {
    if (nvbo.networkOperator.getKn(currentStep) <= 0) {
      if (isDebugEnabled())
        debugOut.println("Removing offer " + nvbo + " for zero capital");
      bundledOffers.remove(nvbo);
    }
    if (nvbo.networkOperator.getKa(currentStep) <= 0) {
      if (isDebugEnabled())
        debugOut.println("Removing offer " + nvbo + " for zero capital");
      bundledOffers.remove(nvbo);
    }
  }
  List<Offers.ContentOffer> tmp3 =
          (List<Offers.ContentOffer>) videoContentOffers.clone();
  for (Offers.ContentOffer co : tmp3) {
    if (co.contentProvider.getKa(currentStep) <= 0) {
      if (isDebugEnabled())
        debugOut.println("Removing offer " + co + " for zero capital");
      videoContentOffers.remove(co);
    }
  }
  tmp3 = (List<Offers.ContentOffer>) otherContentOffers.clone();
  for (Offers.ContentOffer co : tmp3) {
    if (co.contentProvider.getKa(currentStep) <= 0) {
      if (isDebugEnabled())
        debugOut.println("Removing offer " + co + " for zero capital");
      otherContentOffers.remove(co);
    }
  }

  /*
   * Print details of offers made by the individual agents
   */
  if (isDebugEnabled()) {
    debugOut.println("Details of offers follows:");

    debugOut.println("Network Only Offers:");
    for (NetworkOnlyOffer noo : networkOnlyOffers) {
      debugOut.println(noo);
    }

    debugOut.println("Unbundled Video Content Offers:");
    for (Offers.ContentOffer vco : videoContentOffers) {
      debugOut.println(vco);
    }

    debugOut.println("Other Content Offers:");
    for (Offers.ContentOffer oco : otherContentOffers) {
      debugOut.println(oco);
    }

    debugOut.println("Bundled Network and Video Offers:");
    for (NetworkAndVideoBundleOffer nvbo : bundledOffers) {
      debugOut.println(nvbo);
    }

  }


  //
  // (3) Generate Consumption Options
  //
  if (isDebugEnabled())
    debugOut.println("Calculating possible consumption options of consumers");

  ArrayList<ConsumptionOption> otherOnlyOptions = new ArrayList<>();
  ArrayList<ConsumptionOption> videoOnlyOptions = new ArrayList<>();
  ArrayList<ConsumptionOption> bothContentOptions = new ArrayList<>();

  /*
   * Now that we have a list of all the offers made by network operators
   * and content providers, we need to generate a list of possible
	 * consumption options for consumers to consider.
	 */
  determineOptions(this,
                   networkOnlyOffers,
                   bundledOffers,
                   videoContentOffers,
                   otherContentOffers,
                   otherOnlyOptions,
                   videoOnlyOptions,
                   bothContentOptions);

  // Debug for completed offers
  if (isDebugEnabled()) {
    debugOut.println("Details of consumption options follows:");
    debugOut.println("Other content only:");
    for (ConsumptionOption co : otherOnlyOptions) {
      debugOut.println(co);
    }
    debugOut.println("Video content only:");
    for (ConsumptionOption co : videoOnlyOptions) {
      debugOut.println(co);
    }
    debugOut.println("Both video and other content:");
    for (ConsumptionOption co : bothContentOptions) {
      debugOut.println(co);
    }
  }


  //
  // (4) Consumers consume!
  //
  consumersOtherOnly.consume(otherOnlyOptions);
  consumersVideoOnly.consume(videoOnlyOptions);
  consumersBoth.consume(bothContentOptions);


  //
  // (5) Repeat until out of steps
  //
  currentStep++;
  return false; // Don't end early
}

/**
 * Given a list of different kinds of offers from network operators and
 * content providers, this function returns a list of all possible and
 * allowable combinations of consumption.
 *
 * @param networkOnlyOffers
 * @param videoContentOffers
 * @param bundledOffers
 * @return
 */
public static final void determineOptions(
        @NotNull NeutralityModel model,
        @NotNull List<NetworkOnlyOffer> networkOnlyOffers,
        @NotNull List<Offers.NetworkAndVideoBundleOffer> bundledOffers,
        @NotNull List<Offers.ContentOffer> videoContentOffers,
        @NotNull List<Offers.ContentOffer> otherContentOffers,
        @NotNull List<ConsumptionOption> otherOnlyOptions,
        @NotNull List<ConsumptionOption> videoOnlyOptions,
        @NotNull List<ConsumptionOption> bothContentOptions
) {

  if (model == null)
    BUG("model was null");
  if (networkOnlyOffers == null)
    BUG("networkOnlyOffers was null");
  if (bundledOffers == null)
    BUG("bundledOffers was null");
  if (videoContentOffers == null)
    BUG("videoContentOffers was null");
  if (bundledOffers.size() > 0)
    if (!model.policyBundlingAllowed)
      BUG("Model disallowed bundling, but there are >0 bundled offers");

  /*
  With NetworkOnlyOffers, we need to put together all possible bundles of
  consumption.
   */
  for (Offers.NetworkOnlyOffer networkOnlyOffer : networkOnlyOffers) {
    // Video content but not other content
    for (Offers.ContentOffer videoContentOffer : videoContentOffers) {
      ConsumptionOption option =
              new ConsumptionOption(model,
                                    networkOnlyOffer,
                                    null,
                                    videoContentOffer,
                                    null);
      videoOnlyOptions.add(option);
    }

    // No video content, but other content
    for (Offers.ContentOffer otherContentOffer : otherContentOffers) {
      ConsumptionOption option =
              new ConsumptionOption(model,
                                    networkOnlyOffer,
                                    null,
                                    null,
                                    otherContentOffer);
      otherOnlyOptions.add(option);
    }
    // Both integrated and other content
    for (Offers.ContentOffer videoContentOffer : videoContentOffers) {
      for (Offers.ContentOffer otherContentOffer : otherContentOffers) {
        ConsumptionOption option =
                new ConsumptionOption(model,
                                      networkOnlyOffer,
                                      null,
                                      videoContentOffer,
                                      otherContentOffer);
        bothContentOptions.add(option);
      }
    }

  }

  /*
  Some offers will include network service bundled with the network
  operator's own video content service.  Consumers have the option of either
  adding other content services or not.
   */
  for (Offers.NetworkAndVideoBundleOffer bundledOffer : bundledOffers) {
    // Create option without other content included.
    ConsumptionOption option =
            new ConsumptionOption(model,
                                  null,
                                  bundledOffer,
                                  null,
                                  null);
    videoOnlyOptions.add(option);

    // Create option with other content included.  There'll be one option
    // for each other content offer.
    for (Offers.ContentOffer otherContentOffer : otherContentOffers) {
      option = new ConsumptionOption(model,
                                     null,
                                     bundledOffer,
                                     null,
                                     otherContentOffer);
      bothContentOptions.add(option);
    }
  }
}

@Override
public void finish() {

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
  Put bankrupt agents back into their lists for stats processing.
   */
  for (Agent a : bankruptAgents) {
    if (a instanceof NetworkOperator) {
      networkOperators.add((NetworkOperator) a);
    } else {
      ContentProvider cp = (ContentProvider) a;
      if (cp.isVideo()) {
        videoContentProviders.add(cp);
      } else {
        otherContentProviders.add(cp);
      }
    }
  }

  /*
  Consumption Data
   */
  o.utilityVideoOnly = consumersVideoOnly.accumulatedUtility;
  o.utilityOtherOnly = consumersOtherOnly.accumulatedUtility;
  o.utilityBoth = consumersBoth.accumulatedUtility;

  /*
  Data from Network Operators
   */
  for (NetworkOperator no : networkOperators) {
    o.nspQtyNetworkOnly += no.getNetOpData(QUANTITY_NETWORK);
    o.nspRevNetworkOnly += no.getNetOpData(REVENUE_NETWORK);

    o.nspQtyVideoOnly += no.getContentData(QUANTITY);
    o.nspRevVideoOnly += no.getContentData(REVENUE);

    o.nspQtyBundle += no.getNetOpData(QUANTITY_BUNDLE);
    o.nspRevBundle += no.getNetOpData(REVENUE_BUNDLE);

    o.nspQtyIxcVideo += no.getNetOpData(QUANTITY_IXC_VIDEO);
    o.nspRevIxcVideo += no.getNetOpData(REVENUE_IXC_VIDEO);

    o.nspQtyIxcOther += no.getNetOpData(QUANTITY_IXC_OTHER);
    o.nspRevIxcOther += no.getNetOpData(REVENUE_IXC_OTHER);

    o.nspKn += no.getNetOpData(INVESTMENT_NETWORK);
    o.nspKa += no.getContentData(INVESTMENT);

    o.nspBalance += no.getAccount().getBalance();
  }

  /*
  Data from Content Providers
   */
  for (ContentProvider vcp : videoContentProviders) {
    o.vcpQty += vcp.getContentData(QUANTITY);
    o.vcpRev += vcp.getContentData(REVENUE);
    o.vcpKa += vcp.getContentData(INVESTMENT);
    o.vcpBalance += vcp.getContentData(BALANCE);
  }
  for (ContentProvider ocp : otherContentProviders) {
    o.ocpQty += ocp.getContentData(QUANTITY);
    o.ocpRev += ocp.getContentData(REVENUE);
    o.ocpKa += ocp.getContentData(INVESTMENT);
    o.ocpBalance += ocp.getContentData(BALANCE);
  }


  /*
  Market Information
   */
  // Network HHI
  double[] networkSales = new double[networkOperators.size()];
  for (int i = 0; i < networkOperators.size(); i++) {
    NetworkOperator no = networkOperators.get(i);
    networkSales[i] = no.getNetOpData(QUANTITY_NETWORK) +
                      no.getNetOpData(QUANTITY_BUNDLE);
  }
  o.hhiNetwork = HHI(networkSales);

  // Video Content HHI
  List<Double> videoSales = new ArrayList<>();
  for (ContentProvider cp : videoContentProviders) {
    videoSales.add(cp.getContentData(QUANTITY));
  }
  if (policyNSPContentAllowed) {
    for (NetworkOperator no : networkOperators) {
      videoSales.add(no.getContentData(QUANTITY) +
                     no.getNetOpData(QUANTITY_BUNDLE));
    }
  }
  o.hhiVideo = HHI(videoSales);

  // Other Content HHI
  double[] otherSales = new double[otherContentProviders.size()];
  for (int i = 0; i < otherContentProviders.size(); i++) {
    ContentProvider vcp = otherContentProviders.get(i);
    otherSales[i] = vcp.getContentData(QUANTITY);
  }
  o.hhiOther = HHI(otherSales);

  // Bankruptcies
  o.nspBankruptcies = nspBankruptcies;
  o.vcpBankruptcies = vcpBankruptcies;
  o.ocpBankruptcies = ocpBankruptcies;

  return o;
}

@Override
public AgencyData getStepData() {
  // Do not output any per-step data.
  return null;
}

@Override
public void init() {
  // Initialize consumers, print information for debug
  if (isDebugEnabled())
    debugOut.println("Initializing NeutralityModel");

  networkOperators = new ArrayList<>();
  videoContentProviders = new ArrayList<>();
  otherContentProviders = new ArrayList<>();

  bankruptAgents = new ArrayList<>();

  // Pre-calculating some common intermediate variables.
  videoContentValue = alpha / (1.0d + alpha);
  otherContentValue = 1 - videoContentValue;
  videoBWIntensity = beta / (1.0d + beta);
  otherBWIntensity = 1 - videoBWIntensity;
  if (isDebugEnabled()) {
    debugOut.println("videoContentValue = " + videoContentValue);
    debugOut.println("otherContentValue = " + otherContentValue);
    debugOut.println("videoBWIntensity = " + videoBWIntensity);
    debugOut.println("otherBWIntensity = " + otherBWIntensity);
  }

  // Pre-calculating the income for different consumer groups.
  incomeVideoOnly = income * videoContentValue;
  incomeOtherOnly = income * otherContentValue;

  // Initialize representative consumer agents.
  consumersBoth = new Consumers(this, income);
  consumersVideoOnly = new Consumers(this, incomeVideoOnly);
  consumersOtherOnly = new Consumers(this, incomeOtherOnly);


  if (isDebugEnabled())
    debugOut.println("Initialization completed.");
}

}
