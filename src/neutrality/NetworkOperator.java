package neutrality;

import agency.Individual;
import agency.NullIndividual;
import agency.SimpleFirm;
import agency.SimpleFitness;
import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

public abstract class NetworkOperator<T extends Individual> extends SimpleFirm<T> {

	double							networkInvestment;

	// Track the # of different purchases of each type
	int								numStandaloneNetworkOffersAccepted;
	int								numBundledOffersAccepted;
	int								numBundledZeroRatedOffersAccepted;

	// Track the total income from different purchases of each type
	double							totalStandaloneNetworkRevenue;
	double							totalBundledRevenue;
	double							totalBundledZeroRatedRevenue;

	// Track the income from consumer bandwidth
	double							totalConsumerBandwidthPayments;
	double							totalConsumerBandwidthPaymentsFromVideo;
	double							totalConsumerBandwidthPaymentsFromOther;

	// Track the income from interconnection
	double							totalInterconnectionPaymentsReceived;
	double							totalInterconnectionPaymentsFromVideo;
	double							totalInterconnectionPaymentsFromOther;

	ContentProvider<NullIndividual>	integratedContentProvider;
	NeutralityModel					model;

	// TODO: Constructor
	NetworkOperator(NeutralityModel model) {
		this.model = model;
	}

	abstract void step();

	double getInvestment() {
		return networkInvestment;
	}

	public abstract NetworkOffer getNetworkOffer();

	public abstract ContentOffer getVideoContentOffer();

	public abstract BundledOffer getBundledOffer();

	public void receiveInterconnectionPayment(ContentProvider<?> cp, double amount) {
		// Credit the payment to our account
		account.receive(amount);

		// Track the payment, by totals overall...
		totalInterconnectionPaymentsReceived += amount;
		// ...and by type of content provider
		if (cp.isVideoProvider)
			totalInterconnectionPaymentsFromVideo += amount;
		else
			totalInterconnectionPaymentsFromOther += amount;
	}

	/**
	 * Called when a consumer accepts an unbundled network offer. Credits the
	 * payment to the Network Operator's account, both for the connection and
	 * bandwidth usage.
	 * 
	 * @param networkOffer
	 *            the network offer that the consumer accepted.
	 * @param videoUsed
	 *            true if the consumer also purchased video content.
	 * @param otherUsed
	 *            true if the consumer also purchased other content.
	 */
	public void processAcceptedNetworkOffer(
			NetworkOffer networkOffer,
			boolean videoUsed,
			boolean otherUsed) {

		// Track # of accepted standalone offers
		numStandaloneNetworkOffersAccepted++;

		// Track total revenue received from connection fees
		account.receive(networkOffer.connectionPrice);
		totalStandaloneNetworkRevenue += networkOffer.connectionPrice;

		// Track consumer bandwidth usage.
		consumerBandwidthUsage(networkOffer.bandwidthPrice, videoUsed, otherUsed);

	}

	/**
	 * Called when a consumer accepts a bundled offer of both network access and
	 * video content. Credits the payment to the network Operator's account. If
	 * the offer was for zero rated content, then the consumer will not be
	 * charged for video bandwidth. Otherwise, it will (video is always included
	 * in bundled offers). If other content was also used, the consumer will be
	 * charged for that as well.
	 * 
	 * @param bundledOffer
	 *            the bundled offer that the consumer accepted
	 * @param otherUsed
	 *            true if the consumer also purchased other content.
	 */
	public void processAcceptedBundledOffer(BundledOffer bundledOffer, boolean otherUsed) {

		// Earn the $
		account.receive(bundledOffer.bundlePrice);

		// Track # of accepted bundled offers accepted
		if (bundledOffer.contentZeroRated) {
			numBundledZeroRatedOffersAccepted++;
			totalBundledZeroRatedRevenue += bundledOffer.bundlePrice;
		} else {
			numBundledOffersAccepted++;
			totalBundledRevenue += bundledOffer.bundlePrice;
		}

		// Track consumer bandwidth usage.
		consumerBandwidthUsage(
				bundledOffer.bandwidthPrice,
				!bundledOffer.contentZeroRated, // if zero rated, don't charge
				otherUsed);

	}

	/**
	 * Track income from _consumer_ bandwidth usage. Note that this is in
	 * addition to the interconnection bandwidth charged to content providers.
	 * 
	 * @param bandwidthPrice
	 *            the price of bandwidth usage
	 * @param chargeVideoBandwidth
	 *            whether to charge for the use of video content
	 * @param chargeOtherBandwidth
	 *            whether to charge for the use of other content
	 */
	private void consumerBandwidthUsage(
			double bandwidthPrice,
			boolean chargeVideoBandwidth,
			boolean chargeOtherBandwidth) {

		double amount = 0;
		if (chargeVideoBandwidth) {
			amount = bandwidthPrice * model.getVideoBWIntensity();
			totalConsumerBandwidthPayments += amount;
			totalConsumerBandwidthPaymentsFromVideo += amount;
			account.receive(amount);
		}
		if (chargeOtherBandwidth) {
			amount = bandwidthPrice * model.getOtherBWIntensity();
			totalConsumerBandwidthPayments += amount;
			totalConsumerBandwidthPaymentsFromOther += amount;
			account.receive(amount);
		}
	}

	@Override
	public SimpleFitness getFitness() {
		/*
		 * In this case, the total fitness of the agent is equal to the sum of
		 * the fitness for the network operator and content provider aspects of
		 * the agent's business.
		 */
		double fitness = account.getBalance();
		if (integratedContentProvider != null)
			fitness += integratedContentProvider.account.getBalance();
		
		return new SimpleFitness(fitness);
	}

	public abstract double getInterconnectionBandwidthPrice();

	public double getNumStandaloneContentOffersAccepted() {
		if (integratedContentProvider == null)
			return 0;
		else
			return integratedContentProvider.numAcceptedOffers;
	}
	
	public double getTotalStandaloneContentRevenue() {
		if (integratedContentProvider == null)
			return 0;
		else
			return integratedContentProvider.totalRevenue;
	}

	
	@Override
	public String toString() {
		return "NetworkOperator [networkInvestment=" + networkInvestment
				+ ", numStandaloneNetworkOffersAccepted=" + numStandaloneNetworkOffersAccepted
				+ ", numStandaloneContentOffersAccepted=" + getNumStandaloneContentOffersAccepted()
				+ ", numBundledOffersAccepted=" + numBundledOffersAccepted
				+ ", numBundledZeroRatedOffersAccepted=" + numBundledZeroRatedOffersAccepted
				+ ", totalStandaloneNetworkRevenue=" + totalStandaloneNetworkRevenue
				+ ", totalStandaloneContentRevenue=" + getTotalStandaloneContentRevenue()
				+ ", totalBundledRevenue=" + totalBundledRevenue + ", totalBundledZeroRatedRevenue="
				+ totalBundledZeroRatedRevenue + ", totalConsumerBandwidthPayments="
				+ totalConsumerBandwidthPayments + ", totalConsumerBandwidthPaymentsFromVideo="
				+ totalConsumerBandwidthPaymentsFromVideo
				+ ", totalConsumerBandwidthPaymentsFromOther="
				+ totalConsumerBandwidthPaymentsFromOther
				+ ", totalInterconnectionPaymentsReceived=" + totalInterconnectionPaymentsReceived
				+ ", totalInterconnectionPaymentsFromVideo=" + totalInterconnectionPaymentsFromVideo
				+ ", totalInterconnectionPaymentsFromOther=" + totalInterconnectionPaymentsFromOther
				+ ", integratedContentProvider=" + integratedContentProvider + "]";
	}

}
