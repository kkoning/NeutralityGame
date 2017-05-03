package neutrality.cp;

import java.util.Optional;

import agency.vector.VectorIndividual;
import neutrality.MarketInfo;
import neutrality.NeutralityModel;
import neutrality.Offers;

/**
 * The LinearContentProvider makes investment and pricing decisions (in all but
 * the first step) , based on a linear equation whose variables correspond to
 * environmental probes and whose coefficients are determined by the
 * inidividual's genome.
 */
public class LinearContentProvider
    extends
    AbstractContentProvider<VectorIndividual<Double>> {

private static final int NUM_COEFFICIENTS = 6;

private static final int INITIAL_INVESTMENT_IDX      = 0;
private static final int INITIAL_PRICE_IDX           = 1;
private static final int PRICING_COEFFICIENTS_IDX    = 2;
private static final int INVESTMENT_COEFFICEINTS_IDX = PRICING_COEFFICIENTS_IDX
    + VectorIndividual.linearEqExpGenomeLength(NUM_COEFFICIENTS);

@Override
public Offers.ContentOffer getContentOffer(int step) {

  double price = Double.NaN; // Must be set or there's an error.

  /*
   * In the first step, as there's no information from previous steps to use for
   * the linear equation, just use genome values directly.
   */
  if (step == 0) {
    price = Math.exp(getManager().gene(INITIAL_PRICE_IDX));
  } else {
    double[] envVars = getEnvironmentVariables(step - 1);
    price = getManager().linearEqExp(PRICING_COEFFICIENTS_IDX, envVars);
  }

  return new Offers.ContentOffer(step, this, price);
}

@Override
public void step(NeutralityModel model, int step, Optional<Double> substep) {

  double investment = Double.NaN;
  if (step == 0) {
    investment = Math.exp(getManager().gene(INITIAL_INVESTMENT_IDX));
  } else {
    double[] envVars = getEnvironmentVariables(step - 1);
    investment = getManager().linearEqExp(INVESTMENT_COEFFICEINTS_IDX, envVars);
  }
  makeContentInvestment(step, investment);
}

private double[] getEnvironmentVariables(int step) {
  double[] toReturn = new double[NUM_COEFFICIENTS];
  MarketInfo mi = getModel().getMarketInformation(step);
  toReturn[0] = getSectorInvestment(mi);
  toReturn[1] = getSectorPrice(mi);
  toReturn[2] = mi.nspNetworkInvestment;
  toReturn[3] = mi.nspBundledPrice;
  toReturn[4] = mi.nspBundlePremium;
  toReturn[5] = mi.nspIXCPrice;
  return toReturn;
}

}
