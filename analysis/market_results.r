require(ggplot2)
require(data.table)
source("http://home.kkoning.net/misc/multiplot.r")

png_width <- 3840
png_height <- 2160
heat_bins <- 200

# Protected mean, for non-numeric fields.
prot_mean <- function(x, trim = 0, na.rm = FALSE, ...) {
  if (is.numeric(x))
    return(mean(x=x,trim=trim,na.rm=na.rm, ...))
  else
    return(NaN)
}

make_plot_list <- function(colnames) {
  plot_list = list()
  plot_num <- 1
  for (colname in colnames) {
    plot_list[[plot_num]] = ggplot(summary,aes_string(x = "generation", y = colname)) + 
      geom_point(shape=1) +
      geom_smooth(method=loess) + 
      ggtitle(sprintf('Mean of %s', colname))
    plot_num <- plot_num + 1
  }
  return(plot_list)
}

make_plot_list_heat <- function(colnames, numGenerations) {
  plot_list = list()
  plot_num <- 1
  for (colname in colnames) {
    if (min(models[,get(colname)], na.rm=TRUE) == max(models[,get(colname)], na.rm=TRUE)) {
      plot_list[[plot_num]] = ggplot(summary,aes_string(x = "generation", y = colname)) + 
        geom_point(shape=1) +
        geom_smooth(method=loess) + 
        ggtitle(sprintf('Value of %s', colname))
    } else {
      plot_list[[plot_num]] = ggplot(models,aes_string(x = "generation", y = colname)) + 
        stat_bin2d(bins=heat_bins) + 
        scale_fill_gradientn(colours=rainbow(5), trans="log") +
        ggtitle(sprintf('Distribution of %s', colname))
    }
    plot_num <- plot_num + 1
  }
  return(plot_list)
}


# requres revision if data output format changes.
models <- read.csv('summary.csv', header=TRUE, colClasses = c("integer", "character", rep("numeric",35)))
models <- data.table(models)
summary(models)


colnames <- c(
'hhiNetwork',
'hhiOther',
'hhiVideo',
'nspProfit',
'nspBankruptcies',
'nspKa',
'nspKn',
'nspPriceBundle',
'nspPriceNetworkOnly',
'nspPriceVideoOnly',
'nspRevBundle',
'nspRevOtherBW',
'nspRevVideoBW',
'nspRevIxcOther',
'nspRevIxcVideo',
'nspRevNetworkOnly',
'nspRevVideoOnly',
'ocpProfit',
'ocpBankruptcies',
'ocpKa',
'ocpP',
'ocpRev',
'utilityBoth',
'utilityOtherOnly',
'utilityVideoOnly',
'utilityPerCostBoth',
'utilityPerCostOtherOnly',
'utilityPerCostVideoOnly',
'vcpProfit',
'vcpBankruptcies',
'vcpKa',
'vcpP',
'vcpRev'
)

summary <- models[, lapply(.SD, prot_mean, na.rm=TRUE), by=generation, .SDcols=colnames ]
numGenerations <- max(summary$generation)

nsp_cols = c(
'nspProfit',
'nspBankruptcies',
'nspKa',
'nspKn',
'nspPriceNetworkOnly',
'nspRevNetworkOnly',
'nspPriceVideoOnly',
'nspRevVideoOnly',
'nspPriceBundle',
'nspRevBundle',
'nspRevOtherBW',
'nspRevVideoBW',
'nspRevIxcOther',
'nspRevIxcVideo'
)

cp_cols = c(
'ocpProfit',
'ocpBankruptcies',
'ocpKa',
'ocpP',
'ocpRev',
'vcpProfit',
'vcpBankruptcies',
'vcpKa',
'vcpP',
'vcpRev')

other_cols = c(
'hhiNetwork',
'hhiOther',
'hhiVideo',
'utilityBoth',
'utilityOtherOnly',
'utilityVideoOnly',
'utilityPerCostBoth',
'utilityPerCostOtherOnly',
'utilityPerCostVideoOnly'
)

# Investments

plots <- make_plot_list_heat(nsp_cols)
num_cols <- as.integer(length(plots)^0.5 - 0.1) + 1
png("nsp_results.png", height=png_height, width=png_width, res=(300/(num_cols/2)))
multiplot(plotlist=plots,cols=num_cols)
dev.off()


plots <- make_plot_list_heat(cp_cols)
num_cols <- as.integer(length(plots)^0.5 - 0.1) + 1
png("cp_results.png", height=png_height, width=png_width, res=(300/(num_cols/2)))
multiplot(plotlist=plots,cols=num_cols)
dev.off()

plots <- make_plot_list_heat(other_cols)
num_cols <- as.integer(length(plots)^0.5 - 0.1) + 1
png("other.png", height=png_height, width=png_width, res=(300/(num_cols/2)))
multiplot(plotlist=plots,cols=num_cols)
dev.off()



