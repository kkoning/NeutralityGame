require(ggplot2)
require(data.table)

#4k graphics
png_width <- 3840
png_height <- 2160

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

dir.create("analysis")

deno <- read.csv('deno.csv', header=TRUE)
deno <- data.table(deno)
summary(deno)

deno_mean <- deno[,list(
  fitness_mean=mean(fitness),
  loc_0_mean=mean(loc_0),
  loc_1_mean=mean(loc_1),
  loc_2_mean=mean(loc_2),
  loc_3_mean=mean(loc_3),
  loc_4_mean=mean(loc_4),
  loc_5_mean=mean(loc_5),
  loc_6_mean=mean(loc_6),
  loc_7_mean=mean(loc_7),
  loc_8_mean=mean(loc_8),
  loc_9_mean=mean(loc_9)
  ),by=generation]

fitness <- ggplot(deno_mean, aes(generation,fitness_mean)) + 
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Average Fitness')

g0 <- ggplot(deno_mean, aes(generation,loc_0_mean)) +
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[0]\nNetworkInvestment')

g1 <- ggplot(deno_mean, aes(generation,loc_1_mean)) +
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[1]\nContentInvestment')

g2 <- ggplot(deno_mean, aes(generation,loc_2_mean)) +
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[2]\nNetOfferConnectionPrice')

g3 <- ggplot(deno_mean, aes(generation,loc_3_mean)) +
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[3]\nNetOfferBandwidthPrice')

g4 <- ggplot(deno_mean, aes(generation,loc_4_mean)) + 
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[4]\nContentOfferPrice')

g5 <- ggplot(deno_mean, aes(generation,loc_5_mean)) +
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[5]\nBundledOfferPrice')

g6 <- ggplot(deno_mean, aes(generation,loc_6_mean)) +
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[6]\nBundledBandwidthPrice')

g7 <- ggplot(deno_mean, aes(generation,loc_7_mean)) +
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[7]\nBundledZeroRatedOfferPrice')

g8 <- ggplot(deno_mean, aes(generation,loc_8_mean)) +
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[8]\nBundledZeroRatedBandwidthPrice')

g9 <- ggplot(deno_mean, aes(generation,loc_9_mean)) +
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[9]\nInterconnectionBandwidthPrice')

png(filename = "analysis/deno.png", height=png_height, width=png_width, res=150)
multiplot(fitness, g0, g1, g2, g3, g4, g5, g6, g7, g8, g9, cols=3)
dev.off()
rm(fitness, g0, g1, g2, g3, g4, g5, g6, g7, g8, g9)


# Directly encoded video providers

devcp <- read.csv('devcp.csv', header=TRUE)
devcp <- data.table(devcp)
summary(devcp)

devcp_mean <- devcp[,list(
  fitness_mean=mean(fitness),
  loc_0_mean=mean(loc_0),
  loc_1_mean=mean(loc_1)
),by=generation]

fitness <- ggplot(devcp_mean, aes(generation,fitness_mean)) + 
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Average Fitness')

g0 <- ggplot(devcp_mean, aes(generation,loc_0_mean)) + 
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[0]\nContentInvestment')

g1 <- ggplot(devcp_mean, aes(generation,loc_1_mean)) + 
  geom_point(shape=1) +
  geom_smooth(method=loess) + 
  xlab('Generation') +
  ylab('Value of genome[1]\nContentOfferPrice')

png(filename = "analysis/devcp.png", height=png_height, width=png_width, res=300)
multiplot(fitness, g0, g1, cols=2)
dev.off()


# Directly encodded other content providers

deocp <- read.csv('deocp_mean.csv', header=TRUE)
deocp <- data.table(deocp)
summary(deocp)

fitness <- ggplot(deocp, aes(generation,fitness)) + 
  geom_point() + xlab('Generation') +
  ylab('Average Fitness')

g0 <- ggplot(deocp, aes(generation,loc_0)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[0]\nContentInvestment')

g1 <- ggplot(deocp, aes(generation,loc_1)) + 
  geom_point() + xlab('Generation') +
  ylab('Value of genome[1]\nContentOfferPrice')


png(filename = "analysis/DirectlyEncodedOtherContentProviderGenome.png", width=40, 
    height=40, units = "cm", pointsize=8, res=300)
multiplot(fitness, g0, g1, cols=2)
dev.off()


# Model parameters

# Model Results
models <- read.csv('summary_mean.csv', header=TRUE)
models <- data.table(models)
summary(models)

dir.create("analysis/agentModel")

png(filename = "analysis/agentModel/ConsumerSurplus.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,consumerSurplus)) + 
  geom_point() + xlab('Generation') +
  ylab('Consumer Surplus')
dev.off()

png(filename = "analysis/agentModel/NetworkOperatorsSurplus.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,networkOperatorSurplus)) + 
  geom_point() + xlab('Generation') +
  ylab('Network Operator Surplus')
dev.off()

png(filename = "analysis/agentModel/NetworkOperatorInvestment.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,networkOperatorInvestment)) + 
  geom_point() + xlab('Generation') +
  ylab('networkOperatorInvestment')
dev.off()

png(filename = "analysis/agentModel/ICFeesFromVideo.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,totalICFeesFromVideo)) + 
  geom_point() + xlab('Generation') +
  ylab('totalICFeesFromVideo')
dev.off()

png(filename = "analysis/agentModel/ICFeesFromOther.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,totalICFeesFromOther)) + 
  geom_point() + xlab('Generation') +
  ylab('totalICFeesFromOther')
dev.off()

png(filename = "analysis/agentModel/NumStandaloneNetworkOffersAccepted.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,numStandaloneNetworkOffersAccepted)) + 
  geom_point() + xlab('Generation') +
  ylab('numStandaloneNetworkOffersAccepted')
dev.off()

png(filename = "analysis/agentModel/NumNSPStandaloneVideoOffersAccepted.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,numNSPStandaloneVideoOffersAccepted)) + 
  geom_point() + xlab('Generation') +
  ylab('numNSPStandaloneVideoOffersAccepted')
dev.off()

png(filename = "analysis/agentModel/NumThirdPartyStandaloneVideoOffersAccepted.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,numThirdPartyStandaloneVideoOffersAccepted)) + 
  geom_point() + xlab('Generation') +
  ylab('numThirdPartyStandaloneVideoOffersAccepted')
dev.off()

png(filename = "analysis/agentModel/StandaloneVideoOffersHHI.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,numStandaloneVideoOffersHHI)) + 
  geom_point() + xlab('Generation') +
  ylab('numStandaloneVideoOffersHHI')
dev.off()

png(filename = "analysis/agentModel/NumVideoContentProviders.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,numVideoContentProviders)) + 
  geom_point() + xlab('Generation') +
  ylab('numVideoContentProviders')
dev.off()

png(filename = "analysis/agentModel/VideoProvderSurplus.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,cpVideoProvderSurplus)) + 
  geom_point() + xlab('Generation') +
  ylab('cpVideoProvderSurplus')
dev.off()

png(filename = "analysis/agentModel/NumOtherContentProvides.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,numOtherContentProvides)) + 
  geom_point() + xlab('Generation') +
  ylab('numOtherContentProviders')
dev.off()

png(filename = "analysis/agentModel/ConsumerSurplus.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,cpOtherProviderSurplus)) + 
  geom_point() + xlab('Generation') +
  ylab('cpOtherProviderSurplus')
dev.off()

png(filename = "analysis/agentModel/VideoContentInvestment_NSP.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,nspVideoContentInvestment)) + 
  geom_point() + xlab('Generation') +
  ylab('nspVideoContentInvestment')
dev.off()

png(filename = "analysis/agentModel/VideoContentInvestment_CP.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,cpVideoContentInvestment)) + 
  geom_point() + xlab('Generation') +
  ylab('cpVideoContentInvestment')
dev.off()

png(filename = "analysis/agentModel/OtherContentInvestment.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,cpOtherContentInvestment)) + 
  geom_point() + xlab('Generation') +
  ylab('cpOtherContentInvestment')
dev.off()

png(filename = "analysis/agentModel/TotalContentInvestment.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,totalContentInvestment)) + 
  geom_point() + xlab('Generation') +
  ylab('totalContentInvestment')
dev.off()

png(filename = "analysis/agentModel/StandaloneNetworkRevenue.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,totalStandaloneNetworkRevenue)) + 
  geom_point() + xlab('Generation') +
  ylab('totalStandaloneNetworkRevenue')
dev.off()

png(filename = "analysis/agentModel/NumBundledOfferedAccepted.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,numBundledNetworkOffersAccepted)) + 
  geom_point() + xlab('Generation') +
  ylab('numBundledNetworkOffersAccepted')
dev.off()

png(filename = "analysis/agentModel/NumBundledZeroRatedOffersAccepted.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,numBundledZeroRatedOffersAccepted)) + 
  geom_point() + xlab('Generation') +
  ylab('numBundledZeroRatedOffersAccepted')
dev.off()

png(filename = "analysis/agentModel/TotalBundledRevenue.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,totalBundledRevenue)) + 
  geom_point() + xlab('Generation') +
  ylab('totalBundledRevenue')
dev.off()

png(filename = "analysis/agentModel/TotalBundledZeroRatedRevenue.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,totalContentInvestment)) + 
  geom_point() + xlab('totalBundledZeroRatedRevenue') +
  ylab('totalBundledZeroRatedRevenue')
dev.off()

png(filename = "analysis/agentModel/HHI_Network.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,networkHHI)) + 
  geom_point() + xlab('Generation') +
  ylab('networkHHI')
dev.off()

png(filename = "analysis/agentModel/HHI_Video.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,videoHHI)) + 
  geom_point() + xlab('Generation') +
  ylab('videoHHI')
dev.off()

png(filename = "analysis/agentModel/HHI_Other.png", width=20, 
    height=20, units = "cm", pointsize=8, res=300)
ggplot(models, aes(Generation,otherHHI)) + 
  geom_point() + xlab('Generation') +
  ylab('otherHHI')
dev.off()

