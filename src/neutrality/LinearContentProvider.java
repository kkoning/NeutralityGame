package neutrality;

import agency.vector.VectorIndividual;

import java.util.Optional;

/**
 * The LinearContentProvider makes investment and pricing decisions (in all but
 * the first step) , based on a linear equation whose variables correspond to
 * environmental probes and whose coefficients are determined by the
 * inidividual's genome.
 */
public class LinearContentProvider
    extends
    AbstractContentProvider<VectorIndividual<Double>> {

private static final int INITIAL_INVESTMENT_IDX      = 0;
private static final int INITIAL_PRICE_IDX           = 1;
private static final int PRICING_COEFFICIENTS_IDX    = 2;
private static final int INVESTMENT_COEFFICEINTS_IDX = 9;

@Override
public Offers.ContentOffer getContentOffer(int step) {

  double price = Double.NaN; // Must be set or there's an error.

  /*
   * In the first step, as there's no information from previous steps to use for
   * the linear equation, just use genome values directly.
   */
  if (step == 0) {
    price = Math.exp(getManager().getGenomeAt(INITIAL_PRICE_IDX));
  } else {
    double[] envVars = getEnvironmentVariables(step - 1);
    price = applyLinearEq(PRICING_COEFFICIENTS_IDX, envVars);
  }

  return new Offers.ContentOffer(step, this, price);
}

@Override
public void step(NeutralityModel model, int step, Optional<Double> substep) {

  double investment = Double.NaN;
  if (step == 0) {
    investment = Math.exp(getManager().getGenomeAt(INITIAL_INVESTMENT_IDX));
  } else {
    double[] envVars = getEnvironmentVariables(step - 1);
    investment = applyLinearEq(INVESTMENT_COEFFICEINTS_IDX, envVars);
  }
  makeContentInvestment(step, investment);
}

private double[] getEnvironmentVariables(int step) {
  double[] toReturn = new double[6];
  MarketInfo mi = getModel().getMarketInformation(step - 1);
  toReturn[0] = getSectorInvestment(mi);
  toReturn[1] = getSectorPrice(mi);
  toReturn[2] = mi.getNspNetworkInvestment();
  toReturn[3] = mi.getNspBundledPrice();
  toReturn[4] = mi.getNspBundlePremium();
  toReturn[5] = mi.getNspIXCPrice();
  return toReturn;
}

private double applyLinearEq(int start, double[] environmentVariables) {
  /*
   * The constant term is raised to e, and is at the first position in the
   * coeffieients index. All others are multiplied by coefficients from the
   * genome.
   */
  double toReturn = Math.exp(getManager().getGenomeAt(start));
  for (int i = 0; i < environmentVariables.length; i++) {
    double var = environmentVariables[i];
    double coef = getManager().getGenomeAt(start + 1 + i); // 0 is const
    toReturn += var * coef;
  }
  return toReturn;
}

}
