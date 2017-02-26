package neutrality;

import agency.Account;
import agency.Agent;
import agency.Individual;

/**
 * Created by liara on 2/9/17.
 */
public interface ContentProvider<N extends Individual>
        extends Agent<N, NeutralityModel> {

boolean isVideo();

void setVideo(boolean video);

double getKa(int step);

void processContentConsumption(int step, ConsumptionOption co, double qty);

NeutralityModel getModel();

Offers.ContentOffer getContentOffer(int step);

void trackIXCFees(int step, double price, double qty);

Account getAccount();

boolean isBankrupt();

void goBankrupt();

double getContentData(ContentData data);

enum ContentData {
  QUANTITY,
  REVENUE,
  INVESTMENT,
  BALANCE
}

}
