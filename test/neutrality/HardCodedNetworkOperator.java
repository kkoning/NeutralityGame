package neutrality;

import agency.AgentModel;
import agency.NullIndividual;
import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

public class HardCodedNetworkOperator extends NetworkOperator<NullIndividual> {

	NetworkOffer			fixedNetworkOffer;
	ContentOffer			fixedVideoContentOffer;
	BundledOffer			fixedBundledOffer;

	
	/**
	 * A default constructor for debugging purposes. All values=1
	 */
	public HardCodedNetworkOperator() {

		// Investment
		networkInvestment = 1;

		// Vertically Integrated Content Provider, use corresponded hard coded
		// content provider
		integratedContentProvider = new HardCodedContentProvider();
		integratedContentProvider.isVideoProvider = true;

		// Network Offer
		fixedNetworkOffer = new NetworkOffer(this, 1, 1);

		// Content Offer
		fixedVideoContentOffer = integratedContentProvider.getContentOffer();

		// Bundled Offer
		fixedBundledOffer = new BundledOffer(this, integratedContentProvider, 2, 1, false);

	}

	
	
	@Override
	public void setModel(AgentModel model) {
		super.setModel(model);
		integratedContentProvider.setModel(model);
	}

@Override
public BundledOffer getBundledZeroRatedOffer() {
	return null;
}

@Override
	void step() {
		// Nothing needs to be done each step; offers are hard coded and fixed.
	}

	@Override
	public double getInterconnectionBandwidthPrice() {
		return 1;
	}

	@Override
	public NetworkOffer getNetworkOffer() {
		return fixedNetworkOffer;
	}

	@Override
	public ContentOffer getVideoContentOffer() {
		return fixedVideoContentOffer;
	}

	@Override
	public BundledOffer getBundledOffer() {
		return fixedBundledOffer;
	}

	
	
}
