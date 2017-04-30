package neutrality;

import com.sun.istack.internal.NotNull;

import neutrality.cp.ContentProvider;
import neutrality.nsp.NetworkOperator;

import static agency.util.Misc.BUG;

public class Offers {

public static class ContentOffer {
final ContentProvider<?> contentProvider;
final double             price;
final int                step;

public ContentOffer(
    int step,
    @NotNull ContentProvider<?> contentProvider,
    double price) {
  if (contentProvider == null)
    BUG("ContentOffer cannot have a null ContentProvider");
  if (price <= 0)
    BUG("ContentOffer must have a positive price");

  this.step = step;
  this.contentProvider = contentProvider;
  this.price = price;
}

@Override
public String toString() {
  return "ContentOffer{" +
      "Ka=" + contentProvider.getKa(step) +
      ", p=" + price +
      '}';
}
}

public static class NetworkOnlyOffer {
final NetworkOperator<?> network;
final double             connectionPrice;
final double             bandwidthPrice;
final int                step;

public NetworkOnlyOffer(
    int step,
    @NotNull NetworkOperator<?> networkOperator,
    double connectionPrice,
    double bandwidthPrice) {

  if (networkOperator == null)
    BUG("NetworkOnlyOffer created with null NetworkOperator");
  if (connectionPrice <= 0)
    BUG("NetworkOnlyOffer must have a positive connection price.");
  if (bandwidthPrice <= 0)
    BUG("NetworkOnlyOffer must have a positive bandwidth price.");

  this.step = step;
  this.network = networkOperator;
  this.connectionPrice = connectionPrice;
  this.bandwidthPrice = bandwidthPrice;
}

@Override
public String toString() {
  return "NetworkOnlyOffer{" +
      "Kn=" + network.getKn(step) +
      ", Pn=" + connectionPrice +
      ", Pbw=" + bandwidthPrice +
      '}';
}
}

public static class NetworkAndVideoBundleOffer {
final NetworkOperator<?> networkOperator;
final double             bundlePrice;
final double             bandwidthPrice;
final int                step;

public NetworkAndVideoBundleOffer(
    int step,
    @NotNull NetworkOperator<?> networkOperator,
    double bundlePrice,
    double bandwidthPrice) {

  if (networkOperator == null)
    BUG("NetworkAndVideoBundleOffer created with null NetworkOperator");
  if (bundlePrice <= 0)
    BUG("NetworkAndVideoBundleOffer must have a positive connection price.");
  if (bandwidthPrice <= 0)
    BUG("NetworkAndVideoBundleOffer must have a positive bandwidth price.");
  if (!networkOperator.getModel().policyBundlingAllowed)
    BUG("NetworkAndVideoBundleOffer made, but bundling not allowed.");

  this.step = step;
  this.networkOperator = networkOperator;
  this.bundlePrice = bundlePrice;
  this.bandwidthPrice = bandwidthPrice;
}

@Override
public String toString() {
  return "NetworkAndVideoBundleOffer{" +
      "Kn=" + networkOperator.getKn(step) +
      ", Ka=" + networkOperator.getKa(step) +
      ", Pb=" + bundlePrice +
      ", Pbw=" + bandwidthPrice +
      '}';
}
}

}
