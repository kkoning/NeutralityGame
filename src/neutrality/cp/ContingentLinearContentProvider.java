package neutrality.cp;

import java.util.Optional;

import agency.vector.VectorIndividual;
import neutrality.MarketInfo;
import neutrality.NeutralityModel;
import neutrality.Offers;

/**
 * The ContingentLinearContentProvider makes investment and pricing decisions
 * (in all but the first step), based on a set of condition thresholds and what
 * is observed in the environment. These thresholds are encoded in the
 * individual's genome. Each is a separate binary condition, and may therefore
 * be set to correspond to a binary digit that, taken together with the
 * evaluation of other conditions, determines a market condition index.
 * 
 * For each value of this index, the genome encodes a constant and coefficient
 * vector that are used, in combination with observations from the environment,
 * to determine investment and pricing decisions.
 * 
 * This strategy requires rather sizable genome. The exact size can be found either
 * by calculating by hand or by running a debugger and examining the value of
 * TOTAL_GENOME_LENGTH.  It's currently 158.
 * 
 */
public class ContingentLinearContentProvider
    extends
    AbstractContentProvider<VectorIndividual<Double>> {

private static final int INVESTMENT_NUM_CONDITIONS = 3;
private static final int INVESTMENT_NUM_VARIABLES  = 3;

private static final int PRICING_NUM_CONDITIONS = 3;
private static final int PRICING_NUM_VARIABLES  = 4;

private static final int INITIAL_INVESTMENT_IDX = 0;
private static final int INITIAL_PRICE_IDX      = 1;
private static final int INVESTMENT_BLOCK_IDX   = 2;

private static final int INVESTMENT_LOCI_PER_COND = VectorIndividual
    .linearEqExpGenomeLength(INVESTMENT_NUM_VARIABLES);
private static final int INVESTMENT_GENOME_SIZE   = VectorIndividual
    .conditionIndexHelperExpGenomeLength(INVESTMENT_NUM_CONDITIONS,
                                         INVESTMENT_LOCI_PER_COND);

private static final int PRICING_BLOCK_IDX = INVESTMENT_BLOCK_IDX + INVESTMENT_GENOME_SIZE;

private static final int PRICING_LOCI_PER_COND = VectorIndividual
    .linearEqExpGenomeLength(PRICING_NUM_VARIABLES);
private static final int PRICING_GENOME_SIZE   = VectorIndividual
    .conditionIndexHelperExpGenomeLength(PRICING_NUM_CONDITIONS,
                                         PRICING_LOCI_PER_COND);

@SuppressWarnings("unused")
private static final int TOTAL_GENOME_LENGTH = PRICING_BLOCK_IDX + PRICING_GENOME_SIZE;
// Debug says 158.



@Override
public Offers.ContentOffer getContentOffer(int step) {

  // First step, no data on past steps available.
  if (step == 0)
    return new Offers.ContentOffer(step, this, getManager().e(INITIAL_PRICE_IDX));

  /*
   * In all subsequent steps, use the condition system and the linearEqExp
   * system to determine the price.
   */

  // We'll need market information for this...
  MarketInfo mi = getModel().getMarketInformation(step - 1);

  // First, put together the environment conditions
  double[] pricingConditions = new double[PRICING_NUM_CONDITIONS];
  pricingConditions[0] = mi.nspBundlePremium;
  pricingConditions[1] = mi.nspVideoPrice; // only useful for video providers?
  pricingConditions[2] = getSectorPrice(mi);

  // Then collect the observations we'll use for the linear equation for price
  // (remember, coefficients come from the genome).
  double[] pricingVars = new double[PRICING_NUM_VARIABLES];
  pricingVars[0] = mi.nspIXCPrice;
  pricingVars[1] = mi.nspUnbundledPrice;
  pricingVars[2] = mi.nspBundlePremium;
  pricingVars[3] = mi.nspVideoPrice;

  // Compare the observations against the genome to determine which section
  // of the genome to use for coefficients.
  int linearEqPos = getManager().conditionIndexHelper(pricingConditions,
                                                      PRICING_BLOCK_IDX,
                                                      PRICING_LOCI_PER_COND);

  // The price will be determined by that linear equation
  double price = getManager().linearEqExp(linearEqPos, pricingVars);
  return new Offers.ContentOffer(step, this, price);
}

@Override
public void step(
                 NeutralityModel model, int step, Optional<Double> substep) {

  /*
   * For the first step, we have no environmental information on which to base
   * investment decisions. Therefore the initial amount comes directly from the
   * genome without further processing.
   */
  double investment = Double.NaN;

  // First step, no data on past steps available.
  if (step == 0) {
    investment = getManager().e(INITIAL_INVESTMENT_IDX);
    makeContentInvestment(step, investment);
  } else {

    /*
     * In all subsequent steps, use the condition system and the linearEqExp
     * system to determine the investment level.
     */

    // We'll need market information for this.
    MarketInfo mi = getModel().getMarketInformation(step - 1);

    // First, put together the environment conditions
    double[] investConditions = new double[INVESTMENT_NUM_CONDITIONS];
    investConditions[0] = mi.nspNetworkInvestment;
    investConditions[1] = mi.nspIXCPrice;
    investConditions[2] = mi.nspBundlePremium;

    // Then collect the observations we'll use for the linear equation for
    // investment.
    double[] investVars = new double[INVESTMENT_NUM_VARIABLES];
    investVars[0] = mi.nspNetworkInvestment;
    investVars[1] = mi.nspUnbundledPrice;
    investVars[2] = mi.nspIXCPrice;

    // Use the conditions to get the genome position
    int pos = getManager().conditionIndexHelperExp(investConditions,
                                                   INVESTMENT_BLOCK_IDX,
                                                   INVESTMENT_LOCI_PER_COND);

    // The investment amount will be determined by the linear equation
    investment = getManager().linearEqExp(pos, investVars);
    makeContentInvestment(step, investment);

  }

}

}
