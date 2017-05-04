package neutrality.nsp;

import java.util.Optional;

import agency.vector.VectorIndividual;
import neutrality.NeutralityModel;
import neutrality.Offers;

public class DirectlyEncodedNetworkOperator
    extends AbstractNetworkOperator<VectorIndividual<Double>> {

public DirectlyEncodedNetworkOperator() {
  super();
}

@Override
public void step(
    NeutralityModel model, int step, Optional<Double> substep) {

  // Make investments
  makeNetworkInvestment(step, 1 + e(Position.NetworkInvestment));
  makeContentInvestment(step, 1 + e(Position.ContentInvestment));

  // Decide on IXC Price
  setIxcPrice(step, e(Position.InterconnectionBandwidthPrice));

}

@Override
public Offers.NetworkOnlyOffer getNetworkOffer(int step) {
  double pl = e(Position.NetStandalonePriceLevel); // price level
  double pr = e(Position.NetStandaloneConBwPriceBalance); // price balance
  double conPrice = pl * AbstractNetworkOperator.proportionA(pr);
  double bwPrice = pl * AbstractNetworkOperator.proportionB(pr);

  Offers.NetworkOnlyOffer noo = new Offers.NetworkOnlyOffer(step,
      this,
      conPrice,
      bwPrice);
  return noo;
}

@Override
public Offers.ContentOffer getContentOffer(int step) {
  Offers.ContentOffer vco = new Offers.ContentOffer(step,
      this,
      e(Position.StandaloneContentOfferPrice));
  return vco;
}

@Override
public Offers.NetworkAndVideoBundleOffer getBundledOffer(int step) {
  double pl = e(Position.BundledOfferPriceLevel); // price level
  double pr = e(Position.BundledOfferConBwPriceBalance); // price balance
  double bunPrice = pl * AbstractNetworkOperator.proportionA(pr);
  double bwPrice = pl * AbstractNetworkOperator.proportionB(pr);

  Offers.NetworkAndVideoBundleOffer bo = new Offers.NetworkAndVideoBundleOffer(
      step,
      this,
      bunPrice,
      bwPrice);
  return bo;
}

@Override
public void init() {
  super.init();
}

private double e(Position pos) {
  VectorIndividual<Double> ind = getManager();
  Double genomeValue = ind.gene(pos.ordinal());
  double toReturn = Math.exp(genomeValue);
  return toReturn;
}

public enum Position {
  NetworkInvestment, ContentInvestment, NetStandalonePriceLevel, NetStandaloneConBwPriceBalance, StandaloneContentOfferPrice, BundledOfferPriceLevel, BundledOfferConBwPriceBalance, InterconnectionBandwidthPrice
}

}
