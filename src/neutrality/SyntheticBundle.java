package neutrality;

public class SyntheticBundle extends ConsumptionOption {

public SyntheticBundle(NetworkOperator network,
                       ContentProvider video,
                       ContentProvider other,
                       boolean zeroRated) {
  this.network = network;
  this.video = video;
  this.other = other;
  this.zeroRated = zeroRated;
}

@Override
public double getTotalCost() {
  double totalPrice = 0d;

  totalPrice += network.netOnly.price;
  totalPrice += video.content.price;
  totalPrice += other.content.price;

  if (!zeroRated) {
    totalPrice += network.videoBW.price;
  } 
  totalPrice += network.otherBW.price;
  
  // Assume that CPs can pass through IXC prices to consumers, and also that
  // NSPs don't pay IXC prices to themselves.
  if (network != video) {
    totalPrice += network.ixcPrice * network.getModel().videoBWIntensity;
  }
  totalPrice += network.ixcPrice * network.getModel().otherBWIntensity;

  return totalPrice;
}

@Override
public void consume(Consumers consumers, double qty) {
  consumers.accumulatedUtility += utility(qty);
  consumers.accumulatedCost += getTotalCost() * qty;

  network.netOnly.qty += qty;
  video.content.qty += qty;
  other.content.qty += qty;
  
  /*
   *  Bandwidth Fees
   */
  // For Video
  if (zeroRated) {
    network.zeroRatingDiscounts += network.videoBW.price * qty;
  } else {
    network.videoBW.qty += qty;
  }
  // For other
  network.otherBW.qty += qty;
  
  
  /*
   *  IXC Fees
   */
  // Network Operator Receives
  if (network == video) {  // But not from itself.
    network.ixcAvoided += qty * network.videoIXC.price;
  } else {
    network.videoIXC.qty += qty;
  }
  network.otherIXC.qty += qty;

  // Content Providers Pay
  video.ixcPaid += network.videoIXC.price * qty;
  other.ixcPaid += network.otherIXC.price * qty;
}

}
