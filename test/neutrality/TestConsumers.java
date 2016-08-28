package neutrality;

import java.util.ArrayList;

import org.junit.Test;

import neutrality.Consumers.ConsumptionOptionSurplus;
import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

public class TestConsumers {
	@Test
	public void testCreate() {
		NeutralityModel nm = new NeutralityModel();
		nm.alpha = 0.5;
		nm.beta = 0.5;
		nm.psi = 0.4;
		nm.tau = 0.4;
		nm.theta = 0.2;
		Consumers c = new Consumers(100, 100, nm);
		for (int i = 0; i < c.numConsumers; i++) {
			System.out.println(
					"Consumer " + i + "\tincome=" + c.incomes[i] + "\tpref="
							+ c.preferenceFactors[i]);
		}
	}

	@Test
	public void testEvaluateSurplus() {
		NeutralityModel nm = new NeutralityModel();
		nm.alpha = 0.5;
		nm.beta = 0.5;
		nm.psi = 0.4;
		nm.tau = 0.4;
		nm.theta = 0.2;
		Consumers c = new Consumers(100, 100, nm);
		
		// Create a group of standalone offers.

		// One network operator with K_n=25, P_n=3, and P_b=3.
		NetworkOperator netOp = new NetworkOperator();
		netOp.networkInvestment = 25;
		NetworkOffer netOffer = new NetworkOffer();
		netOffer.network = netOp;
		netOffer.connectionPrice = 3;
		netOffer.bandwidthPrice = 3;

		// Another network operator with K_n=60, P_n=9, and P_b=9.
		NetworkOperator netOp2 = new NetworkOperator();
		netOp2.networkInvestment = 60;
		NetworkOffer netOffer2 = new NetworkOffer();
		netOffer2.network = netOp2;
		netOffer2.connectionPrice = 9;
		netOffer2.bandwidthPrice = 9;

		ArrayList<NetworkOffer> netOffers = new ArrayList<>();
		netOffers.add(netOffer);
		netOffers.add(netOffer2);

		
		// Content provider A with Q=9, P=1, pref=0
		ContentProvider cp_A = new ContentProvider();
		cp_A.contentInvestment = 9;
		cp_A.preference = 0;
		ContentOffer co_A = new ContentOffer();
		co_A.content = cp_A;
		co_A.contentPrice = 1;

		// Content provider B with Q=16, P=5, pref=1
		ContentProvider cp_B = new ContentProvider();
		cp_B.contentInvestment = 16;
		cp_B.preference = 1;
		ContentOffer co_B = new ContentOffer();
		co_B.content = cp_B;
		co_B.contentPrice = 5;

		// Offerings for CP A and B are both in the vertically integrated
		// segment
		ArrayList<ContentOffer> contentOffersIntegrated = new ArrayList<>();
		contentOffersIntegrated.add(co_A);
		contentOffersIntegrated.add(co_B);

		// Content provider C with Q=19, P=13, pref=0.5
		ContentProvider cp_C = new ContentProvider();
		cp_C.contentInvestment = 19;
		cp_C.preference = 0.5;
		ContentOffer co_C = new ContentOffer();
		co_C.content = cp_A;
		co_C.contentPrice = 13;

		// Offerings for CP C in the other segment
		ArrayList<ContentOffer> contentOffersOther = new ArrayList<>();
		contentOffersOther.add(co_C);

		// Bundled offer of NetOp and CP A
		BundledOffer bo1 = new BundledOffer();
		bo1.network = netOp;
		bo1.videoContent = cp_A;
		bo1.bandwidthPrice = 1;
		bo1.bundlePrice = 4;
		bo1.contentZeroRated = false;

		// Bundled offer of NetOp and CP A, zero rated
		BundledOffer bo2 = new BundledOffer();
		bo2.network = netOp;
		bo2.videoContent = cp_A;
		bo2.bandwidthPrice = 1;
		bo2.bundlePrice = 6;
		bo2.contentZeroRated = true;

		ArrayList<BundledOffer> bos = new ArrayList<>();
		bos.add(bo1);
		bos.add(bo2);

		// Create the combination of purchase options possible from these offers
		ArrayList<ConsumptionOption> options;
		options = c.determineOptions(netOffers, contentOffersIntegrated, contentOffersOther, bos);

		// Print out details about consumers
		System.out.println(c);

		// Print a list of all the possible combinations of consumption
		for (ConsumptionOption option : options) {
			System.out.println(option);
		}

		ConsumptionOptionSurplus cos = c.determineSurplusses(options);
		System.out.println(cos);

		
		
		
	}

}
