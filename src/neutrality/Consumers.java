package neutrality;

import static neutrality.NeutralityModel.CapitalCalculationMethod.COBB_DOUGLASS;
import static neutrality.NeutralityModel.CapitalCalculationMethod.LOG_LOG;

import java.util.Arrays;
import java.util.List;

public class Consumers {

final NeutralityModel model;
final double          income;
final double          videoContentValue;
final double          otherContentValue;
final double          beta;

double accumulatedUtility;
double accumulatedCost;

public Consumers(NeutralityModel model,
                 double income,
                 double videoContentValue,
                 double otherContentValue) {
  this.model = model;
  this.income = income;
  this.videoContentValue = videoContentValue;
  this.otherContentValue = otherContentValue;

  this.beta = 1 / (model.gamma - 1);

  accumulatedUtility = 0.0d;
  accumulatedCost = 0.0d;
}

public double K(ConsumptionOption co) {

  double netTerm, vidTerm, othTerm;

  if (model.capCalcMethod.equals(LOG_LOG)) {
    netTerm = Math.log(co.Kn() + Math.E);
    vidTerm = Math.log(co.videoKa() + Math.E);
    othTerm = Math.log(co.otherKa() + Math.E);
  } else if (model.capCalcMethod.equals(COBB_DOUGLASS)) {
    netTerm = Math.pow(co.Kn(), model.tau);
    vidTerm = Math.pow(co.videoKa(), model.psi);
    othTerm = Math.pow(co.otherKa(), model.psi);
  } else {
    throw new RuntimeException();
  }

  double kTot = netTerm * vidTerm * videoContentValue +
                netTerm * othTerm * otherContentValue;
  return kTot;
}

public double utility(ConsumptionOption co, double qty) {
  return K(co) * Math.pow(qty, model.gamma);
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

  if (model.debugOut != null) {
    model.debugOut.println("Consumption Price/Qty vectors:");
    model.debugOut.println(Arrays.toString(prices));
    model.debugOut.println(Arrays.toString(quantities));
    // Total purchased, in $
    double total = 0.0;
    for (int i = 0; i < prices.length; i++) {
      total += prices[i] * quantities[i];
    }
    model.debugOut.println("total spent was " + total + ", income was " + model.income);
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

    capTerm[i] = K(option);

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
        if (model.debugOut != null)
          model.debugOut.println("term is " + term);
      }

    }
    // Residual term sensitive to price/capital balance
    double orElse = capitalTerms_toBeta[i];
    orElse *= prices_toNegBeta[i];
    if (model.debugOut != null)
      model.debugOut.println("residterm is " + orElse);
    den += orElse;

    if (model.debugOut != null)
      model.debugOut.println("total denominator is " + den);

    double firstTerm = income / den;

    // Residual term for all other goods; quasi-linear demand.
    // double secondTerm = prices[i] * linearDemandTerm;
    // double secondTerm = linearDemandTerm * prices[i] * prices[i] /
    // capTerm[i];

    // Small negative constant to prevent convergence to zero
    double secondTerm = Double.NaN;

    switch (model.demandAdjustmentMethod) {
      case CONSTANT:
        secondTerm = 1;
        break;
      case PRICE:
        secondTerm = prices[i];
        break;
      default:
        throw new RuntimeException();
    }

    double qty = firstTerm - secondTerm;

    /*
     * Obviously, quantity has a floor of zero. However, this flattens the
     * fitness landscape, and agents need a slope here just to point them
     * towards the portion of the fitness landscape where there may be positive
     * returns. This doesn't prevent agents from choosing prices or investment
     * that result in consumption arbitrarily close to zero.
     * 
     */
    if (qty <= 0) {
      // Punish any agent that tries to give a negative qty.
      ConsumptionOption offendingOption = options.get(i);
      offendingOption.network.fitnessAdjustment -= qty * qty * 10;
      if (offendingOption.video.isPresent())
        offendingOption.video.get().fitnessAdjustment -= qty * qty * 10;
      if (offendingOption.other.isPresent())
        offendingOption.other.get().fitnessAdjustment -= qty * qty * 10;

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
