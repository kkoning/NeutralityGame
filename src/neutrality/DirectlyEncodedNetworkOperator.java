package neutrality;

import agency.vector.VectorIndividual;
import agency.vector.VectorIndividual;
import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

public class DirectlyEncodedNetworkOperator extends NetworkOperator<VectorIndividual<Double>> {

boolean firstStep = true;

public DirectlyEncodedNetworkOperator() {
  this.integratedContentProvider = new PuppetContentProvider();
}

@Override
void step() {
    /*
		 * If this is the first step, make our investments.
		 */
  if (firstStep) {
    makeNetworkInvestment(e(Position.NetworkInvestment));
    makeContentInvestment(e(Position.ContentInvestment));
    // Also, housekeeping for content provider
    integratedContentProvider.setModel(this.getModel());
    firstStep = false;
  }
}

private double e(Position pos) {
  VectorIndividual<Double> ind = getManager();
  Double genomeValue = ind.get(pos.ordinal());
  double toReturn = Math.exp(genomeValue);
  return toReturn;
}

@Override
public NetworkOffer getNetworkOffer() {
  NetworkOffer no = new NetworkOffer(this, e(Position.NetOfferConnectionPrice),
          e(Position.NetOfferBandwidthPrice));
  return no;
}

@Override
public ContentOffer getVideoContentOffer() {
  ContentOffer co = new ContentOffer(integratedContentProvider,
          e(Position.ContentOfferPrice));
  return co;
}

@Override
public BundledOffer getBundledOffer() {
  BundledOffer bo = new BundledOffer(this, integratedContentProvider,
          e(Position.BundledOfferPrice), e(Position.BundledBandwidthPrice), false);
  return bo;
}

@Override
public BundledOffer getBundledZeroRatedOffer() {
  BundledOffer bo = new BundledOffer(this, integratedContentProvider,
                                            e(Position.BundledZeroRatedOfferPrice), e(Position.BundledZeroRatedBandwidthPrice), false);
  bo.contentZeroRated = true;
  return bo;
}

@Override
public double getInterconnectionBandwidthPrice() {
  return e(Position.InterconnectionBandwidthPrice);
}

private enum Position {
  NetworkInvestment,
  ContentInvestment,
  NetOfferConnectionPrice,
  NetOfferBandwidthPrice,
  ContentOfferPrice,
  BundledOfferPrice,
  BundledBandwidthPrice,
  BundledZeroRatedOfferPrice,
  BundledZeroRatedBandwidthPrice,
  InterconnectionBandwidthPrice
}

}
