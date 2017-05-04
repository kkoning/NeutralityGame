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
    makeNetworkInvestment(step, mktNetInvest);

    // Follow the video content market, but only if vertically integrated content
    // is allowed. Otherwise it doesn't make sense to spend anything here.
    if (getModel().policyNSPContentAllowed) {
      double mktVidInvest = mi.nspVideoInvestment;
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
  return new ContentOffer(step, this, mi.nspVideoPrice);

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
  return new NetworkAndVideoBundleOffer(step, this, bunPrice, bwPrice);
}

}
