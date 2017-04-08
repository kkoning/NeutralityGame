require(ggplot2)
require(data.table)
require(broom)
source("http://home.kkoning.net/misc/multiplot.r")

args <- commandArgs(TRUE)
filename <- as.character(args[1])
num_loci <- as.integer(args[2])
output_prefix <- as.character(args[3])

data <- read.csv(filename, header=TRUE)
data <- as.data.table(data)
png_width <- 3840
png_height <- 2160

# Multiple plot function
#
# ggplot objects can be passed in ..., or to plotlist (as a list of ggplot objects)
# - cols:   Number of columns in layout
# - layout: A matrix specifying the layout. If present, 'cols' is ignored.
#
# If the layout is something like matrix(c(1,2,3,3), nrow=2, byrow=TRUE),
# then plot 1 will go in the upper left, 2 will go in the upper right, and
# 3 will go all the way across the bottom.
#
multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
  library(grid)
  
  # Make a list from the ... arguments and plotlist
  plots <- c(list(...), plotlist)
  
  numPlots = length(plots)
  
  # If layout is NULL, then use 'cols' to determine layout
  if (is.null(layout)) {
    # Make the panel
    # ncol: Number of columns of plots
    # nrow: Number of rows needed, calculated from # of cols
    layout <- matrix(seq(1, cols * ceiling(numPlots/cols)),
                     ncol = cols, nrow = ceiling(numPlots/cols))
  }
  
  if (numPlots==1) {
    print(plots[[1]])
    
  } else {
    # Set up the page
    grid.newpage()
    pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
    
    # Make each plot, in the correct location
    for (i in 1:numPlots) {
      # Get the i,j matrix positions of the regions that contain this subplot
      matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
      
      print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                      layout.pos.col = matchidx$col))
    }
  }
}

# Network Operators

deno <- read.csv('deno.csv', header=TRUE)
deno <- data.table(deno)
summary(deno)


g0 <- ggplot(deno[, .(agg = mean(loc_0)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[0]\nNetworkInvestment')

g1 <- ggplot(deno[, .(agg = mean(loc_1)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[1]\nContentInvestment')

g2 <- ggplot(deno[, .(agg = mean(loc_2)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[2]\nNetOfferConnectionPrice')

g3 <- ggplot(deno[, .(agg = mean(loc_3)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[3]\nNetOfferBandwidthPrice')

g4 <- ggplot(deno[, .(agg = mean(loc_4)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[4]\nContentOfferPrice')

g5 <- ggplot(deno[, .(agg = mean(loc_5)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[5]\nBundledOfferPrice')

g6 <- ggplot(deno[, .(agg = mean(loc_6)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[6]\nBundledBandwidthPrice')

g7 <- ggplot(deno[, .(agg = mean(loc_7)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[7]\nBundledZeroRatedOfferPrice')

g8 <- ggplot(deno[, .(agg = mean(loc_8)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[8]\nBundledZeroRatedBandwidthPrice')

g9 <- ggplot(deno[, .(agg = mean(loc_9)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[9]\nInterconnectionBandwidthPrice')

multiplot(g0, g1, g2, g3, g4, g5, g6, g7, g8, g9, cols=3)

rm(g0, g1, g2, g3, g4, g5, g6, g7, g8, g9)


# Video Content Providers
devcp <- read.csv('devcp.csv', header=TRUE)
devcp <- data.table(deno)
summary(devcp)

g0 <- ggplot(devcp[, .(agg = mean(loc_0)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[0]\nContentInvestment')

g1 <- ggplot(devcp[, .(agg = mean(loc_1)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[1]\nContentOfferPrice')

g2 <- ggplot(devcp[, .(agg = mean(loc_2)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[2]\n(Unused)')


multiplot(g0, g1, g2, cols=2)



# Other Content Providers

deocp <- read.csv('deocp.csv', header=TRUE)
deocp <- data.table(deocp)
summary(deocp)

fitness <- ggplot(deocp[, .(agg = mean(fitness)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Average Fitness')

g0 <- ggplot(deocp[, .(agg = mean(loc_0)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[0]\nContentInvestment')

g1 <- ggplot(deocp[, .(agg = mean(loc_1)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[1]\nContentOfferPrice')

g2 <- ggplot(deocp[, .(agg = mean(loc_2)), by=generation], aes(generation,agg)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[2]\n(Unused)')


multiplot(fitness, g0, g1, g2, cols=2)










