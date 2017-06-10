package neutrality;

import java.util.Optional;

import agency.SimpleFirm;
import agency.SimpleFitness;
import agency.vector.VectorIndividual;

public class ContentProvider
    extends SimpleFirm<VectorIndividual<Double>, NeutralityModel> {

/**
 * True if this content provider is in the video market, false if it in the
 * other content market. Set by the AgentFactory.
 */
public Boolean isVideoProvider;

/**
 * If true, this agent should behave as if emulating a competitive market,
 * likely with a fitness function that maximizes consumer surplus subject to a
 * positive account balance.
 */
public Boolean emulateMarket;

// Parameters relevant to consumer value; investment and preference.

public double       Ka      = Double.NaN;
public SalesTracker content = new SalesTracker();

/*
 * IXC paid needs to be tracked directly, because a content provider will pay
 * different amounts of IXC to different network providers.
 */
public double ixcPaid = 0d;

public double fitnessAdjustment = 0d;

/*
 * Genome Layout
 */
public enum Genome {
  CONTENT_INVESTMENT,
  CONTENT_PRICE
}

public ContentProvider() {
  super();
}

public void setKa() {
  double genomeValue = getManager().e(Genome.CONTENT_INVESTMENT.ordinal());
  /*
   * Penalize values below 1 here. The actual Ka is set higher to prevent a
   * negative result when we take the log, but we need to make sure we're
   * preventing genetic drift towards extremely low values that make very
   * little practical difference and might prevent us from finding the hill
   * to climb later.
   */
  if (genomeValue < 1) {
    fitnessAdjustment -= (1 / genomeValue) * 100;
  }
  Ka = 1 + genomeValue;
}

public void setContentPrice() {
  content.price = getManager().e(Genome.CONTENT_PRICE.ordinal());
}

@Override
public void init() {
  // Unnecessary
}

@Override
public void step(NeutralityModel model, int step, Optional<Double> substep) {
  // Unnecessary
}

public double getBalance() {
  double balance = 0d;
  balance += content.revenue();
  balance -= Ka;
  balance -= ixcPaid;
  return balance;
}

@Override
public SimpleFitness getFitness() {
  if (!emulateMarket) {
    /*
     * Use a simple balance of costs / revenues to determine fitness.
     */

    return new SimpleFitness(getBalance() + fitnessAdjustment);

  } else {
    /*
     * Use total utility produced as fitness, but give severe penalties for
     * negative account balance.
     */
    double utilProduced = 0;

    double qtyTerm = Math.pow(content.qty, getModel().gamma);
    double kTerm = Math.pow(Ka, getModel().psi);
    utilProduced = qtyTerm * kTerm;

    double lossPenalty = 0;
    if (getBalance() < 0) {
      lossPenalty = getBalance() * getBalance();
    }

    return new SimpleFitness(utilProduced + fitnessAdjustment - lossPenalty);
  }
}

}
