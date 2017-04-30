package neutrality;

import agency.eval.EvaluationGroup;
import neutrality.cp.ContentProvider;
import neutrality.nsp.NetworkOperator;

import org.junit.Test;

/**
 * Created by liara on 2/7/17.
 */
public class GeneralTest {


@Test
public void test() {
  EvaluationGroup eg = new EvaluationGroup();


  NeutralityModel nm = new NeutralityModel();
  nm.alpha = 1d;
  nm.beta = 1d;
  nm.psi = 0.4;
  nm.tau = 0.4;
  nm.gamma = 0.5;
  nm.demandPriceCoefficient = 1d;
  nm.income = 1_000_000d;
  nm.maxSteps = 3;
  nm.policyNSPContentAllowed = true;
  nm.policy0PriceIXC = false;
  nm.policyBundlingAllowed = true;
  nm.policyZeroRated = false;
  nm.firmEndowment = 10_000d;
  nm.enableDebug(System.out);
  nm.init();

  // Test environment with two NSPs and two CPs of each kind.

  // One network operator with Kn=25, P_n=3, and P_b=3.
  NetworkOperator netOp1 = new RandomNetworkOperator(8,1);
  netOp1.setModel(nm);
  netOp1.init();

  NetworkOperator netOp2 = new RandomNetworkOperator(9,0.3);
  netOp2.setModel(nm);
  netOp2.init();

  ContentProvider vcp1 = new RandomContentProvider(10,1);
  vcp1.setModel(nm);
  vcp1.setVideo(true);
  vcp1.init();

  ContentProvider vcp2 = new RandomContentProvider(11,2);
  vcp2.setModel(nm);
  vcp2.setVideo(true);
  vcp2.init();

  ContentProvider ocp1 = new RandomContentProvider(12,2);
  ocp1.setModel(nm);
  ocp1.setVideo(false);
  ocp1.init();

  ContentProvider ocp2 = new RandomContentProvider(13,2);
  ocp2.setModel(nm);
  ocp2.setVideo(false);
  ocp2.init();

  eg.setModel(nm);
  eg.addAgent(netOp1);
  eg.addAgent(netOp2);
  eg.addAgent(vcp1);
  eg.addAgent(vcp2);
  eg.addAgent(ocp1);
  eg.addAgent(ocp2);

  eg.run();

  System.out.println(eg.getSummaryData());

}

}
