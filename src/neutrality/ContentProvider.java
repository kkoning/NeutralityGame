package neutrality;

import agency.Individual;
import agency.SimpleFirm;

public class ContentProvider<T extends Individual> extends SimpleFirm<T> {
	
	double contentInvestment;
	double preference;
	
	public double getInvestment() {
		return contentInvestment;
	}

	public double getPreference() {
		return preference;
	}


}
