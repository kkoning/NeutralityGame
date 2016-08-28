package neutrality;

import agency.Agent;
import agency.Individual;

public class ContentProvider implements Agent {
	
	double contentInvestment;
	double preference;
	
	public double getInvestment() {
		return contentInvestment;
	}

	public double getPreference() {
		return preference;
	}

	@Override
	public Individual getIndividual() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIndividual(Individual ind) {
		// TODO Auto-generated method stub
		
	}
}
