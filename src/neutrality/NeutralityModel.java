package neutrality;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import agency.Agent;
import agency.AgentModel;
import agency.Fitness;
import agency.Individual;
import agency.SimpleFirm;
import agency.SimpleFitness;
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
			no.step();  // Network Operators
		for (ContentProvider<?> cp : videoContentProviders)
			cp.step();  // Video Content Providers
		for (ContentProvider<?> cp : otherContentProviders)
			cp.step();  // Other Content Providers
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

	@Override
	public Object getSummaryData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getStepData() {
		// TODO Auto-generated method stub
		return null;
	}

}
