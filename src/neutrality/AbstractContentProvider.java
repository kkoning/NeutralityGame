package neutrality;

import agency.Account;
import agency.Individual;
import agency.SimpleFirm;

import static agency.util.Misc.BUG;

public abstract class AbstractContentProvider<N extends Individual>
        extends SimpleFirm<N, NeutralityModel>
        implements ContentProvider<N> {

/**
 * True if this content provider is in the video market, false if it in the
 * other content market.  Set by the AgentFactory.
 */
public Boolean isVideoProvider;
// Parameters relevant to consumer value; investment and preference.

double[] Ka;

// Track the # of units sold, revenue, and $ for interconnection
double[] qtyContent;
double[] revContent;
double[] ixcPaid;

public AbstractContentProvider() {
  super();
  this.account.setBalanceRestricted(true);
}

@Override
public void init() {
  int numSteps = getModel().maxSteps;
  qtyContent = new double[numSteps];
  revContent = new double[numSteps];
  ixcPaid = new double[numSteps];
  Ka = new double[numSteps];
}

@Override
public boolean isVideo() {
  return isVideoProvider;
}

@Override
public void setVideo(boolean video) {
  this.isVideoProvider = video;
}

@Override
public double getKa(int step) {
  return Ka[step];
}

@Override
public void processContentConsumption(
        int step,
        ConsumptionOption co,
        double qty) {
  double price;
  if (isVideoProvider)
    price = co.videoContentPrice;
  else
    price = co.otherContentPrice;
  if (!co.wasVideoBundled) {
    if (price <= 0)
      BUG("Price must be > 0");
  } else { // was bundled
    // if we're video, and this was bundled, then we offered the bundle, and
    // the consumption will be tracked through network consumption; this is
    // not a separate content offer.
    if (isVideoProvider)
      return;
  }

  // Don't count content separately if it's bundled.


  double revenue = price * qty;
  account.receive(revenue);

  qtyContent[step] += qty;
  revContent[step] += revenue;
}

public void makeContentInvestment(int step, double amount) {
  if (amount <= 0)
    BUG("Content investment must be >= 0. Was: " + amount);

  try {
    account.pay(amount);
  } catch (Account.PaymentException pe) {
    this.bankrupt = true;
    return;
  }
  Ka[step] += amount;

}

@Override
public void trackIXCFees(int step, double price, double qty) {
  if (price <= 0)
    BUG("IXC price must be positive");
  if (qty <= 0)
    BUG("IXC quantity must be positive");
  double bill = price * qty;
  ixcPaid[step] += bill;
}

@Override
public double getContentData(ContentData data) {
  double amount = 0.0;
  switch(data) {
    case BALANCE:
      amount = account.getBalance();
      break;
    case INVESTMENT:
      for (double d : Ka)
        amount += d;
      break;
    case QUANTITY:
      for (double d : qtyContent)
        amount += d;
      break;
    case REVENUE:
      for (double d : revContent)
        amount += d;
      break;
    default:
      BUG("Unknown ContentData!");
  }
  return amount;
}


}
