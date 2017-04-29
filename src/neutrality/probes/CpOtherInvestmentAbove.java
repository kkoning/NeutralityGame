package neutrality.probes;

import agency.Agent;
import neutrality.MarketInfo;
import neutrality.NeutralityModel;

/**
 Created by liara on 4/19/17.
 */
public class CpOtherInvestmentAbove implements EnvironmentalContingency {

@Override
public boolean conditionMet(NeutralityModel model,
                            Agent agent,
                            MarketInfo mi,
                            Number parameter) {
  return (mi.getCpOtherInvestment() > parameter.doubleValue());
}

}
