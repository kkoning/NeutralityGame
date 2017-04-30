package neutrality.probes;

import agency.Agent;
import neutrality.MarketInfo;
import neutrality.NeutralityModel;
import neutrality.cp.ContentProvider;

import static agency.util.Misc.BUG;

/**
 * Created by liara on 4/19/17.
 */
public class CpSectorInvestmentAbove implements EnvironmentalContingency {

EnvironmentalContingency ec = null;

@Override
public boolean conditionMet(NeutralityModel model,
                            Agent<?, NeutralityModel> agent,
                            MarketInfo mi,
                            Number parameter) {
  if (ec != null)
    return ec.conditionMet(model, agent, mi, parameter);

  try {
    ContentProvider<?> cp = (ContentProvider<?>) agent;
    if (cp.isVideo())
      ec = new CpVideoInvestmentAbove();
    else
      ec = new CpOtherInvestmentAbove();

    return ec.conditionMet(model, agent, mi, parameter);
  } catch (Exception e) {
    BUG(e.getMessage());
  }

  // Should be unreachable
  return false;
}

}
