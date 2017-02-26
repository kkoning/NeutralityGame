package neutrality;

import agency.NullIndividual;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomContentProvider
        extends AbstractContentProvider<NullIndividual> {

double mean, sd;

public RandomContentProvider(double mean, double sd) {
  this.mean = mean;
  this.sd = sd;
  this.setManager(new NullIndividual());
}

@Override
public void step(
        NeutralityModel model, int step, Optional<Double> substep) {
  makeContentInvestment(step,r());
}

@Override
public Offers.ContentOffer getContentOffer(int step) {
  return new Offers.ContentOffer(step, this, r());
}

private double r() {
  Random r = ThreadLocalRandom.current();
  double var = r.nextGaussian() * sd;
  return mean + var;
}


}
