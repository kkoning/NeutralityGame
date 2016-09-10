package neutrality;

import java.util.Arrays;

import org.junit.Test;

public class TestAgentModel {

	@Test
	public void testAgentModel() {
		NeutralityModel model = new NeutralityModel();
		// For now, hard-code parameter values.
		model.alpha = 1;
		model.beta = 1;
		model.psi = 0.4;
		model.tau = 0.4;
		model.theta = 0.2;
		model.maxSteps = 10;

		model.consumers = new Consumers(100, 100, model);

		// Create one network operator and content provider
		HardCodedNetworkOperator hcno = new HardCodedNetworkOperator(model);
		HardCodedContentProvider hccpVideo = new HardCodedContentProvider(model,true);
		hccpVideo.preference = 1;
		HardCodedContentProvider hccpOther = new HardCodedContentProvider(model,false);

		model.addAgent(hcno);
		model.addAgent(hccpVideo);
		model.addAgent(hccpOther);

		System.out.println("Executing Model");
		for (int i = 0; i < 10; i++) {
			model.step();
		}

		System.out.println("Printing Fitnesses");
		System.out.println("Network Operator Fitness = " + hcno.getFitness().toString());
		System.out.println("Video Content Provider Fitness = " + hccpVideo.getFitness().toString());
		System.out.println("Other Content Provider Fitness = " + hccpOther.getFitness().toString());
		System.out.println("Consumer Surplus = " + model.consumers.getTotalSurplus());
		System.out.println(
				"Detailed Consumer Surpluses = "
						+ Arrays.toString(model.consumers.getSurplusses()));

		System.out.println(hcno);
		System.out.println(hccpVideo);
		System.out.println(hccpOther);
	}

}