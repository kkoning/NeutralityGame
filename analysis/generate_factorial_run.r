library(data.table)

inputFile <- "/Users/liara/ownCloud/src/NeutralityGame/data/config/Template.xml"
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
  configFile <- gsub("__NUMNSPS__", row$numNSPs, configFile)
  configFile <- gsub("__ALPHA__", row$alpha, configFile)
  configFile <- gsub("__BETA__", row$beta, configFile)
  configFile <- gsub("__ZEROIXC__", row$zeroIXC, configFile)
  configFile <- gsub("__BUNDLING__", row$bundling, configFile)
  configFile <- gsub("__ZERORATING__", row$zeroRating, configFile)
  configFile <- gsub("__ISP_CONTENT__", row$ispContent, configFile)
  cat(configFile, file = sprintf("%d.xml",row$ID), sep='\n')
}



parameters <- expand.grid(numNSPs = c(1, 2),
                    zeroIXC = c(TRUE),
                    zeroRating = c(FALSE, TRUE),
                    bundling = c(FALSE, TRUE),
                    ispContent = c(TRUE, FALSE),
                    alpha = rep(c(1), times=30),
                    beta = rep(c(1), times=30))
parameters <- as.data.table(parameters)
parameters <- parameters[!(ispContent == FALSE & bundling == TRUE)]
parameters <- parameters[!(ispContent == FALSE & zeroRating == TRUE)]
parameters$ID <- seq.int(nrow(parameters))
parameters$alpha <- exp(rnorm(nrow(parameters), sd=1.5))
parameters$beta <- exp(rnorm(nrow(parameters), sd=1.5))
# ggplot(parameters, aes(x=log(alpha))) + geom_histogram()


# Actually write stuff.  Commented out so as to not run accidentally and overwrite data!
#
# write.table(parameters,file="non_strategic.table")
# tmp <- by(parameters, 1:nrow(parameters), f, template = template)



