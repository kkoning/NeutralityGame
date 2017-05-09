package neutrality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestConsumers {

Consumers               simpleVideoOnly;
List<ConsumptionOption> simpleVideoOptions;

@Before
public void setUp() {
  simpleVideoOnly = new Consumers(10000, 0.3, 0.4, 0.4, 1, 0, 1, System.out);
  simpleVideoOptions = new ArrayList<>();
  simpleVideoOptions.add(ConsumptionOption.getSyntheticConsumptionOption(29.6, 615.34, 0, 23.17));
  simpleVideoOptions.add(ConsumptionOption.getSyntheticConsumptionOption(29.6, 596.16, 0, 27.05));

}

@Test
public void testDemand() {

  double[] result = simpleVideoOnly.determineConsumption(simpleVideoOptions);
  System.out.println(Arrays.toString(result));

}

}
