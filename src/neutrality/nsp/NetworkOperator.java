package neutrality.nsp;

import agency.Individual;
import neutrality.ConsumptionOption;
import neutrality.Offers;
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

double totQtyBundle();

double totQtyNetworkOnly();

double totRevNetworkOnly();

double totRevBundle();

double totQtyIxcVideo();

double totRevIxcVideo();

double totQtyIxcOther();

double totRevIxcOther();

double totKn();

double totRevVideoBW();

double totRevOtherBW();

}
