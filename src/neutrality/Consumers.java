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
final double videoContentValue;
final double otherContentValue;

double accumulatedUtility;

PrintStream debugOut;

public Consumers(final double income,
                 final double gamma,
                 final double tau,
                 final double psi,
                 final double videoContentValue,
                 final double otherContentValue,
                 final double linearDemandTerm,
                 final PrintStream debugOut) {

  this.income = income;
  this.gamma = gamma;
  this.tau = tau;
  this.psi = psi;
  this.beta = 1 / (gamma - 1);
  this.linearDemandTerm = linearDemandTerm;
  this.videoContentValue = videoContentValue;
  this.otherContentValue = otherContentValue;

  this.debugOut = debugOut;

  accumulatedUtility = 0.0d;
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
  double[] quantities = determineConsumption(options, prices);

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
    co.consume(quantities[i]);
    accumulatedUtility += co.getUtility(quantities[i]);
  }

}

public double[] determineConsumption(List<ConsumptionOption> options, double[] prices) {

  double[] capitalTerms_toBeta = new double[options.size()];
  double[] capitalTerms_toNegBeta = new double[options.size()];
  double[] prices_toNegBeta = new double[options.size()];
  double[] prices_toBetaPlus1 = new double[options.size()];
  double[] quantities = new double[options.size()];

  // Preliminary calculations we need for the demand curve
  for (int i = 0; i < options.size(); i++) {
    ConsumptionOption option = options.get(i);

    // Calculate capital terms to avoid lots of repetitive Math.pow() calls
    // (The effect of alpha and beta is captured here too)
    double netCapTerm;
    double vidCapTerm = 0.0;
    double othCapTerm = 0.0;

    // options always have a network
    netCapTerm = Math.pow(option.K_n, tau);
    // netCapTerm = Math.log(option.K_n + Math.E);

    // but not always both types of content
    if (option.videoContent.isPresent()) {
      // vidCapTerm = Math.log(option.K_v + Math.E);
      vidCapTerm = Math.pow(option.K_v, psi);

    }
    if (option.otherContent.isPresent()) {
      // othCapTerm = Math.log(option.K_o + Math.E);
      othCapTerm = Math.pow(option.K_o, psi);
    }

    double vidCapTot = videoContentValue * netCapTerm * vidCapTerm;
    double othCapTot = otherContentValue * netCapTerm * othCapTerm;
    double capTot = vidCapTot + othCapTot;

    capitalTerms_toBeta[i] = Math.pow(capTot, beta);
    capitalTerms_toNegBeta[i] = Math.pow(capTot, -beta);
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
      }

      // Residual term sensitive to price/capital balance
      double orElse = capitalTerms_toBeta[i];
      orElse *= prices_toNegBeta[i];
      den += orElse;
    }

    double firstTerm = income / den;

    // Residual term for all other goods; quasi-linear demand.
    double secondTerm = prices[i] * linearDemandTerm;

    double qty = firstTerm - secondTerm;

    // Quantity cannot be negative.
    if (qty <= 0)
      qty = 0d;

    if (Double.isNaN(qty) || Double.isInfinite(qty))
      throw new RuntimeException();

    quantities[i] = qty;
  }
  return quantities;
}

public double[] extractPrices(List<ConsumptionOption> options) {
  double[] toReturn = new double[options.size()];
  for (int i = 0; i < options.size(); i++) {
    toReturn[i] = options.get(i).totalCostToConsumer;
  }
  return toReturn;
}

}
