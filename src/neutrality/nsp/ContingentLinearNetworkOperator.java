package neutrality.nsp;

import java.util.Optional;

import agency.vector.VectorIndividual;
import neutrality.MarketInfo;
import neutrality.NeutralityModel;
import neutrality.Offers;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkAndVideoBundleOffer;
import neutrality.Offers.NetworkOnlyOffer;

/**
 * 
 * List of Decisions necessary - Network Investment - Content Investment -
 * Network Connection Price - Network Bandwidth Price - IXC Price
 * 
 * All of these decisions need to be made both for the first period as well as
 * every subsequent period.
 * 
 */
public class ContingentLinearNetworkOperator
    extends AbstractNetworkOperator<VectorIndividual<Double>> {

/*
 * Layout of the Genome
 */
@SuppressWarnings("unused")
private static final int genomeSize; // For debugging

// Positions for the initial decision variables.
private static final int POS_INIT_NET_INVEST;
private static final int POS_INIT_VID_INVEST;
private static final int POS_INIT_NET_CONN_PRICE_LEVEL;
private static final int POS_INIT_NET_CONN_PRICE_BALANCE;
private static final int POS_INIT_BUN_CONN_PRICE_LEVEL;
private static final int POS_INIT_BUN_CONN_PRICE_BALANCE;
private static final int POS_INIT_VID_PRICE;
private static final int POS_INIT_IXC_PRICE;

// The number of variables for each decision's linear eq and # of conditions
// (Currently the same variables)
private static final int NET_INVEST_VARS             = 4;
private static final int VID_INVEST_VARS             = NET_INVEST_VARS;
private static final int NET_CONN_PRICE_LEVEL_VARS   = 4;
private static final int NET_CONN_PRICE_BALANCE_VARS = NET_CONN_PRICE_LEVEL_VARS;
private static final int BUN_CONN_PRICE_LEVEL_VARS   = NET_CONN_PRICE_LEVEL_VARS;
private static final int BUN_CONN_PRICE_BALANCE_VARS = NET_CONN_PRICE_LEVEL_VARS;
private static final int VID_PRICE_VARS              = NET_CONN_PRICE_LEVEL_VARS;
private static final int IXC_PRICE_VARS              = 4;

private static final int NET_INVEST_CONDS             = NET_INVEST_VARS;
private static final int VID_INVEST_CONDS             = NET_INVEST_CONDS;
private static final int NET_CONN_PRICE_LEVEL_CONDS   = NET_CONN_PRICE_LEVEL_VARS;
private static final int NET_CONN_PRICE_BALANCE_CONDS = NET_CONN_PRICE_LEVEL_CONDS;
private static final int BUN_CONN_PRICE_LEVEL_CONDS   = NET_CONN_PRICE_LEVEL_CONDS;
private static final int BUN_CONN_PRICE_BALANCE_CONDS = NET_CONN_PRICE_LEVEL_CONDS;
private static final int VID_PRICE_CONDS              = NET_CONN_PRICE_LEVEL_CONDS;
private static final int IXC_PRICE_CONDS              = 4;

// Based on the number of variables, we can determine the genome positions of
// each of these linear equation blocks.
private static final int POS_NET_INVEST_BLOCK;
private static final int POS_VID_INVEST_BLOCK;
private static final int POS_NET_CONN_PRICE_LEVEL_BLOCK;
private static final int POS_NET_CONN_PRICE_BALANCE_BLOCK;
private static final int POS_BUN_CONN_PRICE_LEVEL_BLOCK;
private static final int POS_BUN_CONN_PRICE_BALANCE_BLOCK;
private static final int POS_VID_PRICE_BLOCK;
private static final int POS_IXC_PRICE_BLOCK;

/**
 * We're going to do things more explicitly in a static block rather than just
 * making do with assignment in the static variable declarations.
 */
static {
  int pos = 0; // Starting position;

  POS_INIT_NET_INVEST = pos++;
  POS_INIT_VID_INVEST = pos++;
  POS_INIT_NET_CONN_PRICE_LEVEL = pos++;
  POS_INIT_NET_CONN_PRICE_BALANCE = pos++;
  POS_INIT_BUN_CONN_PRICE_LEVEL = pos++;
  POS_INIT_BUN_CONN_PRICE_BALANCE = pos++;
  POS_INIT_VID_PRICE = pos++;
  POS_INIT_IXC_PRICE = pos++;

  // Mark blocks and advance pos by the size of those blocks.
  POS_NET_INVEST_BLOCK = pos;
  pos += VectorIndividual
      .conditionIndexHelperExpGenomeLength(NET_INVEST_CONDS,
                                           VectorIndividual
                                               .linearEqExpGenomeLength(NET_INVEST_VARS));
  POS_VID_INVEST_BLOCK = pos;
  pos += VectorIndividual
      .conditionIndexHelperExpGenomeLength(VID_INVEST_CONDS,
                                           VectorIndividual
                                               .linearEqExpGenomeLength(VID_INVEST_VARS));
  POS_NET_CONN_PRICE_LEVEL_BLOCK = pos;
  pos += VectorIndividual
      .conditionIndexHelperExpGenomeLength(NET_CONN_PRICE_LEVEL_CONDS,
                                           VectorIndividual
                                               .linearEqExpGenomeLength(NET_CONN_PRICE_LEVEL_VARS));
  POS_NET_CONN_PRICE_BALANCE_BLOCK = pos;
  pos += VectorIndividual
      .conditionIndexHelperExpGenomeLength(NET_CONN_PRICE_BALANCE_CONDS,
                                           VectorIndividual
                                               .linearEqExpGenomeLength(NET_CONN_PRICE_BALANCE_VARS));
  POS_BUN_CONN_PRICE_LEVEL_BLOCK = pos;
  pos += VectorIndividual
      .conditionIndexHelperExpGenomeLength(BUN_CONN_PRICE_LEVEL_CONDS,
                                           VectorIndividual
                                               .linearEqExpGenomeLength(BUN_CONN_PRICE_LEVEL_VARS));
  POS_BUN_CONN_PRICE_BALANCE_BLOCK = pos;
  pos += VectorIndividual
      .conditionIndexHelperExpGenomeLength(BUN_CONN_PRICE_BALANCE_CONDS,
                                           VectorIndividual
                                               .linearEqExpGenomeLength(BUN_CONN_PRICE_BALANCE_VARS));
  POS_VID_PRICE_BLOCK = pos;
  pos += VectorIndividual
      .conditionIndexHelperExpGenomeLength(VID_PRICE_CONDS,
                                           VectorIndividual
                                               .linearEqExpGenomeLength(VID_PRICE_VARS));

  POS_IXC_PRICE_BLOCK = pos;
  pos += VectorIndividual
      .conditionIndexHelperExpGenomeLength(IXC_PRICE_CONDS,
                                           VectorIndividual
                                               .linearEqExpGenomeLength(IXC_PRICE_VARS));

  genomeSize = pos;

}

@Override
public void step(NeutralityModel model, int step, Optional<Double> substep) {

  /*
   * Determine investment levels and make investments.
   */
  double toInvestNetwork = Double.NaN;
  double toInvestContent = Double.NaN;
  double ixcPrice = Double.NaN;
  if (step == 0) {
    // For the first step, use directly encoded values.
    // Investment
    toInvestNetwork = getManager().e(POS_INIT_NET_INVEST);
    toInvestContent = getManager().e(POS_INIT_VID_INVEST);

    // IXC
    ixcPrice = getManager().e(POS_INIT_IXC_PRICE);
  } else {
    /*
     * In all subsequent steps, use a linear equation.
     */
    // First, determine the position in the genome given conditions.
    int netInvestPos = getManager()
        .conditionIndexHelperExp(netInvestmentConds(),
                                 POS_NET_INVEST_BLOCK,
                                 VectorIndividual.linearEqExpGenomeLength(NET_INVEST_VARS));

    int vidInvestPos = getManager()
        .conditionIndexHelperExp(vidInvestmentConds(),
                                 POS_VID_INVEST_BLOCK,
                                 VectorIndividual.linearEqExpGenomeLength(VID_INVEST_VARS));

    // Then use the linear equation with coefficients from that position
    toInvestNetwork = getManager().linearEqExp(netInvestPos, netInvestmentVars());
    toInvestContent = getManager().linearEqExp(vidInvestPos, vidInvestmentVars());

    // Now determine the IXC price
    int ixcPricePos = getManager()
        .conditionIndexHelperExp(ixcPriceConds(),
                                 POS_IXC_PRICE_BLOCK,
                                 VectorIndividual.linearEqExpGenomeLength(IXC_PRICE_VARS));
    
    ixcPrice = getManager().linearEqExp(ixcPricePos, ixcPriceVars());

  }

  makeNetworkInvestment(step, toInvestNetwork);
  makeContentInvestment(step, toInvestContent);
  setIxcPrice(step, ixcPrice);

}

@Override
public NetworkOnlyOffer getNetworkOffer(int step) {
  double priceLevel;
  double priceBalance;
  if (step == 0) {
    // For the first step, use directly encoded values.
    priceLevel = getManager().e(POS_INIT_NET_CONN_PRICE_LEVEL);
    priceBalance = getManager().e(POS_INIT_NET_CONN_PRICE_BALANCE);
  } else {
    /*
     * In all subsequent steps, use a linear equation.
     */
    // First, determine the position in the genome given conditions.
    int priceLevelPos = getManager()
        .conditionIndexHelperExp(netPriceLevelConds(),
                                 POS_NET_CONN_PRICE_LEVEL_BLOCK,
                                 VectorIndividual
                                     .linearEqExpGenomeLength(NET_CONN_PRICE_LEVEL_VARS));

    int priceBalancePos = getManager()
        .conditionIndexHelperExp(netPriceBalanceConds(),
                                 POS_NET_CONN_PRICE_BALANCE_BLOCK,
                                 VectorIndividual
                                     .linearEqExpGenomeLength(NET_CONN_PRICE_BALANCE_VARS));

    priceLevel = getManager().linearEqExp(priceLevelPos,
                                          netPriceLevelVars());
    priceBalance = getManager().linearEqExp(priceBalancePos,
                                            netPriceBalanceVars());
  }

  double networkPrice = priceLevel * AbstractNetworkOperator.proportionA(priceBalance);
  double bandwitthPrice = priceLevel * AbstractNetworkOperator.proportionB(priceBalance);

  Offers.NetworkOnlyOffer noo = new Offers.NetworkOnlyOffer(step,
                                                            this,
                                                            networkPrice,
                                                            bandwitthPrice);
  return noo;
}

@Override
public ContentOffer getContentOffer(int step) {

  double priceLevel;
  if (step == 0) {
    // For the first step, use directly encoded values.
    priceLevel = getManager().e(POS_INIT_VID_PRICE);
  } else {
    /*
     * In all subsequent steps, use a linear equation.
     */
    // First, determine the position in the genome given conditions.
    int priceLevelPos = getManager()
        .conditionIndexHelperExp(vidPriceConds(),
                                 POS_VID_PRICE_BLOCK,
                                 VectorIndividual
                                     .linearEqExpGenomeLength(VID_PRICE_VARS));
    priceLevel = getManager().linearEqExp(priceLevelPos,
                                          vidPriceVars());
  }

  Offers.ContentOffer vco = new Offers.ContentOffer(step,
                                                    this,
                                                    priceLevel);
  return vco;

}

@Override
public NetworkAndVideoBundleOffer getBundledOffer(int step) {
  double priceLevel;
  double priceBalance;
  if (step == 0) {
    // For the first step, use directly encoded values.
    priceLevel = getManager().e(POS_INIT_BUN_CONN_PRICE_LEVEL);
    priceBalance = getManager().e(POS_INIT_BUN_CONN_PRICE_BALANCE);
  } else {
    /*
     * In all subsequent steps, use a linear equation.
     */
    // First, determine the position in the genome given conditions.
    int priceLevelPos = getManager()
        .conditionIndexHelperExp(bunPriceLevelConds(),
                                 POS_BUN_CONN_PRICE_LEVEL_BLOCK,
                                 VectorIndividual
                                     .linearEqExpGenomeLength(BUN_CONN_PRICE_LEVEL_VARS));

    int priceBalancePos = getManager()
        .conditionIndexHelperExp(bunPriceBalanceConds(),
                                 POS_BUN_CONN_PRICE_BALANCE_BLOCK,
                                 VectorIndividual
                                     .linearEqExpGenomeLength(BUN_CONN_PRICE_BALANCE_VARS));

    priceLevel = getManager().linearEqExp(priceLevelPos,
                                          bunPriceLevelVars());
    priceBalance = getManager().linearEqExp(priceBalancePos,
                                            bunPriceBalanceVars());
  }

  double bunPrice = priceLevel * AbstractNetworkOperator.proportionA(priceBalance);
  double bwPrice = priceLevel * AbstractNetworkOperator.proportionB(priceBalance);

  Offers.NetworkAndVideoBundleOffer bo = new Offers.NetworkAndVideoBundleOffer(
                                                                               step,
                                                                               this,
                                                                               bunPrice,
                                                                               bwPrice);
  return bo;

}

private double[] netInvestmentVars() {
  MarketInfo mi = getModel().getMarketInformation(getModel().currentStep - 1);
  double[] toReturn = new double[NET_INVEST_VARS];
  // Content Investment in both sectors
  toReturn[0] = mi.cpVideoInvestment;
  toReturn[1] = mi.cpOtherInvestment;
  toReturn[2] = mi.nspVideoInvestment;
  // Average investment by all NSPs
  toReturn[3] = mi.nspNetworkInvestment;
  return toReturn;
}

private double[] netInvestmentConds() {
  return netInvestmentVars();
}

/*
 * Same as netInvestmentVars, currently.
 */
private double[] vidInvestmentVars() {
  return netInvestmentVars();
}

private double[] vidInvestmentConds() {
  return vidInvestmentVars();
}

private double[] netPriceLevelVars() {
  MarketInfo mi = getModel().getMarketInformation(getModel().currentStep - 1);
  double[] toReturn = new double[NET_CONN_PRICE_LEVEL_VARS];
  toReturn[0] = mi.cpOtherInvestment;
  toReturn[1] = mi.cpOtherPrice;
  toReturn[2] = mi.cpVideoInvestment;
  toReturn[3] = mi.cpVideoPrice;
  return toReturn;
}

private double[] netPriceLevelConds() {
  return netPriceLevelVars();
}

private double[] netPriceBalanceVars() {
  return netPriceLevelVars();
}

private double[] netPriceBalanceConds() {
  return netPriceLevelVars();
}

private double[] vidPriceVars() {
  return netPriceLevelVars();
}

private double[] vidPriceConds() {
  return netPriceLevelVars();
}

private double[] bunPriceBalanceVars() {
  return netPriceLevelVars();
}

private double[] bunPriceBalanceConds() {
  return netPriceLevelVars();
}

private double[] bunPriceLevelVars() {
  return netPriceLevelVars();
}

private double[] bunPriceLevelConds() {
  return netPriceLevelVars();
}

private double[] ixcPriceVars() {
  return netPriceLevelVars();
}

private double[] ixcPriceConds() {
  return netPriceLevelVars();
}

}
