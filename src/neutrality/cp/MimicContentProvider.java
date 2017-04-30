package neutrality.cp;

import java.util.Optional;

import neutrality.NeutralityModel;
import neutrality.Offers;

public class MimicContentProvider extends DirectlyEncodedContentProvider {
@Override
public Offers.ContentOffer getContentOffer(int step) {
  if (step == 0)
    return super.getContentOffer(step);

  double sectorPrice = getSectorPrice(getModel().getMarketInformation(step-1));
  return new Offers.ContentOffer(step,
          this,
          sectorPrice);
}

@Override
public void step(NeutralityModel model, int step, Optional<Double> substep) {
  if (step == 0)
    super.step(model, step, substep);

  double toInvest = getSectorInvestment(model.getMarketInformation(step-1));
  makeContentInvestment(step, toInvest);


}

}
