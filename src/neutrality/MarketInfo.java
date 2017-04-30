package neutrality;

import neutrality.cp.AbstractContentProvider;
import neutrality.cp.ContentProvider;
import neutrality.nsp.AbstractNetworkOperator;
import neutrality.nsp.NetworkOperator;

/**
 * Created by liara on 4/9/17.
 */
public class MarketInfo {

/**
 * Components/individual
 */
public final double nspNetworkInvestment;
public final double nspVideoInvestment;
public final double nspVideoPrice;
public final double nspUnbundledPrice;
public final double nspBundledPrice;
public final double nspBundlePremium;
public final double nspBandwidthPrice;
public final double nspIXCPrice;
public final double cpVideoInvestment;
public final double cpVideoPrice;
public final double cpOtherInvestment;
public final double cpOtherPrice;

public MarketInfo(NeutralityModel model, int step) {
  
  double tmp = 0; // Can just use one declaration
  
  // nspNetworkInvestment
  tmp = calcAverageNspInvestment(model, step);
  if (tmp == Double.NaN)
    nspNetworkInvestment = 0d;
  else
    nspNetworkInvestment = tmp;
    
  // nspVideoInvestment
  tmp = calcAverageNspVideoInvestment(model, step);
  if (tmp == Double.NaN)
    nspVideoInvestment = 0d;
  else
    nspVideoInvestment = tmp;
  
  // nspVideoPrice
  tmp = calcAverageNspVideoPrice(model, step);
  if (tmp == Double.NaN)
    nspVideoPrice = 0d;
  else
    nspVideoPrice = tmp;
  
  // nspUnbundledPrice
  tmp = calcAverageUnbundledPrice(model, step);
  if (tmp == Double.NaN)
    nspUnbundledPrice = 0d;
  else
    nspUnbundledPrice = tmp;
  
  // nspBundledPrice
  tmp = calcAverageBundledPrice(model, step);
  if (tmp == Double.NaN)
    nspBundledPrice = 0d;
  else
    nspBundledPrice = tmp;
  
  // nspIXCPrice
  tmp = calcAverageIXCPrice(model, step);
  if (tmp == Double.NaN)
    nspIXCPrice = 0d;
  else
    nspIXCPrice = tmp;
  
  // cpVideoInvestment
  tmp = calcAverageCpVideoInvestment(model, step);
  if (tmp == Double.NaN)
    cpVideoInvestment = 0d;
  else
    cpVideoInvestment = tmp;
  
  // cpVideoPrice
  tmp = calcAverageCpVideoPrice(model, step);
  if (tmp == Double.NaN)
    cpVideoPrice = 0d;
  else
    cpVideoPrice = tmp;
  
  // cpOtherInvestment
  tmp = calcAverageCpOtherInvestment(model, step);
  if (tmp == Double.NaN)
    cpOtherInvestment = 0d;
  else
    cpOtherInvestment = tmp;
  
  // cpOtherPrice
  tmp = calcAverageCpOtherPrice(model, step);
  if (tmp == Double.NaN)
    cpOtherPrice = 0d;
  else
    cpOtherPrice = tmp;
  
  // nspBandwidthPrice
  tmp = calcAverageNspBandwidthPrice(model, step);
  if (tmp == Double.NaN)
    nspBandwidthPrice = 0d;
  else
    nspBandwidthPrice = tmp;

  // Secondary values
  tmp = nspBundledPrice - nspUnbundledPrice;
  if (tmp == Double.NaN)
    nspBundlePremium = 0d;
  else
    nspBundlePremium = tmp;
}

private double calcAverageNspInvestment(NeutralityModel model, int step) {
  double totalInvestment = 0;
  for (NetworkOperator<?> no : model.networkOperators) {
    AbstractNetworkOperator<?> ano = (AbstractNetworkOperator<?>) no;
    totalInvestment += ano.Kn[step];
  }
  return totalInvestment / model.networkOperators.size();
}

private double calcAverageUnbundledPrice(NeutralityModel model, int step) {
  // This one needs to be scaled by revenue, because we don't record prices;
  double totalUnbundledRevenue = 0;
  double totalUnbundledSales = 0;
  for (NetworkOperator<?> no : model.networkOperators) {
    AbstractNetworkOperator<?> ano = (AbstractNetworkOperator<?>) no;
    totalUnbundledRevenue += ano.revNetwork[step];
    totalUnbundledSales += ano.qtyNetwork[step];
  }
  return totalUnbundledRevenue / totalUnbundledSales;
}

private double calcAverageBundledPrice(NeutralityModel model, int step) {
  // This one needs to be scaled by revenue, because we don't record prices;
  double totalBundledRevenue = 0;
  double totalBundledSales = 0;
  for (NetworkOperator<?> no : model.networkOperators) {
    AbstractNetworkOperator<?> ano = (AbstractNetworkOperator<?>) no;
    totalBundledRevenue += ano.revBundle[step];
    totalBundledSales += ano.qtyBundle[step];
  }
  return totalBundledRevenue / totalBundledSales;
}

private double calcAverageIXCPrice(NeutralityModel model, int step) {
  // This one needs to be scaled by revenue, because we don't record prices;
  double totalIxcRevenue = 0;
  double totalIxcSales = 0;
  for (NetworkOperator<?> no : model.networkOperators) {
    AbstractNetworkOperator<?> ano = (AbstractNetworkOperator<?>) no;
    // NSPs track this separately, by content type. combine for slightly
    // more accuracy.
    totalIxcRevenue += ano.revIxcOther[step] + ano.revIxcVideo[step];
    totalIxcSales += ano.qtyBandwidthOther[step] + ano.qtyBandwidthVideo[step];
  }
  return totalIxcRevenue / totalIxcSales;
}

private double calcAverageNspVideoInvestment(NeutralityModel model, int step) {
  double totalInvestment = 0;
  for (NetworkOperator<?> no : model.networkOperators) {
    AbstractNetworkOperator<?> ano = (AbstractNetworkOperator<?>) no;
    // Just like the nspInvestment function, but Ka here instead of Kn
    totalInvestment += ano.Ka[step];
  }
  return totalInvestment / model.networkOperators.size();
}

private double calcAverageNspVideoPrice(NeutralityModel model, int step) {
  // This one needs to be scaled by revenue, because we don't record prices;
  double totalVideoRevenue = 0;
  double totalVideoSales = 0;
  for (NetworkOperator<?> no : model.networkOperators) {
    AbstractNetworkOperator<?> ano = (AbstractNetworkOperator<?>) no;
    totalVideoRevenue += ano.revContent[step];
    totalVideoSales += ano.qtyContent[step];
  }
  return totalVideoRevenue / totalVideoSales;

}

private double calcAverageCpVideoInvestment(NeutralityModel model, int step) {
  double totalInvestment = 0;
  for (ContentProvider<?> cp : model.videoContentProviders) {
    AbstractContentProvider<?> acp = (AbstractContentProvider<?>) cp;
    totalInvestment += acp.Ka[step];
  }
  return totalInvestment / model.videoContentProviders.size();
}

private double calcAverageCpVideoPrice(NeutralityModel model, int step) {
  double totalRevenue = 0;
  double totalSales = 0;
  for (ContentProvider<?> cp : model.videoContentProviders) {
    AbstractContentProvider<?> acp = (AbstractContentProvider<?>) cp;
    totalRevenue += acp.revContent[step];
    totalSales += acp.qtyContent[step];
  }
  return totalRevenue / totalSales;
}

private double calcAverageCpOtherInvestment(NeutralityModel model, int step) {
  double totalInvestment = 0;
  for (ContentProvider<?> cp : model.otherContentProviders) {
    AbstractContentProvider<?> acp = (AbstractContentProvider<?>) cp;
    totalInvestment += acp.Ka[step];
  }
  return totalInvestment / model.otherContentProviders.size();
}

private double calcAverageCpOtherPrice(NeutralityModel model, int step) {
  double totalRevenue = 0;
  double totalSales = 0;
  for (ContentProvider<?> cp : model.otherContentProviders) {
    AbstractContentProvider<?> acp = (AbstractContentProvider<?>) cp;
    totalRevenue += acp.revContent[step];
    totalSales += acp.qtyContent[step];
  }
  return totalRevenue / totalSales;
}

private double calcAverageNspBandwidthPrice(NeutralityModel model, int step) {
  double revBW = 0d;
  double qtyBW = 0d;

  for (NetworkOperator<?> no : model.networkOperators) {
    AbstractNetworkOperator<?> ano = (AbstractNetworkOperator<?>) no;
    revBW += ano.revBandwidthOther[step];
    revBW += ano.revBandwidthVideo[step];
    qtyBW += ano.qtyBandwidthOther[step];
    qtyBW += ano.qtyBandwidthVideo[step];
  }

  return revBW / qtyBW;
}

}
