library(data.table)

inputFile <- "Template.xml"
template <- readLines(inputFile)

# for run 004, first refactored run
# parameters <- expand.grid(numNSPs = c(1, 2),
#                     alpha = c(0.2, 0.5, 1, 2, 5),
#                     beta = c(0.2, 0.5, 1, 2, 5),
#                     zeroIXC = c(FALSE, TRUE),
#                     zeroRating = c(FALSE, TRUE),
#                     bundling = c(FALSE, TRUE),
#                     ispContent = c(FALSE, TRUE))
# parameters <- as.data.table(parameters)

# parameters$ID<-seq.int(nrow(parameters))


f <- function(row, template) {
  configFile <- template
  configFile <- gsub("__NUM_NSPS__", row$numNSPs, configFile)
  configFile <- gsub("__ALPHA__", row$alpha, configFile)
  configFile <- gsub("__BETA__", row$beta, configFile)
  configFile <- gsub("__ZERO_IXC__", row$zeroIXC, configFile)
  configFile <- gsub("__GAMMA__", row$gamma, configFile)
  
  configFile <- gsub("__BUNDLING__", row$bundling, configFile)
  configFile <- gsub("__ZERORATING__", row$zeroRating, configFile)
  configFile <- gsub("__ISP_CONTENT__", row$ispContent, configFile)
  configFile <- gsub("__NUM_3P_VIDEO__", row$numVideoCPs, configFile)
  cat(configFile, file = sprintf("%d.xml",row$ID), sep='\n')
}



parameters <- expand.grid(numNSPs = c(1, 2),
                    numVideoCPs = c(1, 2, 3),
					          gamma = c(0.4, 0.8),
                    zeroIXC = c(FALSE, TRUE),
                    zeroRating = c(FALSE, TRUE),
                    bundling = c(FALSE, TRUE),
                    ispContent = c(TRUE, FALSE),
                    alpha = rep(c(1), times=20),
                    beta = rep(c(1), times=20))
parameters <- as.data.table(parameters)
parameters <- parameters[!(ispContent == FALSE & bundling == TRUE)]
parameters <- parameters[!(ispContent == FALSE & zeroRating == TRUE)]
parameters <- parameters[!(ispContent == FALSE & numVideoCPs != 3)]
parameters <- parameters[!(ispContent == TRUE & (numNSPs + numVideoCPs != 3))]

parameters$ID <- seq.int(nrow(parameters))
parameters$alpha <- exp(runif(nrow(parameters), -2,2))
parameters$beta <- exp(runif(nrow(parameters), -2,2))
# ggplot(parameters, aes(x=log(alpha))) + geom_histogram()


# Actually write stuff.  Commented out so as to not run accidentally and overwrite data!
#
write.table(parameters,file="parameters.table")
tmp <- by(parameters, 1:nrow(parameters), f, template = template)



