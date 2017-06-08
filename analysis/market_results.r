require(ggplot2)
require(data.table)
source("http://home.kkoning.net/misc/multiplot.r")

png_width <- 3840
png_height <- 2160
heat_bins <- 200

fmt_dcimals <- function(decimals=0){
   # return a function responpsible for formatting the 
   # axis labels with a given number of decimals 
   function(x) as.character(round(x,decimals))
}
# requres revision if data output format changes.
models <- read.csv('summary.csv', header=TRUE)
models <- data.table(models)
summary(models)

colnames <- c(
'generation',
'bundlePrice',
'bundleQty',
'bundleRev',
'consumerPaid',
'consumerUtility',
'hhiNetwork',
'hhiOther',
'hhiVideo',
'ixcAvoided',
'ixcOtherPrice',
'ixcOtherQty',
'ixcOtherRev',
'ixcVideoPrice',
'ixcVideoQty',
'ixcVideoRev',
'nspBalance',
'nspContentPrice',
'nspContentQty',
'nspContentRev',
'nspKa',
'nspKn',
'otherBalance',
'otherBandwidthPrice',
'otherBandwidthQty',
'otherBandwidthRev',
'otherKa',
'otherPrice',
'otherQty',
'otherRev',
'unbundledPrice',
'unbundledQty',
'unbundledRev',
'videoBalance',
'videoBandwidthPrice',
'videoBandwidthQty',
'videoBandwidthRev',
'videoKa',
'videoPrice',
'videoQty',
'videoRev',
'zeroRatingDiscounts'
)

#
# Diagnostic Plots
#
# diagnostics <- c(
# 'nspBalance',
# 'otherBalance',
# 'videoBalance',
# 'consumerPaid'
# )

diagnostic_plots = list()
plot_num <- 1

    diagnostic_plots[[plot_num]] = ggplot(models,aes(x = generation, y = nspBalance)) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of nspBalance')
    plot_num <- plot_num + 1

    diagnostic_plots[[plot_num]] = ggplot(models,aes(x = generation, y = otherBalance)) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of otherBalance')
    plot_num <- plot_num + 1

    diagnostic_plots[[plot_num]] = ggplot(models,aes(x = generation, y = videoBalance)) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of videoBalance')
    plot_num <- plot_num + 1

    diagnostic_plots[[plot_num]] = ggplot(models,aes(x = generation, y = consumerPaid)) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of consumerPaid')
    plot_num <- plot_num + 1

num_cols <- as.integer(length(diagnostic_plots)^0.5 - 0.1) + 1
png("diagnostics.png", height=png_height, width=png_width, res=(300/(num_cols/2)))
multiplot(plotlist=diagnostic_plots,cols=num_cols)
dev.off()

#
# Sales Plots
#

sales_plots = list()
plot_num <- 1
# 'unbundledPrice',
# 'unbundledQty',
# 'unbundledRev',

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(unbundledPrice))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(unbundledPrice)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(unbundledQty))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(unbundledQty)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(unbundledRev))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(unbundledRev)')
    plot_num <- plot_num + 1

# 'bundlePrice',
# 'bundleQty',
# 'bundleRev',

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(bundlePrice))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(bundlePrice)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(bundleQty))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(bundleQty)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(bundleRev))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(bundleRev)')
    plot_num <- plot_num + 1

# 'videoBandwidthPrice',
# 'videoBandwidthQty',
# 'videoBandwidthRev',
 
    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(videoBandwidthPrice))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(videoBandwidthPrice)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(videoBandwidthQty))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(videoBandwidthQty)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(videoBandwidthRev))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(videoBandwidthRev)')
    plot_num <- plot_num + 1

# 'otherBandwidthPrice',
# 'otherBandwidthQty',
# 'otherBandwidthRev',

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(otherBandwidthPrice))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(otherBandwidthPrice)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(otherBandwidthQty))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(otherBandwidthQty)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(otherBandwidthRev))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(otherBandwidthRev)')
    plot_num <- plot_num + 1

# 'videoPrice',
# 'videoQty',
# 'videoRev',

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(videoPrice))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(videoPrice)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(videoQty))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(videoQty)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(videoRev))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(videoRev)')
    plot_num <- plot_num + 1


# 'otherBandwidthPrice',
# 'otherBandwidthQty',
# 'otherBandwidthRev'

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(otherBandwidthPrice))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(otherBandwidthPrice)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(otherBandwidthQty))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(otherBandwidthQty)')
    plot_num <- plot_num + 1

    sales_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(otherBandwidthRev))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(otherBandwidthRev)')
    plot_num <- plot_num + 1

num_cols <- as.integer(length(sales_plots)^0.5 - 0.1) + 1
png("sales.png", height=png_height, width=png_width, res=(300/(num_cols/2)))
multiplot(plotlist=sales_plots,cols=num_cols)
dev.off()


#
# Market Plots
#
market_plots = list()
plot_num <- 1

# 'nspKn',
# 'nspKa',
# 'videoKa',
# 'otherKa',
    market_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(nspKn))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(nspKn)')
    plot_num <- plot_num + 1

    market_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(nspKa))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(nspKa)')
    plot_num <- plot_num + 1

    market_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(videoKa))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(videoKa)')
    plot_num <- plot_num + 1

    market_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(otherKa))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(otherKa)')
    plot_num <- plot_num + 1

# 'hhiNetwork',
# 'hhiOther',
# 'hhiVideo',
    market_plots[[plot_num]] = ggplot(models,aes(x = generation, y = hhiNetwork)) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of hhiNetwork')
    plot_num <- plot_num + 1

    market_plots[[plot_num]] = ggplot(models,aes(x = generation, y = hhiOther)) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of hhiOther')
    plot_num <- plot_num + 1

    market_plots[[plot_num]] = ggplot(models,aes(x = generation, y = hhiVideo)) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of hhiVideo')
    plot_num <- plot_num + 1


# 'consumerUtility',
# 'zeroRatingDiscounts'
# 'ixcAvoided',
    market_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(consumerUtility))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(consumerUtility)')
    plot_num <- plot_num + 1

    market_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(zeroRatingDiscounts))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(zeroRatingDiscounts)')
    plot_num <- plot_num + 1

    market_plots[[plot_num]] = ggplot(models,aes(x = generation, y = log(ixcAvoided))) + 
      stat_bin2d(bins=heat_bins) + 
      scale_fill_gradientn(colours=rainbow(5), trans="log", labels = fmt_dcimals(0)) +
      ggtitle('Distribution of log(ixcAvoided)')
    plot_num <- plot_num + 1

num_cols <- as.integer(length(market_plots)^0.5 - 0.1) + 1
png("market.png", height=png_height, width=png_width, res=(300/(num_cols/2)))
multiplot(plotlist=market_plots,cols=num_cols)
dev.off()


