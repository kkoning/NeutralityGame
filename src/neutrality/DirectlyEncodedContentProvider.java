package neutrality;

import agency.vector.VectorIndividual;

/**
 * Created by kkoning on 9/23/16.
 */
public class DirectlyEncodedContentProvider extends ContentProvider<VectorIndividual> {

    private enum Position {
        ContentInvestment,
        ContentOfferPrice
    }

    boolean firstStep = true;

    private double e(Position pos) {
        VectorIndividual<Double> ind = getManager();
        Double genomeValue = ind.get(pos.ordinal());
        double toReturn = Math.exp(genomeValue);
        return toReturn;
    }

    public DirectlyEncodedContentProvider() {
        super();
    }

    @Override
    public Offers.ContentOffer getContentOffer() {
        Offers.ContentOffer offer = new Offers.ContentOffer(this, e(Position.ContentOfferPrice));
        return offer;
    }

    @Override
    public void step() {
        if (firstStep) {
            makeContentInvestment(e(Position.ContentInvestment));
            firstStep = false;
        }

    }


}
