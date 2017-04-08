require(ggplot2)
require(data.table)
require(broom)
source("http://home.kkoning.net/misc/multiplot.r")

png_width <- 3840
png_height <- 2160


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

tmp <- data[, lapply(.SD, prot_mean, na.rm=TRUE), by=generation]
summary <- tmp[, "cpEvolving" := (((generation %/% 20) %% 2) == 1) ]



raw_plots <- c()
raw_plots[[1]] <- ggplot(summary,aes_string(x = "generation", y = 'fitness', color = "cpEvolving")) + 
  geom_point(shape=1) +
#  geom_smooth(method=loess) + 
  ggtitle("Fitness over Time")

raw_plots[[2]] <- ggplot(summary,aes(x=generation,y=log(fitness),color=cpEvolving)) + 
  geom_point(shape=1) +
#  geom_smooth(method=loess) + 
  ggtitle("Log Fitness over Time")


plot_index <- 3
for (colname in colnames) {
  raw_plots[[plot_index]] <- ggplot(summary,aes_string(x = "generation", y = colname, color="cpEvolving")) + 
    geom_point(shape=1) +
#    geom_smooth(method=loess) + 
    ggtitle(genome_labels[[plot_index-2]])
  
  plot_index <- plot_index + 1
}

num_cols <- as.integer(num_loci^0.5) + 1
png(paste(output_prefix, "_values_byWhichEvolving.png", sep=''), height=png_height, width=png_width, res=(300/(num_cols/2)))
multiplot(plotlist=raw_plots,cols=num_cols)
dev.off()


