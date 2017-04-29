package neutrality;

import agency.Account;
import neutrality.Offers.NetworkAndVideoBundleOffer;
import neutrality.Offers.NetworkOnlyOffer;

import java.util.Optional;

import static agency.util.Misc.BUG;


/**
 * This
 *
 * @author kkoning
 */
public class ConsumptionOption {

public final double  K_n;
public final double  K_v;
public final double  K_o;
public final double  networkOnlyPrice;
public final double  bundledPrice;
public final double  bandwidthPrice;
public final double  videoContentPrice;
public final double  otherContentPrice;
public final double  videoBWPrice;
public final double  otherBWPrice;
public final double  totalCostToConsumer;
public final double  ixcBWPrice;
public final boolean wasVideoBundled;
//public final boolean wasZeroRated;

public final double utilityCoefficient;

public final NetworkOperator           network;
public final Optional<ContentProvider> videoContent;
public final Optional<ContentProvider> otherContent;

NeutralityModel model;


ConsumptionOption(
        NeutralityModel model,
        NetworkOnlyOffer no,
        NetworkAndVideoBundleOffer bo,
        Offers.ContentOffer vco,
        Offers.ContentOffer oco) {

  this.model = model;

  Optional<NetworkOnlyOffer> netOffer = Optional.ofNullable(no);
  Optional<NetworkAndVideoBundleOffer> bundledOffer = Optional.ofNullable(bo);
  Optional<Offers.ContentOffer> videoOffer = Optional.ofNullable(vco);
  Optional<Offers.ContentOffer> otherOffer = Optional.ofNullable(oco);

  /*
  Perform some sanity checks during development and debugging.  These could
  be removed for a (very minor) performance boost.
   */
  if (netOffer.isPresent() && bundledOffer.isPresent())
    BUG("ConsumptionOption cannot have both a standalone and bundled " +
        "network offer.");

  if (!netOffer.isPresent() && !bundledOffer.isPresent())
    BUG("ConsumptionOption must have either a standalone or bundled " +
        "network offer.");

  if (bundledOffer.isPresent() && videoOffer.isPresent())
    BUG("ConsumptionOption cannot have both a network/video bundled offer " +
        "and a separate video content offer.");

  if (netOffer.isPresent()) {
    network = netOffer.get().network;
    K_n = netOffer.get().network.getKn(model.currentStep);
    networkOnlyPrice = netOffer.get().connectionPrice;
    bundledPrice = 0.0;
    bandwidthPrice = netOffer.get().bandwidthPrice;
    wasVideoBundled = false;
//    wasZeroRated = false;

    if (videoOffer.isPresent()) {
      videoContent = Optional.of(videoOffer.get().contentProvider);
      K_v = videoContent.get().getKa(model.currentStep);
      videoContentPrice = videoOffer.get().price;
    } else {
      videoContent = Optional.empty();
      K_v = 0.0d;
      videoContentPrice = 0.0d;
    }


  } else if (bundledOffer.isPresent()) {
    network = bundledOffer.get().networkOperator;
    videoContent = Optional.of(bundledOffer.get().networkOperator);
    K_n = network.getKn(model.currentStep);
    K_v = videoContent.get().getKa(model.currentStep);
    wasVideoBundled = true;

    bundledPrice = bundledOffer.get().bundlePrice;
    networkOnlyPrice = 0.0;
    videoContentPrice = 0.0d;

    bandwidthPrice = bundledOffer.get().bandwidthPrice;
//    wasZeroRated = model.policyZeroRated;
  } else {
    throw new RuntimeException("BUG: ConsumptionOption must have either a " +
                               "standalone or bundled network offer.");
  }

  if (otherOffer.isPresent()) {
    otherContent = Optional.of(otherOffer.get().contentProvider);
    K_o = otherContent.get().getKa(model.currentStep);
    otherContentPrice = otherOffer.get().price;
  } else {
    otherContent = Optional.empty();
    K_o = 0.0;
    otherContentPrice = 0.0;
  }

  // Calculate cost of bandwidth usage
  if (videoContent.isPresent()) {
    // If zero rating is enabled, and offered by the same firm as the
    // network operator, (either bundled or separately) then there is no cost.
    if (model.policyZeroRated) {
      if (videoContent.get() == network) {
        // Zero rating is on, and the video and network providers are the
        // same agent/firm.
        videoBWPrice = 0.0;
      } else {
        // Zero rating is on, but the video is offered by a different firm.
        videoBWPrice = model.videoBWIntensity * bandwidthPrice;
      }
    } else {
      // Video is present, and zero rating is turned off.
      videoBWPrice = model.videoBWIntensity * bandwidthPrice;
    }
  } else {
    videoBWPrice = 0.0;
  }

  if (otherContent.isPresent()) {
    otherBWPrice = model.otherBWIntensity * bandwidthPrice;
  } else {
    otherBWPrice = 0.0d;
  }

  // Total cost to consumer is sum of all costs; everything that isn't being
  // used has been set to a price of zero.
  totalCostToConsumer = networkOnlyPrice +
                        bundledPrice +
                        videoContentPrice +
                        otherContentPrice +
                        videoBWPrice +
                        otherBWPrice;

  // Determine utility
  double contentCapitalTerm = 0.0;
  double psi = network.getModel().psi;
  if (videoContent.isPresent()) {
    double videoValue = network.getModel().videoContentValue;
    double videoCapitalTerm = Math.pow(K_v, psi);
    contentCapitalTerm += videoValue * videoCapitalTerm;
  }
  if (otherContent.isPresent()) {
    double otherValue = network.getModel().otherContentValue;
    double otherCapitalTerm = Math.pow(K_o, psi);
    contentCapitalTerm += otherValue * otherCapitalTerm;
  }
  double netCapitalTerm = Math.pow(K_n, psi);
  utilityCoefficient = contentCapitalTerm * netCapitalTerm;

  this.ixcBWPrice = network.getIXCPrice(model.currentStep);
}

public double getUtility(double qty) {
  double gamma = network.getModel().gamma;
  return utilityCoefficient * Math.pow(qty, gamma);
}

public void consume(double qty) {
  // Consumer utility is recorded directly by Consumers.consume()

  // Network or bundle consumption
  network.processNetworkConsumption(model.currentStep, this, qty);

  // Unbundled video content, if present
  if (videoContent.isPresent()) {
    videoContent.get().processContentConsumption(model.currentStep, this, qty);
    if (!model.policy0PriceIXC) {
      payIXCFees(videoContent.get(),
                 network,
                 ixcBWPrice,
                 model.videoBWIntensity,
                 qty);

      network.trackIXC(model.currentStep,
                       ixcBWPrice,
                       model.videoBWIntensity * qty,
                       true);
    }
  }

  // Unbundled other content, if present
  if (otherContent.isPresent()) {
    otherContent.get().processContentConsumption(model.currentStep, this, qty);
    if (!model.policy0PriceIXC) {
      payIXCFees(otherContent.get(),
                 network,
                 ixcBWPrice,
                 model.otherBWIntensity,
                 qty);

      network.trackIXC(model.currentStep,
                       ixcBWPrice,
                       model.videoBWIntensity * qty,
                       false);
    }
  }
}

static void payIXCFees(
        ContentProvider cp,
        NetworkOperator no,
        double ixcBWPrice,
        double bwIntensity,
        double qty) {
  Account cpAccount = cp.getAccount();
  Account noAccount = no.getAccount();

  double bill = ixcBWPrice * bwIntensity * qty;
  try {
    cpAccount.payTo(noAccount, bill);
  } catch (Account.PaymentException e) {
    cp.goBankrupt();
    return;
  }
}

@Override
public String toString() {
  StringBuffer sb = new StringBuffer();
  sb.append("Kn=" + K_n);
  sb.append(",Kv=" + K_v);
  sb.append(",Ko=" + K_o);
  sb.append(",p=" + totalCostToConsumer);
  return sb.toString();
}

}
