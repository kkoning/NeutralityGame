package neutrality;

import agency.vector.VectorIndividual;

/**
 * Created by kkoning on 9/23/16.
 */
public class DirectlyEncodedContentProvider extends ContentProvider<VectorIndividual> {

boolean firstStep = true;

public DirectlyEncodedContentProvider() {
  super();
}

@Override
public Offers.ContentOffer getContentOffer() {
  Offers.ContentOffer offer = new Offers.ContentOffer(this, e(Position.ContentOfferPrice));
  return offer;
}

private double e(Position pos) {
  VectorIndividual<Double> ind = getManager();
  Double genomeValue = ind.get(pos.ordinal());
  double toReturn = Math.exp(genomeValue);
  return toReturn;
}

@Override
public void step() {
  if (firstStep) {
    makeContentInvestment(e(Position.ContentInvestment));
    firstStep = false;
  }
}

private enum Position {
  ContentInvestment,
  ContentOfferPrice
}


}
