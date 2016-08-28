package neutrality;

public class Offers {

	public static class ContentOffer {
		ContentProvider	content;
		double			contentPrice;
	}

	public static class NetworkOffer {
		NetworkOperator	network;
		double			connectionPrice;
		double			bandwidthPrice;
	}

	public static class BundledOffer {
		NetworkOperator	network;
		ContentProvider	videoContent;
		double			bundlePrice;
		double			bandwidthPrice;
		boolean			contentZeroRated;
	}

}
