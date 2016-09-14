package neutrality;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import agency.Agent;
import agency.AgentModel;
import agency.Fitness;
import agency.Individual;
import agency.SimpleFirm;
import agency.SimpleFitness;
import agency.util.Statistics;
import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

public class NeutralityModel implements AgentModel {

	public double				alpha;
	public double				beta;

	public double				psi;
	public double				tau;
	public double				theta;

	int							maxSteps;

	List<NetworkOperator<?>>	networkOperators;
	List<ContentProvider<?>>	videoContentProviders;
	List<ContentProvider<?>>	otherContentProviders;
	Consumers					consumers;

	public NeutralityModel() {
		networkOperators = new ArrayList<>();
		videoContentProviders = new ArrayList<>();
		otherContentProviders = new ArrayList<>();
	}

	@Override
	public void addAgent(Agent<? extends Individual> agent) {
		// Classify and track each agent into proper role within model
		if (agent instanceof NetworkOperator)
			networkOperators.add((NetworkOperator<?>) agent);
		else if (agent instanceof ContentProvider) {
			ContentProvider<?> cp = (ContentProvider<?>) agent;
			if (cp.isVideoProvider)
				videoContentProviders.add(cp);
			else
				otherContentProviders.add(cp);
		} else
			// We don't know what to do with this agent type
			throw new RuntimeException("Unsupported agent type: " + agent);
	}

	@Override
	public Fitness getFitness(Agent<? extends Individual> agent) {
		// They should all be instances of SimpleFirm
		SimpleFirm<?> firm = (SimpleFirm<?>) agent;
		SimpleFitness fitness = firm.getFitness();
		return fitness;
	}

	@Override
	public boolean step() {

		// Step each agent; allow them to generate and update offers.
		for (NetworkOperator<?> no : networkOperators)
			no.step(); // Network Operators
		for (ContentProvider<?> cp : videoContentProviders)
			cp.step(); // Video Content Providers
		for (ContentProvider<?> cp : otherContentProviders)
			cp.step(); // Other Content Providers
		// Consumers do not need to be stepped; behavior is specified.

		// Agents generate offers, add them to these lists.
		List<NetworkOffer> networkOnlyOffers = new ArrayList<>();
		List<ContentOffer> videoContentOffers = new ArrayList<>();
		List<ContentOffer> otherContentOffers = new ArrayList<>();
		List<BundledOffer> bundledOffers = new ArrayList<>();

		// Network Operators
		for (NetworkOperator<?> no : networkOperators) {
			if (no.getNetworkOffer() != null)
				networkOnlyOffers.add(no.getNetworkOffer());
			if (no.getVideoContentOffer() != null)
				videoContentOffers.add(no.getVideoContentOffer());
			if (no.getBundledOffer() != null)
				bundledOffers.add(no.getBundledOffer());
		}

		// Video Content Providers
		for (ContentProvider<?> cp : videoContentProviders) {
			if (cp.getContentOffer() != null)
				videoContentOffers.add(cp.getContentOffer());
		}

		// Other Content Providers
		for (ContentProvider<?> cp : otherContentProviders) {
			if (cp.getContentOffer() != null)
				otherContentOffers.add(cp.getContentOffer());
		}

		/*
		 * Now that we have a list of all the offers made by network operators
		 * and content providers, we need to generate a list of possible
		 * consumption options for consumers to consider.
		 */
		List<ConsumptionOption> options = ConsumptionOption.determineOptions(
				this,
				networkOnlyOffers,
				videoContentOffers,
				otherContentOffers,
				bundledOffers);

		// Consumers consider and consume offers.
		// Details are specified in Consumers.procurementProcess
		consumers.procurementProcess(options);

		// Don't terminate early.
		return false;
	}

	/**
	 * @return the consumer valuation of video content.
	 */
	public double getVideoContentValue() {
		// Make calculations of sector value based on alpha
		double videoContentValue;
		videoContentValue = this.alpha / (1.0 + this.alpha);
		return videoContentValue;
	}

	/**
	 * @return the consumer valuation of other content.
	 */
	public double getOtherContentValue() {
		return (1 - getVideoContentValue());
	}

	/**
	 * @return the relative intensity of bandwidth usage for video content.
	 */
	public double getVideoBWIntensity() {
		// Make calculations of bw intensity based on beta
		double videoBWIntensity;
		videoBWIntensity = this.beta / (1.0 + this.beta);
		return videoBWIntensity;
	}

	/**
	 * This value depends on the model parameter beta.
	 * 
	 * @return the relative intensity of bandwidth usage for other content.
	 */
	public double getOtherBWIntensity() {
		// Calculations of bw intensity based on beta, both always sum to 1.
		return (1 - getVideoBWIntensity());
	}

	@Override
	public int getMaxSteps() {
		return maxSteps;
	}

	/*
	 * TODO: In progress...
	 * 
	 * (non-Javadoc)
	 * 
	 * @see agency.AgentModel#getSummaryData()
	 */
	@Override
	public Object getSummaryData() {
		OutputData o = new OutputData();

		/*
		 * Data on network operatros
		 */
		// How many
		o.numNetworkOperators = networkOperators.size();

		// Total investment & HHI
		o.networkOperatorInvestment = networkOperators.stream()
				.mapToDouble(no -> no.networkInvestment).sum();
		o.networkOperatorInvestmentHHI = Statistics.HHI(
				networkOperators.stream().map(no -> no.networkInvestment).toArray(Double[]::new));

		// Standalone Network offers & HHI
		o.numStandaloneNetworkOffersAccepted = networkOperators.stream()
				.mapToInt(no -> no.numStandaloneNetworkOffersAccepted).sum();
		o.numStandaloneNetworkOffersHHI = Statistics.HHI(
				networkOperators.stream().map(no -> no.networkInvestment).toArray(Integer[]::new));

		/*
		 * Data on Video Markets, in which both NSPs and some CPs participate.
		 */

		// Standalone Content Offers & HHI
		// These are by both NSPs and others
		o.numNSPStandaloneVideoOffersAccepted = networkOperators.stream()
				.mapToInt(no -> no.getNumStandaloneContentOffersAccepted()).sum();
		o.numThirdPartyStandaloneVideoOffersAccepted = videoContentProviders.stream()
				.mapToInt(vcp -> vcp.numAcceptedOffers).sum();
		o.numStandaloneNetworkOffersAccepted = o.numNSPStandaloneVideoOffersAccepted
				+ o.numThirdPartyStandaloneVideoOffersAccepted;
		o.numStandaloneVideoOffersHHI = Statistics.HHI(
				Stream.concat(
						networkOperators.stream()
								.map(no -> no.getNumStandaloneContentOffersAccepted()),
						videoContentProviders.stream().map(vcp -> vcp.numAcceptedOffers))
						.toArray(Integer[]::new));

		/*
		 * Data on standalone video content providers
		 */
		o.numVideoContentProviders = videoContentProviders.size();

		/*
		 * Data on other content providers
		 */
		o.numOtherContentProvides = otherContentProviders.size();

		return o;
	}

	@Override
	public Object getStepData() {
		// Do not output any per-step data.
		return null;
	}

	public static class OutputData {
		/*
		 * This object gets translated directly into column headers and data
		 * values in output files. For this reason, it needs to be flattened and
		 * not hierarchical. Also, it will necessarily be somewhat repetitive
		 * because fields from other object cannot be re-used directly.
		 * 
		 * Notes to self:
		 * 
		 * (1) Use first class types to distinguish between null and default
		 * values. (2) Don't add values here faster than populating them.
		 * 
		 */

		// Use first class

		Integer	numNetworkOperators;
		Double	networkOperatorInvestment;
		Double	networkOperatorInvestmentHHI;
		Integer	numStandaloneNetworkOffersAccepted;
		Double	numStandaloneNetworkOffersHHI;

		Integer	numStandaloneVideoOffersAccepted;
		Integer	numNSPStandaloneVideoOffersAccepted;
		Integer	numThirdPartyStandaloneVideoOffersAccepted;
		Double	numStandaloneVideoOffersHHI;

		Integer	numVideoContentProviders;

		Integer	numOtherContentProvides;

		// return "NetworkOperator [networkInvestment=" + networkInvestment
		// + ", numStandaloneNetworkOffersAccepted=" +
		// numStandaloneNetworkOffersAccepted
		// + ", numStandaloneContentOffersAccepted=" +
		// getNumStandaloneContentOffersAccepted()
		// + ", numBundledOffersAccepted=" + numBundledOffersAccepted
		// + ", numBundledZeroRatedOffersAccepted=" +
		// numBundledZeroRatedOffersAccepted
		// + ", totalStandaloneNetworkRevenue=" + totalStandaloneNetworkRevenue
		// + ", totalStandaloneContentRevenue=" +
		// getTotalStandaloneContentRevenue()
		// + ", totalBundledRevenue=" + totalBundledRevenue + ",
		// totalBundledZeroRatedRevenue="
		// + totalBundledZeroRatedRevenue + ", totalConsumerBandwidthPayments="
		// + totalConsumerBandwidthPayments + ",
		// totalConsumerBandwidthPaymentsFromVideo="
		// + totalConsumerBandwidthPaymentsFromVideo
		// + ", totalConsumerBandwidthPaymentsFromOther="
		// + totalConsumerBandwidthPaymentsFromOther
		// + ", totalInterconnectionPaymentsReceived=" +
		// totalInterconnectionPaymentsReceived
		// + ", totalInterconnectionPaymentsFromVideo=" +
		// totalInterconnectionPaymentsFromVideo
		// + ", totalInterconnectionPaymentsFromOther=" +
		// totalInterconnectionPaymentsFromOther
		// + ", integratedContentProvider=" + integratedContentProvider + "]";
		// }

	}

}
