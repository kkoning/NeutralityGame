package neutrality;

public class Bundle extends ConsumptionOption {

public Bundle(NetworkOperator network,
              ContentProvider other,
              boolean zeroRated) {
  this.network = network;
  this.video = network;
  this.other = other;
  this.zeroRated = zeroRated;
}

@Override
public double getTotalCost() {
  double bandwidthPrice = 0d;

  if (zeroRated) {
    // If the video is zero rated, consumers only need to pay for bandwidth
    // for non-video services.
    double bwPortion = network.getModel().otherBWIntensity;
    bandwidthPrice = network.bandwidthPrice * bwPortion;
  } else {
    bandwidthPrice = network.bandwidthPrice;
  }

  double totalPrice = 0d;
  totalPrice += network.bundle.price;
  totalPrice += other.content.price;
  totalPrice += bandwidthPrice;

  // Assume that CPs can pass through IXC prices to consumers, and also that
  // NSPs don't pay IXC prices to themselves.
  totalPrice += network.ixcPrice * network.getModel().otherBWIntensity;

  return totalPrice;

}

@Override
public void consume(Consumers consumers, double qty) {
  consumers.accumulatedUtility += utility(qty);
  consumers.accumulatedCost += getTotalCost() * qty;

  network.bundle.qty += qty;
  other.content.qty += qty;

  /*
   * Bandwidth Fees
   */
  // For Video
  if (zeroRated) {
    network.zeroRatingDiscounts += network.videoBW.price * qty;
  } else {
    network.videoBW.qty += qty;
  }
  // For Other
  network.otherBW.qty += qty;

  /*
   *  IXC Fees
   */
  // Network Operator Receives
  // Doesn't receive IXCs from itself
  network.ixcAvoided += qty * network.videoIXC.price;
  network.otherIXC.qty += qty;  // Does receive from other CP
  
  // Content Providers Pay
  // NSP doesn't send to itself, avoidance tracked above
  other.ixcPaid += qty * network.otherIXC.price; // Is paid by other CP.

}

}
