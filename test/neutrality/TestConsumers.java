package neutrality;

import java.util.ArrayList;

import org.junit.Test;

import neutrality.Consumers.ConsumptionOptionSurplus;
import neutrality.Offers.BundledOffer;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkOffer;

public class TestConsumers {
    @Test
    public void testCreate() {
        NeutralityModel nm = new NeutralityModel();
        nm.alpha = 0.5;
        nm.beta = 0.5;
        nm.psi = 0.4;
        nm.tau = 0.4;
        nm.theta = 0.2;
        Consumers c = new Consumers(100, 100, nm);
        for (int i = 0; i < c.numConsumers; i++) {
            System.out.println(
                    "Consumer " + i + "\tincome=" + c.incomes[i] + "\tpref="
                            + c.preferenceFactors[i]);
        }
    }

    @Test
    public void testEvaluateSurplus() {
        NeutralityModel nm = new NeutralityModel();
        nm.alpha = 0.5;
        nm.beta = 0.5;
        nm.psi = 0.4;
        nm.tau = 0.4;
        nm.theta = 0.2;
        Consumers c = new Consumers(100, 100, nm);

        // Create a group of standalone offers.

        // One network operator with K_n=25, P_n=3, and P_b=3.
        NetworkOperator<?> netOp = new HardCodedNetworkOperator();
        netOp.setModel(nm);
        netOp.networkInvestment = 25;
        NetworkOffer netOffer = new NetworkOffer(netOp, 3, 3);

        // Another network operator with K_n=60, P_n=9, and P_b=9.
        NetworkOperator<?> netOp2 = new HardCodedNetworkOperator();
        netOp2.setModel(nm);
        netOp2.networkInvestment = 60;
        NetworkOffer netOffer2 = new NetworkOffer(netOp2, 9, 9);

        ArrayList<NetworkOffer> netOffers = new ArrayList<>();
        netOffers.add(netOffer);
        netOffers.add(netOffer2);


        // Content provider A with Q=9, P=1, pref=0
        ContentProvider<?> cp_A = new HardCodedContentProvider();
        cp_A.setModel(nm);
        cp_A.isVideoProvider = true;
        cp_A.contentInvestment = 9;
        cp_A.preference = 0;
        ContentOffer co_A = new ContentOffer(cp_A, 1);

        // Content provider B with Q=16, P=5, pref=1
        ContentProvider<?> cp_B = new HardCodedContentProvider();
        cp_B.setModel(nm);
        cp_B.isVideoProvider = true;
        cp_B.contentInvestment = 16;
        cp_B.preference = 1;
        ContentOffer co_B = new ContentOffer(cp_B, 5);

        // Offerings for CP A and B are both in the vertically integrated
        // segment
        ArrayList<ContentOffer> contentOffersIntegrated = new ArrayList<>();
        contentOffersIntegrated.add(co_A);
        contentOffersIntegrated.add(co_B);

        // Content provider C with Q=19, P=13, pref=0.5
        ContentProvider<?> cp_C = new HardCodedContentProvider();
        cp_C.setModel(nm);
        cp_C.isVideoProvider = false;
        cp_C.contentInvestment = 19;
        cp_C.preference = 0.5;
        ContentOffer co_C = new ContentOffer(cp_C, 13);

        // Offerings for CP C in the other segment
        ArrayList<ContentOffer> contentOffersOther = new ArrayList<>();
        contentOffersOther.add(co_C);

        // Bundled offer of NetOp and CP A
        BundledOffer bo1 = new BundledOffer(netOp, cp_A, 4, 1, false);

        // Bundled offer of NetOp and CP A, zero rated
        BundledOffer bo2 = new BundledOffer(netOp, cp_A, 6, 1, true);

        ArrayList<BundledOffer> bos = new ArrayList<>();
        bos.add(bo1);
        bos.add(bo2);

        // Create the combination of purchase options possible from these offers
        ArrayList<ConsumptionOption> options;
        options = ConsumptionOption.determineOptions(nm, netOffers, contentOffersIntegrated, contentOffersOther, bos);

        // Print out details about consumers
        System.out.println(c);

        // Print a list of all the possible combinations of consumption
        for (ConsumptionOption option : options) {
            System.out.println(option);
        }

        ConsumptionOptionSurplus cos = c.determineSurplusses(options);
        System.out.println(cos);


    }

}
