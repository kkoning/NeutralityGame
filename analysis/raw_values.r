require(ggplot2)
require(data.table)
source("http://home.kkoning.net/misc/multiplot.r")

png_width <- 3840
png_height <- 2160
heat_bins <- 200

args <- commandArgs(TRUE)
filename <- as.character(args[1])
num_loci <- as.integer(args[2])
output_prefix <- as.character(args[3])
labels_file <- as.character(args[4])

data <- read.csv(filename, header=TRUE)
data <- as.data.table(data)
source(labels_file)

colnames <- c()
for (i in 1:num_loci) {
  colnames[[i]] <- paste("loc",i-1,sep="_")
}

# Protected mean, for non-numeric fields.
prot_mean <- function(x, trim = 0, na.rm = FALSE, ...) {
  if (is.numeric(x))
    return(mean(x=x,trim=trim,na.rm=na.rm, ...))
  else
    return(NaN)
}

summary <- data[, lapply(.SD, prot_mean, na.rm=TRUE), by=generation]

raw_plots <- c()

raw_plots[[1]] <- ggplot(data,aes_string(x = "generation", y = 'fitness')) + 
        stat_bin2d(bins=heat_bins) + 
        scale_fill_gradientn(colours=rainbow(5), trans="log") +
        ggtitle("Distribution of Fitness")

# ggplot(summary,aes_string(x = "generation", y = 'fitness')) + 
#   geom_point(shape=1) +
#   geom_smooth(method=loess) + 
#   ggtitle("Fitness over Time") + 
#   ylab("mean(fitness)")

raw_plots[[2]] <- ggplot(data,aes(x = generation, y = log(fitness))) + 
        stat_bin2d(bins=heat_bins) + 
        scale_fill_gradientn(colours=rainbow(5), trans="log") +
        ggtitle("Distribution of log(Fitness)")

# ggplot(summary,aes(x=generation,y=log(fitness))) + 
#   geom_point(shape=1) +
#   geom_smooth(method=loess) + 
#   ggtitle("Log Fitness over Time") +
#   ylab("log(mean(fitness))")

plot_index <- 3
for (colname in colnames) {
  raw_plots[[plot_index]] <- ggplot(data,aes_string(x = "generation", y = colname)) + 
        stat_bin2d(bins=heat_bins) + 
        scale_fill_gradientn(colours=rainbow(5), trans="log") +
        ggtitle(sprintf('Distribution of %s', genome_labels[plot_index-2]))

  # ggplot(summary,aes_string(x = "generation", y = colname)) + 
  #   geom_point(shape=1) +
  #   geom_smooth(method=loess) + 
  #   ggtitle(genome_labels[[plot_index-2]]) +     
  #   ylab(sprintf("mean(%s)", genome_labels[[plot_index-2]]))

  plot_index <- plot_index + 1
}

num_cols <- as.integer(num_loci^0.5) + 1

png(paste(output_prefix, "_values.png", sep=''), height=png_height, width=png_width, res=(300/(num_cols/2)))
multiplot(plotlist=raw_plots,cols=num_cols)
dev.off()
