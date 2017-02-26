package neutrality;

import java.util.*;

public class Consumers {

final NeutralityModel model;
final double          income;
final double          gamma;
final double          beta;
final double          tau;
final double          psi;

double accumulatedUtility;

public Consumers(NeutralityModel model, double income) {
  this.income = income;
  this.gamma = model.gamma;
  this.tau = model.tau;
  this.psi = model.psi;
  this.beta = 1 / (gamma - 1);
  this.model = model;
  accumulatedUtility = 0.0d;
}

/**
 * This function determines the quantities of each offer to consume, then
 * records that consumption and it's effects.  The effects of the consumption
 * are in the ConsumptionOption class.
 *
 * @param options
 *         A list of possible options
 */
public void consume(List<ConsumptionOption> options) {

  double[] capitalTerms_toBeta = new double[options.size()];
  double[] capitalTerms_toNegBeta = new double[options.size()];
  double[] prices = new double[options.size()];
  double[] prices_toNegBeta = new double[options.size()];
  double[] prices_toBetaPlus1 = new double[options.size()];
  double[] quantities = new double[options.size()];

  double vidValue = this.model.videoContentValue;
  double othValue = this.model.otherContentValue;

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

    // but not always both types of content
    if (option.videoContent.isPresent())
      vidCapTerm = Math.pow(option.K_v, psi);
    if (option.otherContent.isPresent())
      othCapTerm = Math.pow(option.K_o, psi);

    double vidCapTot = vidValue * netCapTerm * vidCapTerm;
    double othCapTot = othValue * netCapTerm * othCapTerm;
    double capTot = model.videoContentValue * vidCapTot +
                    model.otherContentValue * othCapTot;

    capitalTerms_toBeta[i] = Math.pow(capTot, beta);
    capitalTerms_toNegBeta[i] = Math.pow(capTot, -beta);
    prices[i] = option.totalCostToConsumer;
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
    double secondTerm = -prices[i] * model.demandPriceCoefficient;

    double qty = firstTerm + secondTerm;

    // Quantity cannot be negative.
    if (qty <= 0)
      qty = 0d;

    quantities[i] = qty;
  }

  if (model.isDebugEnabled()) {
    model.debugOut.println("Consumption Price/Qty vectors:");
    model.debugOut.println(Arrays.toString(prices));
    model.debugOut.println(Arrays.toString(quantities));
    // Total purchased, in $
    double total = 0.0;
    for (int i = 0; i < prices.length; i++) {
      total += prices[i] * quantities[i];
    }
    this.model.debugOut.println("total spent was " +
                                total +
                                ", income was " +
                                income);
  }

  for (int i = 0; i < options.size(); i++) {
    ConsumptionOption co = options.get(i);
    co.consume(quantities[i]);
    accumulatedUtility += co.getUtility(quantities[i]);
  }

}

}
