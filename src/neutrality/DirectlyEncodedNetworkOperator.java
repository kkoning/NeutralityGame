package neutrality;

import agency.vector.VectorIndividual;
import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

public class DirectlyEncodedNetworkOperator
        extends NetworkOperator<VectorIndividual<Double>> {

boolean firstStep = true;

public DirectlyEncodedNetworkOperator() {
  this.icp = new PuppetContentProvider(this.account);
}

@Override
void step() {
  super.step();
    /*
     * If this is the first step, make our investments.
		 */
  if (firstStep) {
    makeNetworkInvestment(e(Position.NetworkInvestment));
    makeContentInvestment(e(Position.ContentInvestment));
    // Also, housekeeping for content provider
    icp.setModel(this.getModel());
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
  double pl = e(Position.NetStandalonePriceLevel); // price level
  double pr = e(Position.NetStandaloneConBwPriceBalance); // price balance
  double conPrice = pl * proportionA(pr);
  double bwPrice = pl * proportionB(pr);

  NetworkOffer no = new NetworkOffer(this, conPrice, bwPrice);
  return no;
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

@Override
public ContentOffer getVideoContentOffer() {
  ContentOffer co = new ContentOffer(icp,
                                     e(Position.StandaloneContentOfferPrice));
  return co;
}

@Override
public BundledOffer getBundledOffer() {
  double pl = e(Position.BundledOfferPriceLevel); // price level
  double pr = e(Position.BundledOfferConBwPriceBalance); // price balance
  double bunPrice = pl * proportionA(pr);
  double bwPrice = pl * proportionB(pr);

  BundledOffer bo = new BundledOffer(this,
                                     icp,
                                     bunPrice,
                                     bwPrice,
                                     false);
  return bo;
}

@Override
public BundledOffer getBundledZeroRatedOffer() {
  double pl = e(Position.BundledZeroRatedPriceLevel); // price level
  double pr = e(Position.BundledZeroRatedConBwPriceBalance); // price balance
  double bunPrice = pl * proportionA(pr);
  double bwPrice = pl * proportionB(pr);


  BundledOffer bo = new BundledOffer(this,
                                     icp,
                                     bunPrice,
                                     bwPrice,
                                     true);
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
  NetStandalonePriceLevel,
  NetStandaloneConBwPriceBalance,
  StandaloneContentOfferPrice,
  BundledOfferPriceLevel,
  BundledOfferConBwPriceBalance,
  BundledZeroRatedPriceLevel,
  BundledZeroRatedConBwPriceBalance,
  InterconnectionBandwidthPrice
}

}
