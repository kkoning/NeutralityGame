package neutrality;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Consumers {

NeutralityModel agentModel;

int      numConsumers;
double[] preferenceFactors;
double[] incomes;
double[] runningSurplus;

ConsumptionOptionSurplus surplusCalculator;

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

  // Start with higher incomes first, for ease of debugging.
  Collections.reverse(Arrays.asList(incomes));

  // Running surplus initialized to zero.
  runningSurplus = new double[numConsumers];

  // Use a single surplus calculator
  surplusCalculator = new ConsumptionOptionSurplus(
          agentModel.alpha,
          agentModel.psi,
          agentModel.tau,
          agentModel.theta,
          incomes,
          preferenceFactors);
}



/**
 * Given a list of different kinds of offers from network operators and
 * content providers, this function returns a list of all possible and
 * allowable combinations of consumption.
 *
 * @param networkOnlyOffers
 * @param videoContentOffers
 * @param otherContentOffers
 * @param bundledOffers
 * @return
 */
public static final ArrayList<ConsumptionOption> determineOptions(
        NeutralityModel model,
        List<Offers.NetworkOffer> networkOnlyOffers,
        List<Offers.ContentOffer> videoContentOffers,
        List<Offers.ContentOffer> otherContentOffers,
        List<Offers.BundledOffer> bundledOffers) {

  // TODO: Put together synthetic offer for zero rated but not bundled
  // content?
  ArrayList<ConsumptionOption> options = new ArrayList<>();

  // Network only offers, put together synthetic bundles.
  for (Offers.NetworkOffer networkOnlyOffer : networkOnlyOffers) {
			/*
			 * We need to create all possible combinations of content for use
			 * with this network offer.
			 */

    // Video content but not other content
    for (Offers.ContentOffer videoContentOffer : videoContentOffers) {
      ConsumptionOption option = new ConsumptionOption(model, networkOnlyOffer,
              videoContentOffer, null);
      options.add(option);
    }

    // No integrated content, but other content
    for (Offers.ContentOffer otherContentOffer : otherContentOffers) {
      ConsumptionOption option = new ConsumptionOption(model, networkOnlyOffer, null,
              otherContentOffer);
      options.add(option);
    }

    // Both integrated and other content
    for (Offers.ContentOffer videoContentOffer : videoContentOffers) {
      for (Offers.ContentOffer otherContentOffer : otherContentOffers) {
        ConsumptionOption option = new ConsumptionOption(model, networkOnlyOffer,
                videoContentOffer, otherContentOffer);
        options.add(option);
      }
    }

  }

  // Bundled offers may not be allowed.
  if (bundledOffers != null)
    for (Offers.BundledOffer bundledOffer : bundledOffers) {
      // Bundled offer without other content
      ConsumptionOption option = new ConsumptionOption(model, bundledOffer, null);
      options.add(option);
      options.add(option);

      // With each combination of unrelated content.
      for (Offers.ContentOffer otherContentOffer : otherContentOffers) {
        // Goods
        option = new ConsumptionOption(model, bundledOffer, otherContentOffer);
        options.add(option);
      }
    }
  return options;
}


public String printConsumerProperties() {
  StringBuffer toReturn = new StringBuffer();
  toReturn.append("Consumer,Income,Preference,Surplus\n");
  for (int i = 0; i < numConsumers; i++) {
    toReturn.append(i);
    toReturn.append(",");
    toReturn.append(incomes[i]);
    toReturn.append(",");
    toReturn.append(preferenceFactors[i]);
    toReturn.append(",");
    toReturn.append(runningSurplus[i]);
    toReturn.append("\n");
  }

  return toReturn.toString();
}

public void consume(ConsumptionOption[] choices, double[] surplus) {
  for (int i = 0; i < choices.length; i++) {
    ConsumptionOption co = choices[i];
    consume(co);
    runningSurplus[i] += surplus[i];
  }
}

void consume(ConsumptionOption co) {
  if (co != null)
    co.payProviders();
}

public void procurementProcess(List<ConsumptionOption> options) {

  surplusCalculator.setConsumptionOptions(options);
  surplusCalculator.calculate();

  if (agentModel.debug) {
    agentModel.debugOut.println("Analysis of consumption choices follows:");
    agentModel.debugOut.println(surplusCalculator.surplusTable());
    agentModel.debugOut.println(surplusCalculator.sales());
  }

  // Go through and actually consume
  ConsumptionOption[] toConsume = surplusCalculator.getChosenOptions();
  double[] surplus = surplusCalculator.getSurplus();
  consume(toConsume, surplus);
}


/**
 * This function corresponds to the consumers' utility function. (Eqn. #1 in
 * the proposal)
 */
static final void addAppValues(
        double income[],
        double sectorValue,
        double appInvestment,
        double netInvestment,
        double consumerPreference[],
        double appPreference,
        double psi,
        double tau,
        double theta,
        double[] values) {

  double appVal = Math.pow(appInvestment, psi);
  double netVal = Math.pow(netInvestment, tau);

  for (int i = 0; i < income.length; i++) {
    // Intermediate calculation to reduce complexity of utility eqn. line
    double preference = 1.0 - (theta * (Math.abs(consumerPreference[i] - appPreference)));

    // First half of Eqn. 1
    double value;
    value = (sectorValue * appVal * netVal * income[i] * preference);
    values[i] += value;
  }

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



}
