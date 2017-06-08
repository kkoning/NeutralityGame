package neutrality;

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
//  double netTerm = Math.pow(network.Kn,network.getModel().tau);
//  double vidTerm = Math.pow(video.Ka,network.getModel().psi);
//  double othTerm = Math.pow(other.Ka,network.getModel().psi);
  
  double netTerm = Math.log(network.Kn + Math.E);
  double vidTerm = Math.log(video.Ka + Math.E);
  double othTerm = Math.log(other.Ka + Math.E);
  
  double vidVal = network.getModel().videoContentValue;
  double othVal = network.getModel().otherContentValue;
  
  double kTot = netTerm * vidTerm * vidVal + netTerm * othTerm * othVal;
  return kTot;
}

public double utility(double qty) {
  return K() * Math.pow(qty, network.getModel().gamma);
}

}
