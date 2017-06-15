package neutrality;

import static neutrality.NeutralityModel.CapitalCalculationMethod.COBB_DOUGLASS;
import static neutrality.NeutralityModel.CapitalCalculationMethod.LOG_LOG;

/**
 * This
 *
 * @author kkoning
 */
public abstract class ConsumptionOption {

public NetworkOperator network;
public ContentProvider video;
public ContentProvider other;
public boolean zeroRated;

public abstract double getTotalCost();
public abstract void consume(Consumers consumers, double qty);

public double K() {

  double netTerm, vidTerm, othTerm;
  
  if (network.getModel().capCalcMethod.equals(LOG_LOG)) {
    netTerm = Math.log(network.Kn + Math.E);
    vidTerm = Math.log(video.Ka + Math.E);
    othTerm = Math.log(other.Ka + Math.E);
  } else if (network.getModel().capCalcMethod.equals(COBB_DOUGLASS)) {
    netTerm = Math.pow(network.Kn,network.getModel().tau);
    vidTerm = Math.pow(video.Ka,network.getModel().psi);
    othTerm = Math.pow(other.Ka,network.getModel().psi);
  } else {
    throw new RuntimeException();
  }
  
  double vidVal = network.getModel().videoContentValue;
  double othVal = network.getModel().otherContentValue;
  
  double kTot = netTerm * vidTerm * vidVal + netTerm * othTerm * othVal;
  return kTot;
}

public double utility(double qty) {
  return K() * Math.pow(qty, network.getModel().gamma);
}

}
