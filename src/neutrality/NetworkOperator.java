package neutrality;

import agency.Individual;
import agency.SimpleFirm;

public class NetworkOperator<T extends Individual> extends SimpleFirm<T> {
	double networkInvestment;
	
	double getInvestment() {
		return networkInvestment;
	}

}
