package neutrality.cp;

import agency.Account;
import agency.Agent;
import agency.Individual;
import neutrality.ConsumptionOption;
import neutrality.MarketInfo;
import neutrality.NeutralityModel;
import neutrality.Offers;

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

boolean isVideoProvider();

void goBankrupt();

double getContentData(ContentData data);

default double getSectorInvestment(MarketInfo mi) {
  if (isVideo()) {
    return mi.cpVideoInvestment;
  } else {
    return mi.cpOtherInvestment;
  }
}

default double getSectorPrice(MarketInfo mi) {
  if (isVideo()) {
    return mi.cpVideoPrice;
  } else {
    return mi.cpOtherPrice;
  }
}


enum ContentData {
  QUANTITY,
  REVENUE,
  INVESTMENT,
  BALANCE
}

}
