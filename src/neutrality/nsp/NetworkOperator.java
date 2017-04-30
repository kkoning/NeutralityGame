package neutrality.nsp;

import agency.Individual;
import neutrality.ConsumptionOption;
import neutrality.Offers;
import neutrality.Offers.ContentOffer;
import neutrality.Offers.NetworkAndVideoBundleOffer;
import neutrality.Offers.NetworkOnlyOffer;
import neutrality.cp.ContentProvider;

/**
 * Created by liara on 2/9/17.
 */
public interface NetworkOperator<N extends Individual>
        extends ContentProvider<N> {

double getKn(int step);

void processNetworkConsumption(int step, ConsumptionOption co, double qty);

double getIXCPrice(int step);

void trackIXC(int step, double price, double qty, boolean video);

Offers.NetworkOnlyOffer getNetworkOffer(int step);

Offers.ContentOffer getContentOffer(int step);

Offers.NetworkAndVideoBundleOffer getBundledOffer(int step);

double getNetOpData(NetOpData variable);

enum NetOpData {
  QUANTITY_NETWORK,
  REVENUE_NETWORK,
  QUANTITY_BUNDLE,
  REVENUE_BUNDLE,
  QUANTITY_IXC_VIDEO,
  REVENUE_IXC_VIDEO,
  QUANTITY_IXC_OTHER,
  REVENUE_IXC_OTHER,
  INVESTMENT_NETWORK,
}

}
