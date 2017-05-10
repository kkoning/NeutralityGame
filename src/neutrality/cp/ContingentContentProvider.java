package neutrality.cp;

import java.util.Optional;

import agency.vector.VectorIndividual;
import neutrality.MarketInfo;
import neutrality.NeutralityModel;
import neutrality.Offers.ContentOffer;

public class ContingentContentProvider extends AbstractContentProvider<VectorIndividual<Double>> {

private static final int INVESTMENT_NUM_CONDITIONS = 3;
private static final int PRICING_NUM_CONDITIONS    = 3;

/**
 * Prices are coded directly, but with a positive and negative exponent.
 */
private static final int NUM_LOCI = 1;

/**
 * The total size of the genome. This is calculated in static, so it can be
 * pulled with a debug tool to ensure that the genome size is exactly correct in
 * the configuration file.
 */
@SuppressWarnings("unused")
private static final int GENOME_SIZE; // Calculated in static {}

/*
 * Position Variables.
 */

private static final int INITIAL_INVESTMENT_IDX;
private static final int INITIAL_PRICE_IDX;

private static final int INVESTMENT_BLOCK_IDX;
private static final int PRICING_BLOCK_IDX;

static {
  int pos = 0;
  INITIAL_INVESTMENT_IDX = pos;
  pos += NUM_LOCI;
  INITIAL_PRICE_IDX = pos;
  pos += NUM_LOCI;

  INVESTMENT_BLOCK_IDX = pos;
  int investmentBlockSize = VectorIndividual
      .conditionIndexHelperExpGenomeLength(INVESTMENT_NUM_CONDITIONS, NUM_LOCI);
  pos += investmentBlockSize;

  PRICING_BLOCK_IDX = pos;
  int pricingBlockSize = VectorIndividual
      .conditionIndexHelperExpGenomeLength(PRICING_NUM_CONDITIONS, NUM_LOCI);
  pos += pricingBlockSize;

  GENOME_SIZE = pos;

}

@Override
public ContentOffer getContentOffer(int step) {
  if (step == 0) {
    // In the first step, there are no conditions.
    return new ContentOffer(step, this, getManager().e(INITIAL_PRICE_IDX));
  } else {
    // In all subsequent steps, the outcomes vary based on the conditions.
    MarketInfo mi = getModel().getMarketInformation(step - 1);
    double[] pricingConditions = new double[PRICING_NUM_CONDITIONS];
    pricingConditions[0] = mi.nspBundlePremium;
    pricingConditions[1] = mi.nspVideoPrice; // only useful for video providers?
    pricingConditions[2] = getSectorPrice(mi);

    int priceIdx = getManager().conditionIndexHelperExp(pricingConditions,
                                                        PRICING_BLOCK_IDX,
                                                        NUM_LOCI);
    double price = getManager().e(priceIdx);
    return new ContentOffer(step, this, price);
  }

}

@Override
public void step(NeutralityModel model, int step, Optional<Double> substep) {

  // Make Investment
  double toInvest = Double.NaN;
  if (step == 0) {
    // In the first step, there are no conditions.
    toInvest = getManager().e(INITIAL_INVESTMENT_IDX);
  } else {
    // In all subsequent steps, the outcomes vary based on the conditions.
    MarketInfo mi = getModel().getMarketInformation(step - 1);
    double[] investConditions = new double[INVESTMENT_NUM_CONDITIONS];
    investConditions[0] = mi.nspNetworkInvestment;
    investConditions[1] = mi.nspIXCPrice;
    investConditions[2] = mi.nspBundlePremium;
    int investIdx = getManager().conditionIndexHelperExp(investConditions,
                                                         INVESTMENT_BLOCK_IDX,
                                                         NUM_LOCI);
    toInvest = getManager().e(investIdx);
  }
  makeContentInvestment(step, toInvest);

}

}
