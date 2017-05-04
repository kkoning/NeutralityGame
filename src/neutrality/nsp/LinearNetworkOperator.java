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
 * Network Operators need to make a significantly larger number of decisions.
 * Specifically, these include: (1) Network investment, (2) Content investment,
 * (3) Network Price, (4) Network BW Price, (5) Bundled Price, (6) Bundled BW
 * Price, (7) Unbundled Content Price.
 * 
 * This means its genome is going to be somewhat large and complex, though not
 * as bad as ContingentLinearNetworkOperator. In addition to initial values for
 * each of these decisions, in all subsequent decisions each one of these will
 * require blocks of genome to use linearEqExp.
 * 
 * 
 * @author liara
 *
 */
public class LinearNetworkOperator extends AbstractNetworkOperator<VectorIndividual<Double>> {

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

// The number of variables for each decision's linear eq
private static final int NET_INVEST_VARS             = 4;
private static final int VID_INVEST_VARS             = NET_INVEST_VARS;
private static final int NET_CONN_PRICE_LEVEL_VARS   = 4;
private static final int NET_CONN_PRICE_BALANCE_VARS = NET_CONN_PRICE_LEVEL_VARS;
private static final int BUN_CONN_PRICE_LEVEL_VARS   = NET_CONN_PRICE_LEVEL_VARS;
private static final int BUN_CONN_PRICE_BALANCE_VARS = NET_CONN_PRICE_LEVEL_VARS;
private static final int VID_PRICE_VARS              = NET_CONN_PRICE_LEVEL_VARS;

// Based on the number of variables, we can determine the genome positions of
// each of these linear equation blocks.
private static final int POS_NET_INVEST_BLOCK;
private static final int POS_VID_INVEST_BLOCK;
private static final int POS_NET_CONN_PRICE_LEVEL_BLOCK;
private static final int POS_NET_CONN_PRICE_BALANCE_BLOCK;
private static final int POS_BUN_CONN_PRICE_LEVEL_BLOCK;
private static final int POS_BUN_CONN_PRICE_BALANCE_BLOCK;
private static final int POS_VID_PRICE_BLOCK;

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

  // Mark blocks and advance pos by the size of those blocks.
  POS_NET_INVEST_BLOCK = pos;
  pos += VectorIndividual.linearEqExpGenomeLength(NET_INVEST_VARS);
  POS_VID_INVEST_BLOCK = pos;
  pos += VectorIndividual.linearEqExpGenomeLength(VID_INVEST_VARS);
  POS_NET_CONN_PRICE_LEVEL_BLOCK = pos;
  pos += VectorIndividual.linearEqExpGenomeLength(NET_CONN_PRICE_LEVEL_VARS);
  POS_NET_CONN_PRICE_BALANCE_BLOCK = pos;
  pos += VectorIndividual.linearEqExpGenomeLength(NET_CONN_PRICE_BALANCE_VARS);
  POS_BUN_CONN_PRICE_LEVEL_BLOCK = pos;
  pos += VectorIndividual.linearEqExpGenomeLength(BUN_CONN_PRICE_LEVEL_VARS);
  POS_BUN_CONN_PRICE_BALANCE_BLOCK = pos;
  pos += VectorIndividual.linearEqExpGenomeLength(BUN_CONN_PRICE_BALANCE_VARS);
  POS_VID_PRICE_BLOCK = pos;
  pos += VectorIndividual.linearEqExpGenomeLength(VID_PRICE_VARS);
  genomeSize = pos;
  
}

@Override
public void step(NeutralityModel model, int step, Optional<Double> substep) {

  /*
   * Determine investment levels and make investments.
   */
  double toInvestNetwork = Double.NaN;
  double toInvestContent = Double.NaN;
  if (step == 0) {
    // For the first step, use directly encoded values.
    toInvestNetwork = getManager().e(POS_INIT_NET_INVEST);
    toInvestContent = getManager().e(POS_INIT_VID_INVEST);
  } else {
    // In all subsequent steps, use a linear equation.
    toInvestNetwork = getManager().linearEqExp(POS_NET_INVEST_BLOCK, netInvestmentVars());
    toInvestContent = getManager().linearEqExp(POS_VID_INVEST_BLOCK, vidInvestmentVars());
  }
  makeNetworkInvestment(step, toInvestNetwork);
  makeContentInvestment(step, toInvestContent);
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
    // In all subsequent steps, use a linear equation.
    priceLevel = getManager().linearEqExp(POS_NET_CONN_PRICE_LEVEL_BLOCK,
                                          netPriceLevelVars());
    priceBalance = getManager().linearEqExp(POS_NET_CONN_PRICE_BALANCE_BLOCK,
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
    // In all subsequent steps, use a linear equation.
    priceLevel = getManager().linearEqExp(POS_VID_PRICE_BLOCK,
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
    // In all subsequent steps, use a linear equation.
    priceLevel = getManager().linearEqExp(POS_BUN_CONN_PRICE_LEVEL_BLOCK,
                                          bunPriceLevelVars());
    priceBalance = getManager().linearEqExp(POS_BUN_CONN_PRICE_BALANCE_BLOCK,
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

/*
 * Same as netInvestmentVars, currently.
 */
private double[] vidInvestmentVars() {
  return netInvestmentVars();
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

private double[] netPriceBalanceVars() {
  return netPriceLevelVars();
}

private double[] vidPriceVars() {
  return netPriceLevelVars();
}

private double[] bunPriceBalanceVars() {
  return netPriceLevelVars();
}

private double[] bunPriceLevelVars() {
  return netPriceLevelVars();
}

}
