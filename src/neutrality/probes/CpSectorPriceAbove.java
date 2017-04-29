package neutrality.probes;

import agency.Agent;
import neutrality.ContentProvider;
import neutrality.MarketInfo;
import neutrality.NeutralityModel;

import static agency.util.Misc.BUG;

/**
 Created by liara on 4/19/17.
 */
public class CpSectorPriceAbove implements EnvironmentalContingency {

  EnvironmentalContingency ec = null;

@Override
public boolean conditionMet(NeutralityModel model, Agent agent, MarketInfo mi, Number parameter) {
  if (ec != null)
    return ec.conditionMet(model,agent,mi,parameter);

  try {
    ContentProvider cp = (ContentProvider) agent;
    if (cp.isVideo())
      ec = new CpVideoPriceAbove();
    else
      ec = new CpOtherPriceAbove();

    return ec.conditionMet(model,agent,mi,parameter);
  } catch (Exception e) {
    BUG(e.getMessage());
  }

  // Should be unreachable
  return false;
}

}
