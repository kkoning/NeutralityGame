package neutrality;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liara on 2/7/17.
 */
public class AnalyticalConsumers
        extends Consumers {

double income;
double gamma; // default 0.5?  exponent on quantities.
double beta; // 1 / (gamma - 1)
double tau;
double psi;

public AnalyticalConsumers(
        double income,
        double gamma,
        NeutralityModel agentModel) {
  super(1, 1, agentModel);
  this.income = income;
  this.gamma = gamma;
  this.beta = 1 / (gamma - 1);
  this.tau = agentModel.tau;
  this.psi = agentModel.psi;
}

@Override
public void procurementProcess(List<ConsumptionOption> options) {

  double[] capitalTerms_toBeta = new double[options.size()];
  double[] capitalTerms_toNegBeta = new double[options.size()];
  double[] prices = new double[options.size()];
  double[] prices_toNegBeta = new double[options.size()];
  double[] prices_toBetaPlus1 = new double[options.size()];
  double[] quantities = new double[options.size()];

  double vidValue = this.agentModel.videoContentValue();
  double othValue = this.agentModel.otherContentValue();

  // Preliminary calculations we need for the demand curve
  for (int i = 0; i < options.size(); i++) {
    ConsumptionOption option = options.get(i);

    // Calculate capital terms to avoid lots of repetitive Math.pow() calls
    // (The effect of alpha and beta is captured here too)
    double netCapTerm = 0.0;
    double vidCapTerm = 0.0;
    double othCapTerm = 0.0;

    // options have a network
    netCapTerm = Math.pow(option.networkInvestment(), tau);
    if (option.hasVideo())
      vidCapTerm = Math.pow(option.videoInvestment(), psi);
    if (option.hasOther())
      othCapTerm = Math.pow(option.otherInvestment(), psi);

    double vidCapTot = vidValue * netCapTerm * vidCapTerm;
    double othCapTot = othValue * netCapTerm * othCapTerm;
    double capTot = vidCapTot + othCapTot;

    capitalTerms_toBeta[i] = Math.pow(capTot,beta);
    capitalTerms_toNegBeta[i] = Math.pow(capTot,-beta);
    prices[i] = option.cost;
    prices_toNegBeta[i] = Math.pow(option.cost,-beta);
    prices_toBetaPlus1[i] = Math.pow(option.cost,beta+1);
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

      // This is an "other goods" term.  Comment to deactivate.
//      double orelse = capitalTerms_toBeta[i];
//      orelse *= prices_toNegBeta[i];
//      den += orelse;
    }

    double qty = income / den;
    quantities[i] = qty;
  }

  if (this.agentModel.debug) {
    this.agentModel.debugOut.println("Consumption Price/Qty vectors:");
    this.agentModel.debugOut.println(Arrays.toString(prices));
    this.agentModel.debugOut.println(Arrays.toString(quantities));
    // Total purchased, in $
    double total = 0.0;
    for (int i = 0; i < prices.length; i++) {
      total += prices[i] * quantities[i];
    }
    System.out.println("total spent was " + total + ", income was " + income);

  }

  for (int i = 0; i < options.size(); i++) {
    ConsumptionOption co = options.get(i);
    co.payProviders(quantities[i]);
  }

  // TODO: Figure out what to do about consumer surplus

}

}
