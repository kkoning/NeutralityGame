package neutrality.nsp;

import static agency.util.Misc.BUG;

import agency.Account;
import agency.Individual;
import neutrality.ConsumptionOption;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkAndVideoBundleOffer;
import neutrality.Offers.NetworkOnlyOffer;
import neutrality.cp.AbstractContentProvider;

public abstract class AbstractNetworkOperator<N extends Individual>
    extends AbstractContentProvider<N>
    implements NetworkOperator<N> {

public double[] Kn;

// Track the # of different sales and total revenue of each type
public double[] qtyNetwork;
public double[] revNetwork;

public double[] qtyBundle;
public double[] revBundle;

public double[] qtyBandwidthVideo;
public double[] revBandwidthVideo;

public double[] qtyBandwidthOther;
public double[] revBandwidthOther;

public double[] qtyIxcVideo;
public double[] revIxcVideo;

public double[] qtyIxcOther;
public double[] revIxcOther;

public double[] ixcPrice;

public AbstractNetworkOperator() {
  this.isVideoProvider = true;
}

@Override
public void init() {
  super.init();

  int steps = getModel().maxSteps;

  // Capital investment
  Kn = new double[steps];

  // The # of different purchases of each type
  qtyNetwork = new double[steps];
  revNetwork = new double[steps];

  qtyBundle = new double[steps];
  revBundle = new double[steps];

  qtyBandwidthVideo = new double[steps];
  revBandwidthVideo = new double[steps];

  qtyBandwidthOther = new double[steps];
  revBandwidthOther = new double[steps];

  qtyIxcVideo = new double[steps];
  revIxcVideo = new double[steps];

  qtyIxcOther = new double[steps];
  revIxcOther = new double[steps];

  ixcPrice = new double[steps];
}

public void makeNetworkInvestment(int step, double amount) {

  /*
   * Since the investments are raised to powers <1, investment amounts less than
   * 1 have paradoxical effects on the desirability vs cost ratio; in other
   * words, the slope of the desirability/cost line is greater than 1, which can
   * create an edge condition local maximum. To prevent this, the smallest
   * investment amount allowed is 1.
   */
  if (amount < 1)
    amount = 1;

  try {
    account.pay(amount);
  } catch (Account.PaymentException e) {
    bankrupt = true;
    return;
  }
  Kn[step] += amount;
}

public void setIxcPrice(int step, double price) {
  ixcPrice[step] = price;
}

@Override
public double getKn(int step) {
  return Kn[step];
}

@Override
public void processNetworkConsumption(
    int step,
    ConsumptionOption co,
    double qty) {
  // Connections / bundles
  if (co.wasVideoBundled) {
    double bunRev = co.bundledPrice * qty;
    qtyBundle[step] += qty;
    revBundle[step] += bunRev;
    account.receive(bunRev);
  } else {
    double netRev = co.networkOnlyPrice * qty;
    qtyNetwork[step] += qty;
    revNetwork[step] += netRev;
    account.receive(netRev);
  }

  // Bandwidth from content consumption
  // Content fees, if any, are processed in processVideoContentConsumption()
  if (co.videoContent.isPresent()) {
    double rev = co.videoBWPrice * qty;
    qtyBandwidthVideo[step] += getModel().videoBWIntensity * qty;
    revBandwidthVideo[step] += rev;
    account.receive(rev);
  }
  if (co.otherContent.isPresent()) {
    double rev = co.otherBWPrice * qty;
    qtyBandwidthOther[step] += getModel().otherBWIntensity * qty;
    revBandwidthOther[step] += rev;
    account.receive(rev);
  }

  // Needs marginal costs, to address CES utility function issue; otherwise
  // profit maximization will have p -> zero and q-> infinity.
  // TODO: This is just qty, MC=1 right now. Think about this again later.
  try {
    account.pay(qty * getModel().nspMarginalCost);
  } catch (Account.PaymentException e) {
    this.goBankrupt();
  }

}

@Override
public double getIXCPrice(int step) {
  return ixcPrice[step];
}

@Override
public void trackIXC(int step, double price, double qty, boolean video) {
  double rev = price * qty;
  if (rev > 1E10)
    throw new RuntimeException();
  if (video) {
    qtyIxcVideo[step] += qty;
    revIxcVideo[step] += rev;
  } else {
    qtyIxcOther[step] += qty;
    revIxcOther[step] += rev;
  }
  // Actual amounts are transferred by ConsumptionOption.consume
}

public static double proportionA(double split) {
  // Make calculations of bw intensity based on beta
  double videoBWIntensity;
  videoBWIntensity = split / (1.0 + split);
  return videoBWIntensity;
}

public static double proportionB(double split) {
  return 1.0d - AbstractNetworkOperator.proportionA(split);
}

@Override
public double totQtyBundle() {
  double toReturn = 0;
  for (int i = 0; i < qtyBundle.length; i++)
    toReturn += qtyBundle[i];
  return toReturn;
}

@Override
public double totQtyNetworkOnly() {
  double toReturn = 0;
  for (int i = 0; i < qtyNetwork.length; i++)
    toReturn += qtyNetwork[i];
  return toReturn;
}

@Override
public double totRevNetworkOnly() {
  double toReturn = 0;
  for (int i = 0; i < revNetwork.length; i++)
    toReturn += revNetwork[i];
  return toReturn;
}

@Override
public double totRevBundle() {
  double toReturn = 0;
  for (int i = 0; i < revBundle.length; i++)
    toReturn += revBundle[i];
  return toReturn;
}

@Override
public double totQtyIxcVideo() {
  double toReturn = 0;
  for (int i = 0; i < qtyIxcVideo.length; i++)
    toReturn += qtyIxcVideo[i];
  return toReturn;
}

@Override
public double totRevIxcVideo() {
  double toReturn = 0;
  for (int i = 0; i < revIxcVideo.length; i++)
    toReturn += revIxcVideo[i];
  return toReturn;
}

@Override
public double totQtyIxcOther() {
  double toReturn = 0;
  for (int i = 0; i < qtyIxcOther.length; i++)
    toReturn += qtyIxcOther[i];
  return toReturn;
}

@Override
public double totRevIxcOther() {
  double toReturn = 0;
  for (int i = 0; i < revIxcOther.length; i++)
    toReturn += revIxcOther[i];
  return toReturn;
}

@Override
public double totKn() {
  double toReturn = 0;
  for (int i = 0; i < Kn.length; i++)
    toReturn += Kn[i];
  return toReturn;
}

@Override
public double totRevVideoBW() {
  double toReturn = 0;
  for (int i = 0; i < revBandwidthVideo.length; i++)
    toReturn += revBandwidthVideo[i];
  return toReturn;
}

@Override
public double totRevOtherBW() {
  double toReturn = 0;
  for (int i = 0; i < revBandwidthOther.length; i++)
    toReturn += revBandwidthOther[i];
  return toReturn;
}




}
