package neutrality;

/**
 Created by liara on 4/9/17.
 */
public class MarketInfo {

/**
 Components/individual
 */
private final double nspNetworkInvestment;
private final double nspVideoInvestment;
private final double nspVideoPrice;
private final double nspUnbundledPrice;
private final double nspBundledPrice;
private final double nspBundlePremium;
private final double nspIXCPrice;
private final double cpVideoInvestment;
private final double cpVideoPrice;
private final double cpOtherInvestment;
private final double cpOtherPrice;

public MarketInfo(NeutralityModel model, int step) {
  nspNetworkInvestment = calcAverageNspInvestment(model,step);
  nspVideoInvestment = calcAverageNspVideoInvestment(model,step);
  nspVideoPrice = calcAverageNspVideoPrice(model,step);
  nspUnbundledPrice = calcAverageUnbundledPrice(model, step);
  nspBundledPrice = calcAverageBundledPrice(model,step);
  nspIXCPrice = calcAverageIXCPrice(model,step);
  cpVideoInvestment = calcAverageCpVideoInvestment(model,step);
  cpVideoPrice = calcAverageCpVideoPrice(model,step);
  cpOtherInvestment = calcAverageCpOtherInvestment(model,step);
  cpOtherPrice = calcAverageCpOtherPrice(model,step);

  // Secondary values
  nspBundlePremium = nspBundledPrice - nspUnbundledPrice;

}

public double getNspNetworkInvestment() {
  if (Double.isNaN(nspNetworkInvestment))
    return 0;
  else
    return nspNetworkInvestment;
}

public double getNspVideoInvestment() {
  if (Double.isNaN(nspVideoInvestment))
    return 0;
  else
    return nspVideoInvestment;
}

public double getNspVideoPrice() {
  if (Double.isNaN(nspVideoPrice))
    return 0;
  else
    return nspVideoPrice;
}

public double getNspUnbundledPrice() {
  if (Double.isNaN(nspUnbundledPrice))
    return 0;
  else
    return nspUnbundledPrice;
}

public double getNspBundledPrice() {
  if (Double.isNaN(nspBundledPrice))
    return 0;
  else
    return nspBundledPrice;
}

public double getNspBundlePremium() {
  if (Double.isNaN(nspBundlePremium))
    return 0;
  else
    return nspBundlePremium;
}

public double getNspIXCPrice() {
  if (Double.isNaN(nspIXCPrice))
    return 0;
  else
    return nspIXCPrice;
}

public double getCpVideoInvestment() {
  if (Double.isNaN(cpVideoInvestment))
    return 0;
  else
    return cpVideoInvestment;
}

public double getCpVideoPrice() {
  if (Double.isNaN(cpVideoPrice))
    return 0;
  else
    return cpVideoPrice;
}

public double getCpOtherInvestment() {
  if (Double.isNaN(cpOtherInvestment))
    return 0;
  else
    return cpOtherInvestment;
}

public double getCpOtherPrice() {
  if (Double.isNaN(cpOtherPrice))
    return 0;
  else
    return cpOtherPrice;
}

private double calcAverageNspInvestment(NeutralityModel model, int step) {
  double totalInvestment = 0;
  for (NetworkOperator no : model.networkOperators) {
    AbstractNetworkOperator ano = (AbstractNetworkOperator) no;
    totalInvestment += ano.Kn[step];
  }
  return totalInvestment / model.networkOperators.size();
}

private double calcAverageUnbundledPrice(NeutralityModel model, int step) {
  // This one needs to be scaled by revenue, because we don't record prices;
  double totalUnbundledRevenue = 0;
  double totalUnbundledSales = 0;
  for (NetworkOperator no : model.networkOperators) {
    AbstractNetworkOperator ano = (AbstractNetworkOperator) no;
    totalUnbundledRevenue += ano.revNetwork[step];
    totalUnbundledSales += ano.qtyNetwork[step];
  }
  return totalUnbundledRevenue / totalUnbundledSales;
}

private double calcAverageBundledPrice(NeutralityModel model, int step) {
  // This one needs to be scaled by revenue, because we don't record prices;
  double totalBundledRevenue = 0;
  double totalBundledSales = 0;
  for (NetworkOperator no : model.networkOperators) {
    AbstractNetworkOperator ano = (AbstractNetworkOperator) no;
    totalBundledRevenue += ano.revBundle[step];
    totalBundledSales += ano.qtyBundle[step];
  }
  return totalBundledRevenue / totalBundledSales;
}

private double calcAverageIXCPrice(NeutralityModel model, int step) {
  // This one needs to be scaled by revenue, because we don't record prices;
  double totalIxcRevenue = 0;
  double totalIxcSales = 0;
  for (NetworkOperator no : model.networkOperators) {
    AbstractNetworkOperator ano = (AbstractNetworkOperator) no;
    // NSPs track this separately, by content type.  combine for slightly
    // more accuracy.
    totalIxcRevenue += ano.revIxcOther[step] + ano.revIxcVideo[step];
    totalIxcSales += ano.qtyBandwidthOther[step] + ano.qtyBandwidthVideo[step];
  }
  return totalIxcRevenue / totalIxcSales;
}

private double calcAverageNspVideoInvestment(NeutralityModel model, int step) {
  double totalInvestment = 0;
  for (NetworkOperator no : model.networkOperators) {
    AbstractNetworkOperator ano = (AbstractNetworkOperator) no;
    // Just like the nspInvestment function, but Ka here instead of Kn
    totalInvestment += ano.Ka[step];
  }
  return totalInvestment / model.networkOperators.size();
}


private double calcAverageNspVideoPrice(NeutralityModel model, int step) {
  // This one needs to be scaled by revenue, because we don't record prices;
  double totalVideoRevenue = 0;
  double totalVideoSales = 0;
  for (NetworkOperator no : model.networkOperators) {
    AbstractNetworkOperator ano = (AbstractNetworkOperator) no;
    totalVideoRevenue += ano.revContent[step];
    totalVideoSales += ano.qtyContent[step];
  }
  return totalVideoRevenue / totalVideoSales;

}

private double calcAverageCpVideoInvestment(NeutralityModel model, int step) {
  double totalInvestment = 0;
  for (ContentProvider cp : model.videoContentProviders) {
    AbstractContentProvider acp = (AbstractContentProvider) cp;
    totalInvestment += acp.Ka[step];
  }
  return totalInvestment / model.videoContentProviders.size();
}

private double calcAverageCpVideoPrice(NeutralityModel model, int step) {
  double totalRevenue = 0;
  double totalSales = 0;
  for (ContentProvider cp : model.videoContentProviders) {
    AbstractContentProvider acp = (AbstractContentProvider) cp;
    totalRevenue += acp.revContent[step];
    totalSales += acp.qtyContent[step];
  }
  return totalRevenue / totalSales;
}

private double calcAverageCpOtherInvestment(NeutralityModel model, int step) {
  double totalInvestment = 0;
  for (ContentProvider cp : model.otherContentProviders) {
    AbstractContentProvider acp = (AbstractContentProvider) cp;
    totalInvestment += acp.Ka[step];
  }
  return totalInvestment / model.otherContentProviders.size();
}

private double calcAverageCpOtherPrice(NeutralityModel model, int step) {
  double totalRevenue = 0;
  double totalSales = 0;
  for (ContentProvider cp : model.otherContentProviders) {
    AbstractContentProvider acp = (AbstractContentProvider) cp;
    totalRevenue += acp.revContent[step];
    totalSales += acp.qtyContent[step];
  }
  return totalRevenue / totalSales;
}



}
