package neutrality;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class Consumers {

// final NeutralityModel model;
final double income;
final double gamma;
final double beta;
final double tau;
final double psi;
final double linearDemandTerm;

double accumulatedUtility;
double accumulatedCost;

PrintStream debugOut;

public Consumers(final double income,
                 final double gamma,
                 final double tau,
                 final double psi,
                 final double linearDemandTerm,
                 final PrintStream debugOut) {

  this.income = income;
  this.gamma = gamma;
  this.tau = tau;
  this.psi = psi;
  this.beta = 1 / (gamma - 1);
  this.linearDemandTerm = linearDemandTerm;

  this.debugOut = debugOut;

  accumulatedUtility = 0.0d;
  accumulatedCost = 0.0d;
}

/**
 * This function determines the quantities of each offer to consume, then
 * records that consumption and it's effects. The effects of the consumption are
 * in the ConsumptionOption class.
 *
 * @param options
 *          A list of possible options
 */
public void consume(List<ConsumptionOption> options) {

  double[] prices = extractPrices(options);
  double[] quantities = determineConsumption(options);

  if (debugOut != null) {
    debugOut.println("Consumption Price/Qty vectors:");
    debugOut.println(Arrays.toString(prices));
    debugOut.println(Arrays.toString(quantities));
    // Total purchased, in $
    double total = 0.0;
    for (int i = 0; i < prices.length; i++) {
      total += prices[i] * quantities[i];
    }
    debugOut.println("total spent was " + total + ", income was " + income);
  }

  for (int i = 0; i < options.size(); i++) {
    ConsumptionOption co = options.get(i);
    co.consume(this, quantities[i]);
  }

}

public double[] determineConsumption(List<ConsumptionOption> options) {

  double[] capTerm = new double[options.size()];
  double[] capitalTerms_toBeta = new double[options.size()];
  double[] capitalTerms_toNegBeta = new double[options.size()];
  double[] prices_toNegBeta = new double[options.size()];
  double[] prices_toBetaPlus1 = new double[options.size()];
  double[] quantities = new double[options.size()];
  double[] prices = extractPrices(options);

  // Preliminary calculations we need for the demand curve
  for (int i = 0; i < options.size(); i++) {
    ConsumptionOption option = options.get(i);

    capTerm[i] = option.K();

    capitalTerms_toBeta[i] = Math.pow(capTerm[i], beta);
    capitalTerms_toNegBeta[i] = Math.pow(capTerm[i], -beta);
    prices_toNegBeta[i] = Math.pow(prices[i], -beta);
    prices_toBetaPlus1[i] = Math.pow(prices[i], beta + 1);
  }

  // i = thing we're calculating demand for
  // j = all other options

  // Actual calculation of quantity demanded.
  for (int i = 0; i < options.size(); i++) {
    double den = 0.0; // denominator of demand func
    for (int j = 0; j < options.size(); j++) {
      if (i == j) {
        den += prices[i];
      } else {
        double term = capitalTerms_toBeta[i];
        term *= capitalTerms_toNegBeta[j];
        term *= prices_toNegBeta[i];
        term *= prices_toBetaPlus1[j];
        den += term;
        if (debugOut != null)
          debugOut.println("term is " + term);
      }

    }
    // Residual term sensitive to price/capital balance
    double orElse = capitalTerms_toBeta[i];
    orElse *= prices_toNegBeta[i];
    
    if (debugOut != null)
      debugOut.println("residterm is " + orElse);
    den += orElse;

    if (debugOut != null)
      debugOut.println("total denominator is " + den);

    double firstTerm = income / den;

    // Residual term for all other goods; quasi-linear demand.
    // double secondTerm = prices[i] * linearDemandTerm;
    // double secondTerm = linearDemandTerm * prices[i] * prices[i] /
    // capTerm[i];
    
    // Small negative constant to prevent convergence to zero
//    double secondTerm = 1; //prices[i];
    

    double qty = firstTerm;  //- secondTerm;

    /*
     * Obviously, quantity has a floor of zero. However, this flattens the
     * fitness landscape, and agents need a slope here just to point them
     * towards the portion of the fitness landscape where there may be positive
     * returns. This doesn't prevent agents from choosing prices or investment
     * that result in consumption arbitrarily close to zero.
     * 
     */
    if (qty < 1d) {
      // Punish any agent that sells negative units.
      double pun = 1 / qty;
      pun = pun * 100d;
      
      ConsumptionOption offendingOption = options.get(i);
      // An initial cliff
      double cliff = 100000d;
      
      offendingOption.network.fitnessAdjustment -= cliff + pun;
      offendingOption.video.fitnessAdjustment -= cliff + pun;
      offendingOption.other.fitnessAdjustment -= cliff + pun;

      // Set to zero for other purposes.
      qty = 0d;
    }

    if (Double.isNaN(qty) || Double.isInfinite(qty))
      throw new RuntimeException();

    quantities[i] = qty;
  }
  return quantities;
}

public double[] extractPrices(List<ConsumptionOption> options) {
  double[] toReturn = new double[options.size()];
  for (int i = 0; i < options.size(); i++) {
    toReturn[i] = options.get(i).getTotalCost();
  }
  return toReturn;
}

}
