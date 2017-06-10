package neutrality;

import agency.SimpleFitness;

public class NetworkOperator extends ContentProvider {

public double Kn = Double.NaN;
public double bandwidthPrice = Double.NaN;
public double ixcPrice = Double.NaN;

public double ixcAvoided = 0d;
public double zeroRatingDiscounts = 0d;

// Track the # of different sales and total revenue of each type

public SalesTracker netOnly = new SalesTracker();
public SalesTracker bundle  = new SalesTracker();
public SalesTracker videoBW = new SalesTracker();
public SalesTracker otherBW = new SalesTracker();

public SalesTracker videoIXC = new SalesTracker();
public SalesTracker otherIXC = new SalesTracker();

/*
 * Genome Layout
 */
public enum Genome {
CONTENT_INVESTMENT,
CONTENT_PRICE,
NETWORK_INVESTMENT,
NETWORK_PRICE_LEVEL,  // Together, these two determine the connection
NETWORK_PRICE_BALANCE, // and bandwidth price.
BUNDLE_PREMIUM,
IXC_PRICE
}

public NetworkOperator() {
  this.isVideoProvider = true;
}

public void setKn() {
  double genomeValue = getManager().e(Genome.NETWORK_INVESTMENT.ordinal());
  /*
   * Penalize values below 1 here. The actual Kn is set higher to prevent a
   * negative result when we take the log, but we need to make sure we're
   * preventing genetic drift towards extremely low values that make very
   * little practical difference and might prevent us from finding the hill
   * to climb later.
   */
  if (genomeValue < 1) {
    fitnessAdjustment -= (1 / genomeValue) * 100;
  }
  Kn = 1 + genomeValue;
}

public void setNetPrices() {
  double priceLevel = getManager().e(Genome.NETWORK_PRICE_LEVEL.ordinal());
  double priceBalance = getManager().e(Genome.NETWORK_PRICE_BALANCE.ordinal());
  
  netOnly.price = NeutralityModel.proportionA(priceBalance) * priceLevel;
  bandwidthPrice = NeutralityModel.proportionB(priceBalance) * priceLevel;
  videoBW.price = bandwidthPrice * getModel().videoBWIntensity;
  otherBW.price = bandwidthPrice * getModel().otherBWIntensity;
  
  if (getModel().policy0PriceIXC) {
    ixcPrice = 0;
  } else {
    ixcPrice = getManager().e(Genome.IXC_PRICE.ordinal());
  }
  videoIXC.price = ixcPrice * getModel().videoBWIntensity;
  otherIXC.price = ixcPrice * getModel().otherBWIntensity;
  
  double bundlePremium = getManager().e(Genome.BUNDLE_PREMIUM.ordinal());
  bundle.price = netOnly.price + bundlePremium;
}

@Override
public double getBalance() {
  double balance = 0;

  // Sum of revenue
  balance += netOnly.revenue();
  balance += bundle.revenue();
  balance += videoBW.revenue();
  balance += otherBW.revenue();
  balance += videoIXC.revenue();
  balance += otherIXC.revenue();

  // Costs for investment and operation
  balance -= Kn;
  balance -= Ka;
  balance -= getModel().nspMarginalCost * netOnly.qty;
  balance -= getModel().nspMarginalCost * bundle.qty;
  return balance;
}

@Override
public SimpleFitness getFitness() {
  return new SimpleFitness(getBalance() + fitnessAdjustment);
}

}
