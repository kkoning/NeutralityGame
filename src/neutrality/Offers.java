package neutrality;

import com.sun.istack.internal.NotNull;

import static agency.util.Misc.BUG;

public class Offers {

public static class ContentOffer {
final ContentProvider contentProvider;
final double          price;

public ContentOffer(
                    int step,
                    @NotNull ContentProvider contentProvider,
                    double price) {
  if (contentProvider == null)
    BUG("ContentOffer cannot have a null ContentProvider");
  if (price < 1E-20)
    price = 1E-20;

  NeutralityModel.verifySaneAmount(price);

  this.contentProvider = contentProvider;
  this.price = price;
}

@Override
public String toString() {
  return "ContentOffer{" +
      "Ka=" + contentProvider.Ka +
      ", p=" + price +
      '}';
}
}

public static class NetworkOnlyOffer {
final NetworkOperator network;
final double          connectionPrice;
final double          bandwidthPrice;
final int             step;

public NetworkOnlyOffer(
                        int step,
                        @NotNull NetworkOperator networkOperator,
                        double connectionPrice,
                        double bandwidthPrice) {

  NeutralityModel.verifySaneAmount(connectionPrice);
  NeutralityModel.verifySaneAmount(bandwidthPrice);

  if (networkOperator == null)
    BUG("NetworkOnlyOffer created with null NetworkOperator");
  if (connectionPrice < 1E-20)
    connectionPrice = 1E-20;
  if (bandwidthPrice <= 1E-20)
    bandwidthPrice = 1E-20;

  // Connection prices are interpreted as a premium over marginal cost.
  connectionPrice += networkOperator.getModel().nspMarginalCost;

  this.step = step;
  this.network = networkOperator;
  this.connectionPrice = connectionPrice;
  this.bandwidthPrice = bandwidthPrice;
}

@Override
public String toString() {
  return "NetworkOnlyOffer{" +
         "Kn=" + network.Kn +
         ", Pn=" + connectionPrice +
         ", Pbw=" + bandwidthPrice +
         '}';
}
}

public static class BundleOffer {
final NetworkOperator networkOperator;
double                bundlePrice;
final double          bandwidthPrice;
final int             step;

public BundleOffer(
                   int step,
                   NetworkOnlyOffer unbundled,
                   double bundlePremium) {

  NeutralityModel.verifySaneAmount(bundlePremium);

  if (unbundled.network == null)
    BUG("NetworkAndVideoBundleOffer created with null NetworkOperator");
  if (!unbundled.network.getModel().policyBundlingAllowed)
    BUG("NetworkAndVideoBundleOffer made, but bundling not allowed.");

  if (bundlePremium < 1E-20)
    bundlePremium = 1E-20;

  bundlePrice = unbundled.connectionPrice + bundlePremium;
  bandwidthPrice = unbundled.bandwidthPrice;

  this.step = step;
  this.networkOperator = unbundled.network;
}

@Override
public String toString() {
  return "NetworkAndVideoBundleOffer{" +
         "Kn=" + networkOperator.Kn +
         ", Ka=" + networkOperator.Ka +
         ", Pb=" + bundlePrice +
         ", Pbw=" + bandwidthPrice +
         '}';
}
}

}
