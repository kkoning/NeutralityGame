package neutrality;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Consumers {

	NeutralityModel	agentModel;

	int				numConsumers;
	double[]		preferenceFactors;
	double[]		incomes;
	double[]		runningSurplus;

	public Consumers(int numConsumers, double topIncome, NeutralityModel agentModel) {
		this.numConsumers = numConsumers;
		this.agentModel = agentModel;

		// Per individual consumer stuff, preferences and income
		preferenceFactors = new double[numConsumers];
		incomes = new double[numConsumers];
		Random r = ThreadLocalRandom.current();
		for (int i = 0; i < numConsumers; i++) {
			// preference factors are random
			preferenceFactors[i] = r.nextDouble();

			// incomes are evenly distributed from [0,topIncome]
			incomes[i] = ((double) i / numConsumers) * topIncome;
		}

		// Running surplus initialized to zero.
		runningSurplus = new double[numConsumers];
	}

	public void procurementProcess(List<ConsumptionOption> options) {

		// Calculate the surplus for each option
		ConsumptionOptionSurplus cos;
		cos = determineSurplusses(options);

		// Consume the best positive option, or nothing if all negative.
		for (int i = 0; i < numConsumers; i++) {
			// starting initial best is to consume nothing
			ConsumptionOption bestOption = null;
			double bestOptionValue = 0;
			for (int j = 0; j < cos.consumptionOptions.size(); j++) {
				// i_th consumer, j_th option

				// if i_th consumer, j_th option is better
				if (cos.surplus[j][i] > bestOptionValue) {
					// then probably consume it instead
					bestOption = cos.consumptionOptions.get(j);
					bestOptionValue = cos.surplus[j][i];
				}
			}

			// We've tried to find the best consumption option. If there
			// was one, consume it.
			if (bestOption != null) {
				// Buy it
				bestOption.consume();
				// keep track of our surplus.
				runningSurplus[i] += bestOptionValue;
			}
		}
	}

	/**
	 * Given a list of consumption options, this function returns a list of
	 * surplusses. The actual values for each consumer are determined by method
	 * determineAppValues().
	 * 
	 * @param contentOffersVerticalSegment
	 * @param contentOffersOtherSegment
	 * @param networkOffers
	 * @param bundledOffers
	 * @param zeroRatedOffers
	 * @param bundledZeroRatedOffers
	 */
	ConsumptionOptionSurplus determineSurplusses(List<ConsumptionOption> options) {
		/*
		 * This function currently seems computationally expensive. How bad is
		 * it? Is there a way to do this easier? Reduce mathematically? Look
		 * into symbolic regression?
		 */

		// randomizing order of offers to eliminate potential
		// order effects, e.g., from equalities
		Collections.shuffle(options);

		int numOptions = options.size();
		// Surplus starts at zero.
		double[][] consumerSurplus = new double[numOptions][numConsumers];

		for (int i = 0; i < numOptions; i++) {
			ConsumptionOption option = options.get(i);

			// Add value of video content, if any.
			if (option.videoContent != null) {
				double[] videoContentValue = determineAppValues(
						agentModel.getVideoContentValue(),
						option.videoContent.getInvestment(),
						option.network.getInvestment(),
						option.videoContent.getPreference());
				for (int j = 0; j < numConsumers; j++) {
					consumerSurplus[i][j] += videoContentValue[j];
				}
			}

			// Add value of other content, if any.
			if (option.otherContent != null) {
				double[] otherContentValue = determineAppValues(
						agentModel.getOtherContentValue(),
						option.otherContent.getInvestment(),
						option.network.getInvestment(),
						option.otherContent.getPreference());
				for (int j = 0; j < numConsumers; j++) {
					consumerSurplus[i][j] += otherContentValue[j];
				}
			}

			// Subtract price
			for (int j = 0; j < numConsumers; j++) {
				consumerSurplus[i][j] -= option.cost;
			}
		}

		ConsumptionOptionSurplus consumptionOptionSurpluses = new ConsumptionOptionSurplus();
		consumptionOptionSurpluses.consumptionOptions = options;
		consumptionOptionSurpluses.surplus = consumerSurplus;
		return consumptionOptionSurpluses;
	}

	/**
	 * This function corresponds to the consumers' utility function. (Eqn. #1 in
	 * the proposal)
	 * 
	 * @param sectorValue
	 * @param appInvestment
	 * @param appPrice
	 * @param netInvestment
	 * @param bandwidthIntensity
	 * @param bandwidthPrice
	 * @param appPreference
	 * @return
	 */
	double[] determineAppValues(
			double sectorValue,
			double appInvestment,
			double netInvestment,
			double appPreference) {

		double[] values = new double[numConsumers];

		double appVal = Math.pow(appInvestment, agentModel.psi);
		double netVal = Math.pow(netInvestment, agentModel.tau);
		double abstractVal = appVal * netVal * sectorValue;

		for (int i = 0; i < values.length; i++) {
			double pref = (1 - agentModel.theta) * Math.abs(preferenceFactors[i] - appPreference);
			double value = abstractVal * incomes[i] * pref;
			values[i] = value;
		}

		return values;
	}

	public double[] getSurplusses() {
		return runningSurplus;
	}

	/**
	 * @return The sum of all consumer surplusses
	 */
	public double getTotalSurplus() {
		double totalSurplus = 0;

		for (double d : runningSurplus)
			totalSurplus += d;

		return totalSurplus;

	}

	@Override
	public String toString() {
		return "Consumers [alpha=" + agentModel.alpha + ", beta=" + agentModel.beta + ", psi="
				+ agentModel.psi + ", tau=" + agentModel.tau + ", theta=" + agentModel.theta
				+ ", numConsumers=" + numConsumers
				+ /*
					 * ", preferenceFactors=" +
					 * Arrays.toString(preferenceFactors) + ", incomes=" +
					 * Arrays.toString(incomes) +
					 */ ", integratedValue=" + agentModel.getVideoContentValue() + ", otherValue="
				+ agentModel.getOtherContentValue() + ", videoBWIntensity="
				+ agentModel.getVideoBWIntensity() + ", otherBWIntensity="
				+ agentModel.getOtherBWIntensity() + "]";
	}

	public class ConsumptionOptionSurplus {

		List<ConsumptionOption>	consumptionOptions;

		/**
		 * Contains the surplus of each option for each consumer. The first
		 * dimension represents the option#, such that surplus[m][n] corresponds
		 * to the surplus for option <consumptionOptions.get(m)> for consumer n.
		 * 
		 * Or, in shorthand, surplus[option][consumer].
		 */
		double[][]				surplus;

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Consumer\t");
			sb.append("Income\t");
			sb.append("Preference\t");
			// for each consumption option
			for (int i = 0; i < consumptionOptions.size(); i++) {
				sb.append(consumptionOptions.get(i) + "\t");
			}
			sb.append("\n");

			// for each consumer
			for (int i = 0; i < surplus[0].length; i++) {
				sb.append(i + "\t");
				sb.append(incomes[i] + "\t");
				sb.append(preferenceFactors[i] + "\t");
				for (int j = 0; j < consumptionOptions.size(); j++) {
					sb.append(surplus[j][i] + "\t");
				}
				sb.append("\n");
			}

			return sb.toString();
		}

	}

}
