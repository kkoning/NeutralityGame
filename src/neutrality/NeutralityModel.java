package neutrality;

import agency.Agent;
import agency.AgentModel;
import agency.Fitness;
import agency.Individual;

public class NeutralityModel implements AgentModel {

	public double		alpha;
	public double		beta;

	public double		psi;
	public double		tau;
	public double		theta;
	
	
	@Override
	public void addAgent(Agent<? extends Individual> agent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Fitness getFitness(Agent<? extends Individual> agent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean step() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMaxSteps() {
		// TODO Auto-generated method stub
		return 0;
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
