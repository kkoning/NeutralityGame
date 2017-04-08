package neutrality;

import agency.vector.VectorIndividual;

import java.util.Optional;

/**
 * Created by kkoning on 9/23/16.
 */
public class DirectlyEncodedContentProvider
        extends AbstractContentProvider<VectorIndividual<Double>> {

public DirectlyEncodedContentProvider() {
  super();
}

@Override
public Offers.ContentOffer getContentOffer(int step) {
  return new Offers.ContentOffer(step,
                                 this,
                                 e(Position.ContentOfferPrice));
}

@Override
public void step(
        NeutralityModel model, int step, Optional<Double> substep) {
  makeContentInvestment(step, 1 + e(Position.ContentInvestment));
}

private double e(Position pos) {
  VectorIndividual<Double> ind = getManager();
  Double genomeValue = ind.get(pos.ordinal());
  double toReturn = Math.exp(genomeValue);
  return toReturn;
}

public enum Position {
  ContentInvestment,
  ContentOfferPrice
}


}
