package neutrality;

import java.util.ArrayList;
import java.util.List;

import agency.AbstractAgentModel;
import agency.Agent;
import agency.Fitness;
import agency.SimpleFirm;
import agency.SimpleFitness;
import agency.data.AgencyData;

public class NeutralityModel
    extends AbstractAgentModel {

public Double alpha;
public Double beta;
public Double gamma;
public Double demandPriceCoefficient;
public Double income;
public Double firmEndowment;
public Double nspMarginalCost;

public Double omega;

public Boolean policy0PriceIXC;
public Boolean policyBundlingAllowed;
public Boolean policyZeroRated;
public Boolean policyNSPContentAllowed;

// Quasi-parameters; model is broken-ish without them?
public CapitalCalculationMethod capCalcMethod;
public PolicyRegime             policyRegime;
public DemandAdjustmentMethod   demandAdjustmentMethod;

public Integer maxSteps;

// Some pre-computed intermediate variables
public double videoContentValue;
public double otherContentValue;
public double videoBWIntensity;
public double otherBWIntensity;

public double incomeVideoOnly;
public double incomeOtherOnly;

public double psi;
public double tau;

// Operational Variables
ArrayList<NetworkOperator> networkOperators      = new ArrayList<>();
ArrayList<ContentProvider> videoContentProviders = new ArrayList<>();
ArrayList<ContentProvider> otherContentProviders = new ArrayList<>();

Consumers consumers;

public NeutralityModel() {

}

@Override
public void addAgent(Agent<?, ?> agent) {
  // Classify and track each agent into proper role within model
  if (agent instanceof NetworkOperator)
    networkOperators.add((NetworkOperator) agent);
  else if (agent instanceof ContentProvider) {
    ContentProvider cp = (ContentProvider) agent;
    if (cp.isVideoProvider)
      videoContentProviders.add(cp);
    else
      otherContentProviders.add(cp);
  } else {
    // We don't know what to do with this agent type
    throw new RuntimeException("Unsupported agent type: " + agent);
  }

}

@Override
public Fitness getFitness(Agent<?, ?> agent) {
  // They should all be instances of SimpleFirm
  SimpleFirm<?, ?> firm = (SimpleFirm<?, ?>) agent;
  SimpleFitness fitness = firm.getFitness();
  return fitness;
}

@Override
public Object getAgentDetails(Agent<?, ?> agent) {
  return null;
}

/**
 * General order of events: (1) Step agents. Most won't need this, but in case
 * some do... (2) Collect Offers from Agents. (3) Generate consumption options.
 * (4) Consume (5) Repeat from 1 until maxSteps is reached.
 */
@Override
public boolean step() {

  /*
   * Determine investments and prices. The amounts are saved in variables local
   * to the agents themselves.
   */
  for (NetworkOperator no : networkOperators) {
    no.setKn();
    no.setNetPrices();
    no.setKa();
    no.setContentPrice();
  }
  for (ContentProvider cp : videoContentProviders) {
    cp.setKa();
    cp.setContentPrice();
  }
  for (ContentProvider cp : otherContentProviders) {
    cp.setKa();
    cp.setContentPrice();
  }

  /*
   * Generate consumption options. See getConsumptionOptions for details.
   */
  if (isDebugEnabled())
    debugOut.println("Calculating possible consumption options of consumers");
  List<ConsumptionOption> options = getConsumptionOptions();

  // Debug for completed offers
  if (isDebugEnabled()) {
    debugOut.println("Details of consumption options follows:");
    debugOut.println("Other content only:");
    for (ConsumptionOption co : options) {
      debugOut.println(co);
    }
  }

  consumers.consume(options);

  return true; // Model is only one step.
}

private List<ConsumptionOption> getConsumptionOptions() {
  ArrayList<ConsumptionOption> options = new ArrayList<>();

  // All possible synthetic options with 3rd party content
  for (NetworkOperator no : networkOperators) {
    for (ContentProvider vcp : videoContentProviders) {
      for (ContentProvider ocp : otherContentProviders) {
        SyntheticBundle sb = new SyntheticBundle(no, vcp, ocp, false);
        options.add(sb);
      }
    }
  }

  if (policyNSPContentAllowed) {
    // All possible NSP video content bundles
    for (NetworkOperator no1 : networkOperators) {
      for (NetworkOperator no2 : networkOperators) {
        for (ContentProvider ocp : otherContentProviders) {
          if (no1 == no2) {
            // A Network Operator offering their own content
            if (policyBundlingAllowed) {
              Bundle b = new Bundle(no1, ocp, policyZeroRated);
              options.add(b);
            } else {
              SyntheticBundle sb = new SyntheticBundle(no1, no1, ocp, policyZeroRated);
              options.add(sb);
            }
          } else {
            // When they're not the same company, treat it like 3rd party
            // content
            SyntheticBundle sb = new SyntheticBundle(no1, no2, ocp, false);
            options.add(sb);
          }
        }
      }
    }
  }

  return options;
}

@Override
public void finish() {

}

@Override
public int getMaxSteps() {
  return 1;
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
  return new OutputData(this);

}

//
// The following functions are used by agents to gather information about
// their environment.
//

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

  // Pre-calculate psi and tau from omega.
  psi = tau = omega * (1 - gamma) * 0.5;

  // Pre-calculate individual policy restrictions
  switch (policyRegime) {
    case STRUCTURAL_SEPARATION:
      policyNSPContentAllowed = false;
      policyBundlingAllowed = false;
      policyZeroRated = false;
      break;
    case RESTRICTED:
      policyNSPContentAllowed = true;
      policyBundlingAllowed = false;
      policyZeroRated = false;
      break;
    case BUNDLING_ONLY:
      policyNSPContentAllowed = true;
      policyBundlingAllowed = true;
      policyZeroRated = false;
      break;
    case ZERO_RATING_ONLY:
      policyNSPContentAllowed = true;
      policyBundlingAllowed = false;
      policyZeroRated = true;
      break;
    case BUNDLING_AND_ZERO_RATING:
      policyNSPContentAllowed = true;
      policyBundlingAllowed = true;
      policyZeroRated = true;
      break;
    default:
      throw new RuntimeException();
  }

  // Initialize representative consumer agents.
  consumers = new Consumers(this);

  if (isDebugEnabled())
    debugOut.println("Initialization completed.");
}

public static void verifySaneAmount(double amount) {
  if (Double.isInfinite(amount) || Double.isNaN(amount))
    throw new RuntimeException();
}

public static final double proportionA(double split) {
  return split / (1.0 + split);
}

public static final double proportionB(double split) {
  return 1.0d - proportionA(split);
}

public static enum CapitalCalculationMethod {
  LOG_LOG,
  COBB_DOUGLASS
}

public static enum DemandAdjustmentMethod {
  CONSTANT,
  PRICE
}

public static enum PolicyRegime {
  STRUCTURAL_SEPARATION,
  RESTRICTED,
  BUNDLING_ONLY,
  ZERO_RATING_ONLY,
  BUNDLING_AND_ZERO_RATING
}

}
