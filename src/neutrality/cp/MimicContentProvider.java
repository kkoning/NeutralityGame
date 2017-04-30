package neutrality.cp;

import java.util.Optional;

import neutrality.NeutralityModel;
import neutrality.Offers;

/**
 * MimicContentProvider makes investments in content, and prices that content,
 * to match the market average in its sector during the previous step. For the
 * first step, it relies on the genetic encoding of its parent class,
 * DirectlyEncodedContentProvider.
 * 
 * @author liara
 */
public class MimicContentProvider extends DirectlyEncodedContentProvider {

@Override
public Offers.ContentOffer getContentOffer(int step) {
  // In the first step, use genome.
  if (step == 0)
    return super.getContentOffer(step);

  // In all other steps, follow the market.
  double sectorPrice = getSectorPrice(
      getModel().getMarketInformation(step - 1));
  return new Offers.ContentOffer(step, this, sectorPrice);
}

@Override
public void step(NeutralityModel model, int step, Optional<Double> substep) {
  // In the first step, use genome.
  if (step == 0)
    super.step(model, step, substep);

  // In all other steps, follow the market.
  double toInvest = getSectorInvestment(model.getMarketInformation(step - 1));
  makeContentInvestment(step, toInvest);

}

}
