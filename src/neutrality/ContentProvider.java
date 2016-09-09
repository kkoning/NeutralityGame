package neutrality;

import agency.Individual;
import agency.SimpleFirm;
import neutrality.Offers.ContentOffer;

public abstract class ContentProvider<T extends Individual> extends SimpleFirm<T> {

	// Parameters relevant to consumer value; investment and preference.
	double			contentInvestment;
	double			preference;

	/**
	 * True if this content provider is in the video market, false if it in the
	 * other content market.
	 */
	boolean			isVideoProvider;

	// Keep reference to
	NeutralityModel	model;

	// Track the # of units sold, revenue, and $ for interconnection
	int				numAcceptedOffers;
	double			totalRevenue;
	double			totalPaidForInterconnection;

	// TODO: Constructor
	public ContentProvider(NeutralityModel model, boolean isVideoProvider) {
		this.model = model;
		this.isVideoProvider = isVideoProvider;
	}

	public double getInvestment() {
		return contentInvestment;
	}

	public double getPreference() {
		return preference;
	}

	public abstract ContentOffer getContentOffer();

	/**
	 * Is called whenever a content provider's offer is accepted by a consumer.
	 * 
	 * @param acceptedOffer
	 */
	public void processAcceptedContentOffer(
			ContentOffer acceptedOffer,
			NetworkOperator<?> onNetwork) {
		// Earn $
		account.receive(acceptedOffer.contentPrice);

		// Pay network provider for bandwidth use.
		payInterconnectionBandwidth(onNetwork);
	}

	/**
	 * Pay interconnection bandwidth to the consumer's network operator. This is
	 * left as an abstract function
	 * 
	 * @param toNetwork
	 *            the consumer's network operator.
	 */
	void payInterconnectionBandwidth(NetworkOperator<?> toNetwork) {

		// How much to pay depends on BW usage of sector apps.
		double bwIntensity;
		if (isVideoProvider)
			bwIntensity = model.getVideoBWIntensity();
		else
			bwIntensity = model.getOtherBWIntensity();
		double paymentAmount = bwIntensity * toNetwork.getInterconnectionBandwidthPrice();

		// Pay and track.
		account.pay(paymentAmount); // from us
		toNetwork.receiveInterconnectionPayment(this, paymentAmount); // to nsp
		totalPaidForInterconnection += paymentAmount;

	}

	public abstract void step();

}
