package neutrality;

import java.util.ArrayList;
import java.util.List;

import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

/**
 * This
 *
 * @author kkoning
 */
public class ConsumptionOption {

/**
 * If sanity_checks is true, additional checks will be performed.
 */
static final boolean sanity_checks = true;

// The completed package considered by consumers
NetworkOperator<?> network;
ContentProvider<?> videoContent;
ContentProvider<?> otherContent;
double             cost;

// The offers, for tracking purposes
// may be null
NetworkOffer netOffer;
BundledOffer bundledOffer;
ContentOffer videoOffer;
ContentOffer otherOffer;

/**
 * Put together a completed option with unbundled components.
 *
 * @param netOffer
 * @param videoContentOffer
 * @param otherContentOffer
 */
ConsumptionOption(
        NeutralityModel model,
        NetworkOffer netOffer,
        ContentOffer videoContentOffer,
        ContentOffer otherContentOffer) {

		/*
     * There are three possible combinations here. (1) Network access and
		 * video content, (2) Network access and other content, and (3) Network
		 * access and both video and other content. Whether or not a component
		 * is included depends on whether a parameter is null.
		 */
  if (sanity_checks) {
    // Must have reference to a model
    if (model == null)
      throw new RuntimeException("ConsumptionOption missing reference to model");

    // Must have a network
    if (netOffer == null)
      throw new RuntimeException("Cannot create a ConsumptionOption w/o a network");

    // Must have either video or other content offer, i.e., these
    // parameters must not both be null.
    if (videoContentOffer == null)
      if (otherContentOffer == null)
        throw new RuntimeException("ConsumptionOption must include _some_ content");
  }

  /*
   * We have now guaranteed that the combination of consumption is valid.
   * The main tasks to be accomplished now is allowing consumers to
	 * determine the utility and preparing to track data if the consumption
	 * offer is chosen.
	 */

  // The cost will always include network access
  this.netOffer = netOffer;
  this.network = netOffer.network;
  this.cost += netOffer.connectionPrice;

  // If a content option is included, the total cost will also include the
  // subscription fee for that content, plus associated bandwidth.
  // First, process the video content offer.
  if (videoContentOffer != null) {
    this.videoContent = videoContentOffer.content;
    this.cost += videoContentOffer.contentPrice;
    this.cost += netOffer.bandwidthPrice * NeutralityModel.videoBWIntensity(model.beta);
    this.videoOffer = videoContentOffer;

  }
  // Then process the other content offer.
  if (otherContentOffer != null) {
    this.otherContent = otherContentOffer.content;
    this.cost += otherContentOffer.contentPrice;
    this.cost += netOffer.bandwidthPrice * NeutralityModel.otherBWIntensity(model.beta);
    this.otherOffer = otherContentOffer;
  }
}

ConsumptionOption(
        NeutralityModel model,
        BundledOffer bundledOffer,
        ContentOffer otherContentOffer) {

  if (sanity_checks) {
    // Must have reference to a model
    if (model == null)
      throw new RuntimeException("ConsumptionOption missing reference to model");

    // Bundled offer must have been passed
    if (bundledOffer == null)
      throw new RuntimeException(
              "Cannot create consumption option with null bundled offer");
  }

  // All bundled offers contain network access and video content.
  this.bundledOffer = bundledOffer;
  this.network = bundledOffer.network;
  this.videoContent = bundledOffer.videoContent;

  // Always pay the content + network price for the bundle
  this.cost += bundledOffer.bundlePrice;
  // if the bundled video content is not also zero rated, pay bandwidth
  // costs for it.
  if (!bundledOffer.contentZeroRated)
    this.cost += bundledOffer.bandwidthPrice * model.videoBWIntensity();

  // Bundled offers (net+video) may or may not be combined with other
  // content
  if (otherContentOffer != null) {
    this.cost += otherContentOffer.contentPrice;
    this.cost += bundledOffer.bandwidthPrice * model.otherBWIntensity();
    this.otherOffer = otherContentOffer;
    this.otherContent = otherContentOffer.content;
  }
}

public double networkInvestment() {
  return network.getInvestment();
}

public boolean hasVideo() {
  return (videoContent != null);
}

public double videoInvestment() {
  return videoContent.getInvestment();
}

public double videoPreference() {
  return videoContent.getPreference();
}


public boolean hasOther() {
  return (otherContent != null);
}


public double otherInvestment() {
  return otherContent.getInvestment();
}

public double otherPreference() {
  return otherContent.getPreference();
}

public double getCost() {
  return cost;
}

/**
 * Pay each firm and execute any side-effects of this consumption (currently
 * none, as consumer surplus is added by Consumers.procurementProcess, which
 * should be the primary called of this function).
 */
public void payProviders() {
  payProviders(1);
}

/**
 * Pay each firm and execute any side-effects of this consumption (currently
 * none, as consumer surplus is added by Consumers.procurementProcess, which
 * should be the primary called of this function).
 */
public void payProviders(double qty) {
  // Network and video can come from either bundled or standalone offers
  if (wasBundled()) {  // Video included in bundle
    network.processAcceptedBundledOffer(
            qty,
            bundledOffer,
            (otherContent != null));
  } else {
    network.processAcceptedNetworkOffer(
            qty,
            netOffer,
            (videoContent != null),
            (otherContent != null));

    // Video processed separately, if chosen.
    if (videoContent != null)
      videoContent.processAcceptedContentOffer(qty, videoOffer, network);
  }

  // Other content always comes from the stand alone offer
  if (otherContent != null)
    otherContent.processAcceptedContentOffer(qty, otherOffer, network);
}

/**
 * @return true if the option was created from a bundled offer of network
 * access and content.
 */
public boolean wasBundled() {
  // Should match this condition.
  return (bundledOffer != null);
}

@Override
public String toString() {
  StringBuffer sb = new StringBuffer();
  sb.append("K_n=" + network.getInvestment() + ",");
  if (videoContent != null) {
    sb.append("K_{a,vid}=" + videoContent.getInvestment() + ",");
    sb.append("Pref_{a,vid}=" + videoContent.preference + ",");
  }
  if (otherContent != null) {
    sb.append("K_{a,oth}=" + otherContent.getInvestment() + ",");
    sb.append("Pref_{a,oth}=" + otherContent.preference + ",");
  }
  sb.append("p=" + cost);

  return sb.toString();
}

}
