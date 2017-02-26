package neutrality;

import agency.vector.VectorIndividual;

import java.util.Optional;

public class DirectlyEncodedNetworkOperator
        extends AbstractNetworkOperator<VectorIndividual<Double>> {

public DirectlyEncodedNetworkOperator() {
  super();
}

@Override
public void step(
        NeutralityModel model, int step, Optional<Double> substep) {

  // Make investments
  makeNetworkInvestment(step,1 + e(Position.NetworkInvestment));
  makeContentInvestment(step,1 + e(Position.ContentInvestment));

  // Decide on IXC Price
  setIxcPrice(step,e(Position.InterconnectionBandwidthPrice));

}


@Override
public Offers.NetworkOnlyOffer getNetworkOffer(int step) {
  double pl = e(Position.NetStandalonePriceLevel); // price level
  double pr = e(Position.NetStandaloneConBwPriceBalance); // price balance
  double conPrice = pl * proportionA(pr);
  double bwPrice = pl * proportionB(pr);

  Offers.NetworkOnlyOffer noo =
          new Offers.NetworkOnlyOffer(step,
                                      this,
                                      conPrice,
                                      bwPrice);
  return noo;
}

@Override
public Offers.ContentOffer getContentOffer(int step) {
  Offers.ContentOffer vco =
          new Offers.ContentOffer(step,
                                  this,
                                  1 + e(Position.StandaloneContentOfferPrice));
  return vco;
}

@Override
public Offers.NetworkAndVideoBundleOffer getBundledOffer(int step) {
  double pl = e(Position.BundledOfferPriceLevel); // price level
  double pr = e(Position.BundledOfferConBwPriceBalance); // price balance
  double bunPrice = pl * proportionA(pr);
  double bwPrice = pl * proportionB(pr);

  Offers.NetworkAndVideoBundleOffer bo =
          new Offers.NetworkAndVideoBundleOffer(step,
                                                this,
                                                bunPrice,
                                                bwPrice);
  return bo;
}

@Override
public void init() {
  super.init();
}

public static final double proportionA(double split) {
  // Make calculations of bw intensity based on beta
  double videoBWIntensity;
  videoBWIntensity = split / (1.0 + split);
  return videoBWIntensity;
}

public final double proportionB(double split) {
  return 1.0d - proportionA(split);
}

private double e(Position pos) {
  VectorIndividual<Double> ind = getManager();
  Double genomeValue = ind.get(pos.ordinal());
  double toReturn = Math.exp(genomeValue);
  return toReturn;
}

public enum Position {
  NetworkInvestment,
  ContentInvestment,
  NetStandalonePriceLevel,
  NetStandaloneConBwPriceBalance,
  StandaloneContentOfferPrice,
  BundledOfferPriceLevel,
  BundledOfferConBwPriceBalance,
  InterconnectionBandwidthPrice
}

}
