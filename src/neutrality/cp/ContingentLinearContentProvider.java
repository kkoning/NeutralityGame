package neutrality.cp;

import agency.vector.VectorIndividual;
import neutrality.MarketInfo;
import neutrality.NeutralityModel;
import neutrality.Offers;
import neutrality.Offers.ContentOffer;
import neutrality.probes.*;

import java.util.*;

/**
 The ContingentLinearContentProvider makes investment and pricing decisions
 (in all but the first step), based on a set of condition thresholds and what
 is observed in the environment.  These thresholds are encoded in the
 individual's genome.  Each is a separate binary condition, and may therefore
 be set to correspond to a binary digit that, taken together with the
 evaluation of other conditions, determines a market condition index.
 <p>
 For each value of this index, the genome encodes a constant and coefficient
 vector that are used, in combination with observations from the environment,
 to determine investment and pricing decisions.
 <p>
 This strategy requires rather sizable genome.  Specifically:
 <p>
 (#conditions) + 2^(#conditions) * (#Output Variables) for each contingency
 set. This means that the investment contingencies section requires
 <p>
 3 positions for the condition levels, and
 2^3 = 8; 8 * 4 = 32 positions for the output matrix
 for a total of 35.
 <p>
 The pricing section requires
 3 positions for the condition levels, and
 2^3 = 8; 8 * 5 = 40 positions for the output matrix
 for a total of 43.
 <p>
 That puts the total size of the genome at
 2 for the initial decisions,
 35 for the subsequent investment decisions, and
 43 for the subsequent pricing decisions, for a grant total of:
 80 Loci.
 */
public class ContingentLinearContentProvider
        extends
        AbstractContentProvider<VectorIndividual<Double>> {

private static final int INITIAL_INVESTMENT_IDX       = 0;
private static final int INITIAL_PRICE_IDX            = 1;
private static final int INVESTMENT_CONTINGENCIES_IDX = 2;

// Investment contingency helper.
private final ContingencyHelper<Double> ich;
// Pricing contingency helper.
private final ContingencyHelper<Double> pch;

public ContingentLinearContentProvider() {

  // Investment Contingenices (ics)
  List<EnvironmentalContingency> ics = new ArrayList<>();
  ics.add(new NspNetworkInvestmentAbove());
  ics.add(new IxcPriceAbove());
  ics.add(new NspBundlePremiumAbove());

  // Pricing Contingencies (pcs)
  List<EnvironmentalContingency> pcs = new ArrayList<>();
  pcs.add(new NspBundlePremiumAbove());
  pcs.add(new NspVideoPriceAbove()); // only useful for video providers?
  pcs.add(new CpSectorPriceAbove());

  ich = new ContingencyHelper<>(this,
          INVESTMENT_CONTINGENCIES_IDX,
          ics,
          Arrays.asList(InvestmentOutputs.values()));

  pch = new ContingencyHelper<>(this,
          ich.nextAvailableLoci(),
          pcs,
          Arrays.asList(PricingOutputs.values()));

}

@Override
public Offers.ContentOffer getContentOffer(int step) {
  // First step, no data on past steps available.
  if (step == 0)
    return new Offers.ContentOffer(step,
            this,
            g_e(INITIAL_PRICE_IDX));

  pch.update();
  MarketInfo mi = getModel().getMarketInformation(step - 1);
  Double[] x_n = new Double[PricingOutputs.values().length - 1];
  x_n[0] = mi.getNspIXCPrice();
  x_n[1] = mi.getNspUnbundledPrice();
  x_n[2] = mi.getNspBundlePremium();
  x_n[3] = mi.getNspVideoPrice();

  double price = pch.applyLinearEq(x_n);

  // Now we've got the price, we need to make sure it is >0, as some
  // representations may attempt to evolve a negative price (there is no way
  // to limit this with some representations, such as a linear equation on
  // environmental variables.
  if (price <= Double.MIN_NORMAL)
    price = Double.MIN_NORMAL;

  return new Offers.ContentOffer(step, this, price);
}

@Override
public void step(
        NeutralityModel model, int step, Optional<Double> substep) {

  /*
  For the first step, we have no environmental information on which to base
  investment decisions.  Therefore the initial amount comes directly from the
  genome without further processing.
   */
  double investment = Double.NaN;

  if (step == 0) {
    investment = g_e(INITIAL_INVESTMENT_IDX);
    makeContentInvestment(step, investment);
  }

  if (step > 0) {
    ich.update();
    MarketInfo mi = getModel().getMarketInformation(step - 1);
    Double[] x_n = new Double[InvestmentOutputs.values().length - 1];
    x_n[0] = mi.getNspNetworkInvestment();
    x_n[1] = mi.getNspUnbundledPrice();
    x_n[2] = mi.getNspIXCPrice();

    investment = ich.applyLinearEq(x_n);
  }

  makeContentInvestment(step, investment);
}

private final double g_e(int genome_position) {
  return Math.exp(getManager().getGenomeAt(genome_position));
}

enum InvestmentOutputs {
  CONSTANT,
  NSP_NETWORK_INVESTMENT_COEF,
  NSP_UNBUNDLED_PRICE_COEF,
  IXC_PRICE_COEF
}

enum PricingOutputs {
  CONSTANT,
  IXC_PRICE_COEF,
  NSP_UNBUNDLED_PRICE_COEF,
  NSP_BUNDLING_PREMIUM_COEF,
  NSP_VIDEO_PRICE_COEF
}


}
