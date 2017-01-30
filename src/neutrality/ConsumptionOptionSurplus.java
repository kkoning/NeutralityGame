package neutrality;

import java.util.List;

/**
 * Calculates information about consumer value and consumption decisions.  This
 * class should be thoroughly debugged and tested because it contains much of
 * the core math of the model.
 */
public final class ConsumptionOptionSurplus {
/**
 * Contains a list of possible options for consumers to consider
 */
List<ConsumptionOption> consumptionOptions;
double[] consumerIncomes;
double[] consumerPreferences;
/**
 * Contains the surplus of each option for each consumer. The first
 * dimension represents the consumer#, such that surplus[n][m] corresponds
 * to the surplus for option <consumptionOptions.get(n)> for consumer m.
 * <p>
 * Or, in shorthand, surplus[option][consumer].
 */
double[][] surplus;
double[] bestValue;
int[]    bestOption;
/**
 * Model Parameters
 */
private double psi, tau, theta;
/**
 * Functions of alpha and beta
 */
private double videoValue, otherValue;

public ConsumptionOptionSurplus(NeutralityModel model, Consumers consumers) {
  this(model.alpha,
       model.psi,
       model.tau,
       model.theta,
       consumers.incomes,
       consumers.preferenceFactors);
}

public ConsumptionOptionSurplus(
        double alpha,
        double psi,
        double tau,
        double theta,
        double[] consumerIncomes,
        double[] consumerPreferences) {
  this.psi = psi;
  this.tau = tau;
  this.theta = theta;
  this.consumerIncomes = consumerIncomes;
  this.consumerPreferences = consumerPreferences;
  videoValue = NeutralityModel.videoContentValue(alpha);
  otherValue = NeutralityModel.otherContentValue(alpha);

  bestOption = new int[consumerIncomes.length];
  bestValue = new double[consumerIncomes.length];

  // Sanity check; lengths of income and preference vectors should match.
  if (consumerIncomes.length != consumerPreferences.length) {
    throw new RuntimeException();
  }
}

public void setConsumptionOptions(List<ConsumptionOption> options) {
  /*
  The surplus matrix is of dimension options.size() *
  consumerIncome.length.  This means that these arrays might need to change
  if the # of options changes.  This is also where they'll be set initially.
   */
  if (surplus == null) {
    // If null, allocate for the first time.
    surplus = new double[options.size()][consumerIncomes.length];
  } else {
    // otherwise, allocate a new one only if the # of consumption options is
    // different than it was last time.
    if (this.consumptionOptions.size() != options.size()) {
      surplus = new double[options.size()][consumerIncomes.length];
    }
  }

  this.consumptionOptions = options;
}

public void calculate() {
  /*
   * Reset the calculation of surpluses;
   */
  for (int i = 0; i < consumptionOptions.size(); i++) {
    for (int j = 0; j < consumerIncomes.length; j++) {
      surplus[i][j] = 0.0;
    }
  }

  for (int optionNum = 0; optionNum < consumptionOptions.size(); optionNum++) {
    ConsumptionOption option = consumptionOptions.get(optionNum);

    /*
     * First, must calculate the values for each consumer/option pair. Start with
     * the value received from video, if any.
     */
    if (option.hasVideo()) {
      Consumers.addAppValues(
              consumerIncomes,
              videoValue,
              option.videoInvestment(),
              option.networkInvestment(),
              consumerPreferences,
              option.videoPreference(),
              psi,
              tau,
              theta,
              surplus[optionNum]);
    }

    /*
     * Then add the value received from other content, if any.
     */
    if (option.hasOther()) {
      Consumers.addAppValues(
              consumerIncomes,
              otherValue,
              option.otherInvestment(),
              option.networkInvestment(),
              consumerPreferences,
              option.otherPreference(),
              psi,
              tau,
              theta,
              surplus[optionNum]);
    }

    /*
     * The total value is the combined value of video and other content, minus
     * costs.
     */
    double cost = option.getCost();
    for (int j = 0; j < consumerIncomes.length; j++) {
      surplus[optionNum][j] -= cost;
    }
  }

  /*
   * Reset the bestValue and bestOption judgements; the default is no
   * consumption.
   */
  for (int i = 0; i < bestValue.length; i++) {
    bestValue[i] = 0.0;
    bestOption[i] = -1;
  }


  /*
   * We need to keep track of the highest positive value, and choose that option
    */
  for (int j = 0; j < consumerIncomes.length; j++) {
    for (int i = 0; i < consumptionOptions.size(); i++) {

      if (surplus[i][j] > bestValue[j]) {
        bestValue[j] = surplus[i][j];
        bestOption[j] = i;
      }
    }
  }

}

public ConsumptionOption[] getChosenOptions() {
  ConsumptionOption[] toReturn = new ConsumptionOption[consumerIncomes.length];
  for (int i = 0; i < consumerIncomes.length; i++) {
    int bestOptionNum = bestOption[i];
    if (bestOptionNum >= 0) { // will be null if = -1
      toReturn[i] = consumptionOptions.get(bestOptionNum);
    }
  }

  return toReturn;
}

public double[] getSurplus() {
  return bestValue;
}

public String optionsTable() {
  StringBuffer sb = new StringBuffer();
  sb.append("Option\t");
  sb.append("Price\t");
  sb.append("Generic_Value\t");
  sb.append("Video_Value\t");
  sb.append("Other_Value\t");
  sb.append("K_n\t");
  sb.append("K_vid\t");
  sb.append("Pref_vid\t");
  sb.append("K_oth\t");
  sb.append("Pref_oth\n");

  for (int i = 0; i < consumptionOptions.size(); i++) {
    ConsumptionOption co = consumptionOptions.get(i);

    double nativeValue = 0;
    Double videoPart = null;
    if (co.hasVideo()) {
      double appVal = Math.pow(co.videoContent.contentInvestment, psi);
      double netVal = Math.pow(co.network.getInvestment(), tau);
      videoPart = videoValue * appVal * netVal;
      nativeValue += videoPart;
    }
    Double otherPart = null;
    if (co.hasOther()) {
      double appVal = Math.pow(co.otherContent.contentInvestment, psi);
      double netVal = Math.pow(co.network.getInvestment(), tau);
      otherPart = otherValue * appVal * netVal;
      nativeValue += otherPart;
    }

    sb.append(i + "\t");
    sb.append(co.cost + "\t");
    sb.append(nativeValue + "\t");
    sb.append(videoPart + "\t");
    sb.append(otherPart + "\t");
    sb.append(co.network.getInvestment() + "\t");
    if (co.hasVideo()) {
      sb.append(co.videoContent.contentInvestment + "\t");
      sb.append(co.videoContent.preference + "\t");
    } else {
      sb.append("null" + "\t");
      sb.append("null" + "\t");
    }
    if (co.hasOther()) {
      sb.append(co.otherContent.contentInvestment + "\t");
      sb.append(co.otherContent.preference);
    } else {
      sb.append("null" + "\t");
      sb.append("null");
    }
    sb.append("\n");
  }
  return sb.toString();
}

public String surplusTable() {
  StringBuffer sb = new StringBuffer();
  sb.append("Consumer\t");
  sb.append("Income\t");
  sb.append("Preference\t");
  // for each consumption option
  for (int i = 0; i < consumptionOptions.size(); i++) {
    sb.append("Option_" + i + "\t");
  }
  sb.append("\n");

  // for each consumer
  for (int i = 0; i < surplus[0].length; i++) {
    sb.append(i + "\t");
    sb.append(consumerIncomes[i] + "\t");
    sb.append(consumerPreferences[i] + "\t");
    for (int j = 0; j < consumptionOptions.size(); j++) {
      sb.append(surplus[j][i] + "\t");
    }
    sb.append("\n");
  }

  return sb.toString();
}

public String consumerChoicesTable() {
  StringBuffer sb = new StringBuffer();
  sb.append("Consumer\tOption\n");
  for (int i = 0; i < bestOption.length; i++) {
    sb.append(i + "\t" + bestOption[i] + "\n");
  }

  return sb.toString();
}

public String sales() {
  StringBuffer sb = new StringBuffer();
  sb.append("Option\tSales\n");
  int[] numChosen = numChosen();
  for (int optionNum = 0; optionNum < consumptionOptions.size(); optionNum++) {
    sb.append(optionNum + "\t" + numChosen[optionNum] + "\n");
  }

  int totalSales = 0;
  for (int i = 0; i < numChosen.length; i++) {
    totalSales += numChosen[i];
  }
  int didntBuy = consumerIncomes.length - totalSales;

  sb.append("None\t" + didntBuy + "\n");
  return sb.toString();
}

int[] numChosen() {
  int[] numChosen = new int[consumptionOptions.size()];
  for (int consumer = 0; consumer < consumerIncomes.length; consumer++) {
    int opChosen = bestOption[consumer];
    if (opChosen > 0)
      numChosen[opChosen]++;
  }

  return numChosen;
}

}
