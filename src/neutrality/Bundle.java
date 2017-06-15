package neutrality;

import java.util.Optional;

public class Bundle extends ConsumptionOption {

public Bundle(NetworkOperator network,
              Optional<ContentProvider> other,
              boolean zeroRated) {
  this.network = network;
  this.video = Optional.of(network);
  this.other = other;
  this.zeroRated = zeroRated;
}

@Override
public double getTotalCost() {
  // Connection/Content Prices
  double totalPrice = 0d;
  totalPrice += network.bundle.price;
  if (other.isPresent())
    totalPrice += other.get().content.price;
  
  // Assume CPs pass through IXC fees
  if (other.isPresent())
    totalPrice += network.otherIXC.price;
  
  // Consumer Bandwidth fees
  if (!zeroRated)
    totalPrice += network.videoBW.price;
  if (other.isPresent())
    totalPrice += network.otherBW.price;

  return totalPrice;
}

@Override
public void consume(Consumers consumers, double qty) {
  // Consumer costs/benefits
  consumers.accumulatedUtility += consumers.utility(this, qty);
  consumers.accumulatedCost += getTotalCost() * qty;

  // Connection/Content
  network.bundle.qty += qty;
  if (other.isPresent())
    other.get().content.qty += qty;

  // IXC fees
  network.ixcAvoided += qty * network.videoIXC.price;
  if (other.isPresent()) {
    network.otherIXC.qty += qty;
    other.get().ixcPaid += network.otherIXC.price * qty;
  }

  // Consumer Bandwidth fees
  if (video.isPresent()) {
    if (zeroRated) {
      network.zeroRatingDiscounts += network.videoBW.price * qty;
    } else {
      network.videoBW.qty += qty;
    }
  }
  if (other.isPresent()) {
    network.otherBW.qty += qty;
  }


}

}
