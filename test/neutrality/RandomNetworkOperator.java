package neutrality;

import agency.NullIndividual;
import neutrality.nsp.AbstractNetworkOperator;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomNetworkOperator
        extends AbstractNetworkOperator<NullIndividual> {

double mean, sd;

public RandomNetworkOperator(double mean, double sd) {
  this.mean = mean;
  this.sd = sd;
  this.setManager(new NullIndividual());
}

@Override
public Offers.NetworkOnlyOffer getNetworkOffer(int step) {
  return new Offers.NetworkOnlyOffer(
          step, this, r(), r());
}

@Override
public Offers.NetworkAndVideoBundleOffer getBundledOffer(int step) {
  return new Offers.NetworkAndVideoBundleOffer(
          step, this, r(), r());
}

@Override
public Offers.ContentOffer getContentOffer(int step) {
  return new Offers.ContentOffer(step, this, r());
}

@Override
public void step(
        NeutralityModel model, int step, Optional<Double> substep) {
  makeNetworkInvestment(step, r());
  makeContentInvestment(step, r());
  setIxcPrice(step, r());
}

private double r() {
  Random r = ThreadLocalRandom.current();
  double var = r.nextGaussian() * sd;
  return mean + var;
}

}
