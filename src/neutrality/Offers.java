package neutrality;

public class Offers {

public static class ContentOffer {
  ContentProvider<?> content;
  double             contentPrice;

  public ContentOffer(ContentProvider<?> contentProvider, double contentPrice) {
    this.content = contentProvider;
    this.contentPrice = contentPrice;
  }

  @Override
  public String toString() {
    return "ContentOffer{" +
            "content=" + content +
            ", contentPrice=" + contentPrice +
            '}';
  }
}

public static class NetworkOffer {
  NetworkOperator<?> network;
  double             connectionPrice;
  double             bandwidthPrice;

  public NetworkOffer(
          NetworkOperator<?> networkOperator,
          double connectionPrice,
          double bandwidthPrice) {
    this.network = networkOperator;
    this.connectionPrice = connectionPrice;
    this.bandwidthPrice = bandwidthPrice;
  }

  @Override
  public String toString() {
    return "NetworkOffer{" +
            "network=" + network +
            ", connectionPrice=" + connectionPrice +
            ", bandwidthPrice=" + bandwidthPrice +
            '}';
  }
}

public static class BundledOffer {
  NetworkOperator<?> network;
  ContentProvider<?> videoContent;
  double             bundlePrice;
  double             bandwidthPrice;
  boolean            contentZeroRated;

  /**
   * If sanity_checks is true, additional checks will be performed.
   */
  static final boolean sanity_checks = true;

  public BundledOffer(
          NetworkOperator<?> networkOperator,
          ContentProvider<?> videoContent,
          double bundlePrice,
          double bandwidthPrice,
          boolean contentZeroRated) {

    if (sanity_checks) {
      if (networkOperator == null)
        throw new RuntimeException("Cannot create bundled offer w/o a network");
      if (videoContent == null)
        throw new RuntimeException("Cannot create bundled offer w/o video content");
    }

    this.network = networkOperator;
    this.videoContent = videoContent;
    this.bundlePrice = bundlePrice;
    this.bandwidthPrice = bandwidthPrice;
    this.contentZeroRated = contentZeroRated;
  }

  @Override
  public String toString() {
    return "BundledOffer{" +
            "network=" + network +
            ", videoContent=" + videoContent +
            ", bundlePrice=" + bundlePrice +
            ", bandwidthPrice=" + bandwidthPrice +
            ", contentZeroRated=" + contentZeroRated +
            '}';
  }
}

}
