package neutrality;

import static agency.util.Statistics.HHI;

import java.util.ArrayList;
import java.util.List;

/**
 * This object gets translated directly into column headers and data values in
 * output files. For this reason, it needs to be flattened and not hierarchical.
 * Also, it will necessarily be somewhat repetitive because fields from other
 * object cannot be re-used directly.
 * <p>
 * Notes to self:
 * <p>
 * (1) Use first class types to distinguish between null and default values. (2)
 * Don't add values here faster than writing the code that calculates them, this
 * helps to make sure nothing gets missed! ;)
 */
public class OutputData {
/*
 * Consumption Variables
 */
double consumerVideoUtility;
double consumerVideoPaid;

double consumerOtherUtility;
double consumerOtherPaid;

double consumerBothUtility;
double consumerBothPaid;

double consumerUtility;
double consumerPaid;

/*
 * Network Operator Variables
 */
double unbundledPrice;
double unbundledQty;
double unbundledRev;

double bundlePrice;
double bundleQty;
double bundleRev;

double nspContentPrice;
double nspContentQty;
double nspContentRev;

double videoBandwidthPrice;
double videoBandwidthQty;
double videoBandwidthRev;

double otherBandwidthPrice;
double otherBandwidthQty;
double otherBandwidthRev;

double zeroRatingDiscounts;

double ixcVideoPrice;
double ixcVideoQty;
double ixcVideoRev;

double ixcOtherPrice;
double ixcOtherQty;
double ixcOtherRev;

double ixcAvoided;

double nspKn;  
double nspKa;
double nspBalance; 

/*
 * Content Provider Variables
 */
double videoKa;
double videoPrice;
double videoQty;
double videoRev;
double videoBalance;

double otherKa;
double otherPrice;
double otherQty;
double otherRev;
double otherBalance;


/*
 * Market variables
 */
double hhiNetwork;
double hhiVideo;
double hhiOther;


public OutputData(NeutralityModel model) {
  calculateConsumerStats(model);
  calculateNSPStats(model);
  calculateCPStats(model);
  calculateHHIs(model);
}

public void calculateConsumerStats(NeutralityModel model) {
  consumerVideoUtility = model.consumersVideo.accumulatedUtility;
  consumerVideoPaid = model.consumersVideo.accumulatedCost;

  consumerOtherUtility = model.consumersOther.accumulatedUtility;
  consumerOtherPaid = model.consumersOther.accumulatedCost;

  consumerBothUtility = model.consumersBoth.accumulatedUtility;
  consumerBothPaid = model.consumersBoth.accumulatedCost;
  
  consumerUtility = consumerVideoUtility + consumerOtherUtility + consumerBothUtility;
  consumerPaid = consumerVideoPaid + consumerOtherPaid + consumerBothPaid;
}

public void calculateCPStats(NeutralityModel model) {
  for (ContentProvider vcp : model.videoContentProviders) {
    videoKa += vcp.Ka;
    videoBalance += vcp.getBalance();
    
    videoQty += vcp.content.qty;
    videoRev += vcp.content.revenue();
  }
  
  for (ContentProvider ocp : model.otherContentProviders) {
    otherKa += ocp.Ka;
    otherBalance += ocp.getBalance();
    
    otherQty += ocp.content.qty;
    otherRev += ocp.content.revenue();
  }
  
  videoPrice = videoRev / (videoQty + Double.MIN_NORMAL);
  otherPrice = otherRev / (otherQty + Double.MIN_NORMAL);
  
}

public void calculateNSPStats(NeutralityModel model) {
  for (NetworkOperator no : model.networkOperators) {
    unbundledQty += no.netOnly.qty;
    unbundledRev += no.netOnly.revenue();
    
    bundleQty += no.bundle.qty;
    bundleRev += no.bundle.revenue();
    
    nspContentQty += no.content.qty;
    nspContentRev += no.content.revenue();
    
    videoBandwidthQty += no.videoBW.qty;
    videoBandwidthRev += no.videoBW.revenue();
    
    otherBandwidthQty += no.otherBW.qty;
    otherBandwidthRev += no.otherBW.revenue();

    zeroRatingDiscounts += no.zeroRatingDiscounts;

    ixcVideoQty += no.videoIXC.qty;
    ixcVideoRev += no.videoIXC.revenue();
    
    ixcOtherQty += no.otherIXC.qty;
    ixcOtherRev += no.otherIXC.revenue();

    ixcAvoided += no.ixcAvoided;
    
    nspKn += no.Kn;
    nspKa += no.Ka;
    nspBalance += no.getBalance();
  }
  
  unbundledPrice = unbundledRev / (unbundledQty + Double.MIN_NORMAL);
  bundlePrice = bundleRev / (bundleQty + Double.MIN_NORMAL);
  nspContentPrice = nspContentRev / (nspContentQty + Double.MIN_NORMAL);
  
  videoBandwidthPrice = videoBandwidthRev / (videoBandwidthQty + Double.MIN_NORMAL);
  otherBandwidthPrice = otherBandwidthRev / (otherBandwidthQty + Double.MIN_NORMAL);
  
  ixcVideoPrice = ixcVideoRev / (ixcVideoQty + Double.MIN_NORMAL);
  ixcOtherPrice = ixcOtherRev / (ixcOtherQty + Double.MIN_NORMAL);
}

public void calculateHHIs(NeutralityModel model) {

  /*
   * Market Information
   */
  // Network HHI
  double[] networkSales = new double[model.networkOperators.size()];
  for (int i = 0; i < model.networkOperators.size(); i++) {
    NetworkOperator no = model.networkOperators.get(i);
    networkSales[i] = no.netOnly.qty +
                      no.bundle.qty + Double.MIN_NORMAL;
  }
  hhiNetwork = HHI(networkSales);
  if (Double.isNaN(hhiNetwork) || Double.isInfinite(hhiNetwork))
    hhiNetwork = 0;

  
  // Video Content HHI
  List<Double> videoSales = new ArrayList<>();
  for (ContentProvider cp : model.videoContentProviders) {
    videoSales.add(cp.content.qty + Double.MIN_NORMAL);
  }
  if (model.policyNSPContentAllowed) {
    for (NetworkOperator no : model.networkOperators) {
      videoSales.add(no.content.qty +
                     no.bundle.qty + Double.MIN_NORMAL);
    }
  }
  hhiVideo = HHI(videoSales);
  if (Double.isNaN(hhiVideo) || Double.isInfinite(hhiVideo))
    hhiVideo = 0;

  
  // Other Content HHI
  double[] otherSales = new double[model.otherContentProviders.size()];
  for (int i = 0; i < model.otherContentProviders.size(); i++) {
    ContentProvider ocp = model.otherContentProviders.get(i);
    otherSales[i] = ocp.content.qty;
  }
  hhiOther = HHI(otherSales);
  if (Double.isNaN(hhiOther) || Double.isInfinite(hhiOther))
    hhiOther = 0;
  }





}
