package neutrality;

import agency.Account;
import agency.Individual;

import static agency.util.Misc.BUG;

public abstract class AbstractNetworkOperator<N extends Individual>
    extends AbstractContentProvider<N>
    implements NetworkOperator<N> {

double[] Kn;

// Track the # of different sales and total revenue of each type
double[] qtyNetwork;
double[] revNetwork;

double[] qtyBundle;
double[] revBundle;

double[] qtyBandwidthVideo;
double[] revBandwidthVideo;

double[] qtyBandwidthOther;
double[] revBandwidthOther;

double[] qtyIxcVideo;
double[] revIxcVideo;

double[] qtyIxcOther;
double[] revIxcOther;

double[] ixcPrice;

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
    account.pay(qty);
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
  if (video) {
    qtyIxcVideo[step] += qty;
    revIxcVideo[step] += rev;
  } else {
    qtyIxcOther[step] += qty;
    revIxcOther[step] += rev;
  }
  // Actual amounts are transferred by ConsumptionOption.consume
}

@Override
public double getNetOpData(NetOpData variable) {
  double amount = 0.0d;
  switch (variable) {
    case QUANTITY_NETWORK:
      for (double d : qtyNetwork) {
        amount += d;
      }
      break;
    case REVENUE_NETWORK:
      for (double d : revNetwork) {
        amount += d;
      }
      break;
    case QUANTITY_BUNDLE:
      for (double d : qtyBundle) {
        amount += d;
      }
      break;
    case REVENUE_BUNDLE:
      for (double d : revBundle) {
        amount += d;
      }
      break;
    case QUANTITY_IXC_VIDEO:
      for (double d : qtyIxcVideo) {
        amount += d;
      }
      break;
    case REVENUE_IXC_VIDEO:
      for (double d : revIxcVideo) {
        amount += d;
      }
      break;
    case QUANTITY_IXC_OTHER:
      for (double d : qtyIxcOther) {
        amount += d;
      }
      break;
    case REVENUE_IXC_OTHER:
      for (double d : revIxcOther) {
        amount += d;
      }
      break;
    case INVESTMENT_NETWORK:
      for (double d : Kn) {
        amount += d;
      }
      break;
    default:
      BUG("Unimplemented NetOpData");
  }

  return amount;
}

}
