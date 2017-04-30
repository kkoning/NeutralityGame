package neutrality;

import agency.eval.EvaluationGroup;
import agency.vector.VectorIndividual;
import neutrality.cp.DirectlyEncodedContentProvider;
import neutrality.nsp.DirectlyEncodedNetworkOperator;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by liara on 2/7/17.
 */
public class DirectlyEncodedManualTest {


@Test
public void test() {
  EvaluationGroup eg = new EvaluationGroup();


  NeutralityModel nm = new NeutralityModel();
  nm.alpha = 1d;
  nm.beta = 1d;
  nm.psi = 0.4;
  nm.tau = 0.4;
  nm.gamma = 0.5;
  nm.income = 1_000_000d;
  nm.maxSteps = 3;
  nm.policyNSPContentAllowed = true;
  nm.policy0PriceIXC = false;
  nm.policyBundlingAllowed = true;
  nm.policyZeroRated = false;
  nm.firmEndowment = 10_000d;
  nm.enableDebug(System.out);
  nm.init();

  eg.setModel(nm);


  // Test environment with two NSPs and two CPs of each kind.

  for (int i = 0; i < 2; i++) {
    DirectlyEncodedNetworkOperator neno = new DirectlyEncodedNetworkOperator();
    neno.setManager(
            randomVectorDoubleIndividual(
                    DirectlyEncodedNetworkOperator.Position.values().length,
                    2,
                    3)
    );
    eg.addAgent(neno);
  }
  for (int i = 0; i < 2; i++) {
    DirectlyEncodedContentProvider decp = new DirectlyEncodedContentProvider();
    decp.setManager(
            randomVectorDoubleIndividual(
                    DirectlyEncodedContentProvider.Position.values().length,
                    2,
                    3)
    );
    decp.setVideo(true);
    eg.addAgent(decp);
  }
  for (int i = 0; i < 2; i++) {
    DirectlyEncodedContentProvider decp = new DirectlyEncodedContentProvider();
    decp.setManager(
            randomVectorDoubleIndividual(
                    DirectlyEncodedContentProvider.Position.values().length,
                    2,
                    3)
    );
    decp.setVideo(false);
    eg.addAgent(decp);
  }

  eg.run();

  System.out.println(eg.getSummaryData());

}

static VectorIndividual<Double> randomVectorDoubleIndividual(
        int genomeLength,
        double mean,
        double sd) {
  VectorIndividual<Double> ind = new VectorIndividual<>(genomeLength);
  Object[] genome = ind.getGenome();
  Random r = ThreadLocalRandom.current();
  for (int i = 0; i < genomeLength; i++) {
    double var = sd * r.nextGaussian();
    genome[i] = mean + var;
  }
  return ind;
}


}
