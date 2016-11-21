package neutrality;

import org.junit.Test;

/**
 * Created by liara on 11/20/16.
 */
public class TestParameters {
/**
 * Test that the alpha parameter is correctly translated into relative
 * valuations.  Verified working 2016-11-21
 */
@Test
public void describeAlpha() {
  double[] alphaValues = {0.1, 0.2, 0.5, 1, 2, 5, 10};
  for (double val : alphaValues) {
    describeAlpha(val);
  }
}

/**
 * Test that the beta parameter is correctly translated into relative
 * bandwidth intensities.  Verified working 2016-11-21
 */
@Test
public void describeBeta() {
  double[] betaValues = {0.1, 0.2, 0.5, 1, 2, 5, 10};
  for (double val : betaValues) {
    describeBeta(val);
  }
}


private void describeAlpha(double value) {
  System.out.println("When alpha=" + value);
  NeutralityModel model = new NeutralityModel();
  model.alpha = value;
  System.out.println("Video Content Value = " + model.getVideoContentValue());
  System.out.println("Other Content Value = " + model.getOtherContentValue());
}

private void describeBeta(double value) {
  System.out.println("When beta=" + value);
  NeutralityModel model = new NeutralityModel();
  model.beta = value;
  System.out.println("Video Content Bandwidth = " + model.getVideoBWIntensity());
  System.out.println("Other Content Bandwidth = " + model.getOtherBWIntensity());
}


}
