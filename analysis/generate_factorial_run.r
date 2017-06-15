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
  configFile <- gsub("__NUM_3P_VIDEO__", row$numVideoCPs, configFile)

  configFile <- gsub("__CAP_CALC_METHOD__", row$capCalcMethod, configFile)
  configFile <- gsub("__DEMAND_ADJ_METHOD__", row$demandAdjMethod, configFile)
  configFile <- gsub("__POLICY_REGIME__", row$policyRegime, configFile)

  cat(configFile, file = sprintf("%d.xml",row$ID), sep='\n')
}

parameters <- expand.grid(numNSPs = c(1, 2),
                    numVideoCPs = c(3, 4, 5),
					          capCalcMethod = c("COBB_DOUGLASS", "LOG_LOG"),
                    demandAdjMethod = c("CONSTANT", "PRICE"),
                    policyRegime = c("STRUCTURAL_SEPARATION",
                                    "RESTRICTED",
                                    "BUNDLING_ONLY",
                                    "ZERO_RATING_ONLY",
                                    "BUNDLING_AND_ZERO_RATING"),
                    zeroIXC = c(FALSE, TRUE),
                    alpha = rep(c(1), times=20),
                    beta = rep(c(1), times=20))
parameters <- as.data.table(parameters)
parameters <- parameters[!(policyRegime == "STRUCTURAL_SEPARATION" & numVideoCPs != 4)]
parameters <- parameters[!(policyRegime =! "STRUCTURAL_SEPARATION" & (numNSPs + numVideoCPs != 4))]
parameters$ID <- seq.int(nrow(parameters))
parameters$alpha <- exp(rnorm(nrow(parameters), 1.5))
parameters$beta <- exp(rnorm(nrow(parameters), 1.5))
nrow(parameters)




# Actually write stuff.  Commented out so as to not run accidentally and overwrite data!
#
write.table(parameters,file="parameters.table")
tmp <- by(parameters, 1:nrow(parameters), f, template = template)



