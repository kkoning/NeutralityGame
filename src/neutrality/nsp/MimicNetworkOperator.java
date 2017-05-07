package neutrality.nsp;

import java.util.Optional;

import neutrality.MarketInfo;
import neutrality.NeutralityModel;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkAndVideoBundleOffer;
import neutrality.Offers.NetworkOnlyOffer;

/**
 * MimicNetworkOperatror makes investments in network and video content, as well
 * as prices that content, to match the market average for other NSPs.  For the
 * first step, it relies on the genetic encoding of its parent class,
 * DirectlyEncodedNetworkOperator.
 * 
 * @author liara
 *
 */
public class MimicNetworkOperator extends DirectlyEncodedNetworkOperator {

@Override
public void step(NeutralityModel model, int step, Optional<Double> substep) {
  // For the first step, use the genome.
  if (step == 0) {
    super.step(model, step, substep);
  } else {
    /*
     * For all other steps, follow the market
     */
    // Network investment
    MarketInfo mi = getModel().getMarketInformation(step - 1);
    double mktNetInvest = mi.nspNetworkInvestment;
    if (Double.isNaN(mktNetInvest) || Double.isInfinite(mktNetInvest))
      mktNetInvest = this.getKn(0);
    
    makeNetworkInvestment(step, mktNetInvest);

    // Follow the video content market, but only if vertically integrated content
    // is allowed. Otherwise it doesn't make sense to spend anything here.
    if (getModel().policyNSPContentAllowed) {
      double mktVidInvest = mi.nspVideoInvestment;
      if (Double.isNaN(mktVidInvest) || Double.isInfinite(mktVidInvest))
        mktVidInvest = this.getKa(0);
      makeContentInvestment(step, mktVidInvest);
    } else {
      makeContentInvestment(step, 0);
    }
  }

}

@Override
public NetworkOnlyOffer getNetworkOffer(int step) {
  // For the first step, use the genome.
  if (step == 0)
    return super.getNetworkOffer(step);

  // For all other steps, follow the market.
  MarketInfo mi = getModel().getMarketInformation(step - 1);
  double conPrice = mi.nspUnbundledPrice;
  double bwPrice = mi.nspBandwidthPrice;
  
  if (Double.isNaN(bwPrice) || Double.isNaN(conPrice) || Double.isInfinite(bwPrice) || Double.isInfinite(conPrice))
    return super.getNetworkOffer(step);
  
  NetworkOnlyOffer noo = new NetworkOnlyOffer(step, this, conPrice, bwPrice);
  return noo;
}

@Override
public ContentOffer getContentOffer(int step) {
  // For the first step, use the genome.
  if (step == 0)
    return super.getContentOffer(step);

  // For all other steps, follow the market.
  MarketInfo mi = getModel().getMarketInformation(step - 1);
  double price = mi.nspVideoPrice;
  if (Double.isNaN(price) || Double.isInfinite(price))
    return super.getContentOffer(step);
  else 
    return new ContentOffer(step, this, price);

}

@Override
public NetworkAndVideoBundleOffer getBundledOffer(int step) {
  // For the first step, use the genome.
  if (step == 0)
    return super.getBundledOffer(step);

  // For all other steps, follow the market.
  MarketInfo mi = getModel().getMarketInformation(step - 1);
  double bunPrice = mi.nspBundledPrice;
  double bwPrice = mi.nspBandwidthPrice;

  if (Double.isNaN(bunPrice) || Double.isInfinite(bunPrice))
    bunPrice = getModel().income;
  if (Double.isNaN(bwPrice) || Double.isInfinite(bwPrice))
    bwPrice = getModel().income;

  
  return new NetworkAndVideoBundleOffer(step, this, bunPrice, bwPrice);
}

}
