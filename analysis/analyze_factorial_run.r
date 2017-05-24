library(data.table)
library(ggplot2)

# Parameters have already been generated; must use the 
# existing ones, in order to match variables!
#
# parameters <- expand.grid(numNSPs = c(1, 2),
#                     alpha = c(0.2, 0.5, 1, 2, 5),
#                     beta = c(0.2, 0.5, 1, 2, 5),
#                     zeroIXC = c(FALSE, TRUE),
#                     zeroRating = c(FALSE, TRUE),
#                     bundling = c(FALSE, TRUE),
#                     separation = c(FALSE, TRUE))
# parameters <- as.data.table(parameters)
# parameters <- parameters[!(separation == TRUE & bundling == TRUE)]


parameters <- read.table("non_strategic.table")

# In the future, load data directly.  For now, some condensing is necessary
#
data <- read.table("summary.csv.means.table_8001-8500.table")


# i <- 1
# results <- list(NULL)
# data <- NULL
# for (file in dir(pattern="*means.table")) {
# 	tmp <- read.table(file)
# 	# results[[i]] <- tmp
# 	# i <- i + 1
# 	data <- rbind(data,tmp)
# }

data <- merge(data,parameters)
data <- as.data.table(data)

data$utilityTotal <- data$utilityVideoOnly + data$utilityOtherOnly + data$utilityBoth


summary(data[bundling == FALSE]$hhiVideo)


summary(lm(hhiVideo ~ numNSPs + zeroRating +
	bundling * log(alpha) + 
	bundling * log(beta) + 
	zeroRating * log(alpha) + 
	zeroRating * log(beta), 
	data=data[ispContent == TRUE]))

summary(lm(hhiVideo ~ numNSPs +
	bundling * zeroRating * log(alpha) +
	bundling * zeroRating * log(beta),
	data=data[ispContent == TRUE]))

summary(lm(hhiVideo ~ numNSPs +
	bundling * zeroRating * alpha +
	bundling * zeroRating * beta,
	data=data[ispContent == TRUE]))




dummies_to_factor <- function(ispContent, bundling, zeroRating) {
	factor <- rep(0,length(ispContent))
	for (i in 1:length(ispContent) ) {
		if (ispContent[[i]] == FALSE) {
			factor[[i]] <- 1
		} else {
			if (bundling[[i]] == FALSE & zeroRating[[i]] == FALSE) {
				factor[[i]] <- 2 
			}
			if (bundling[[i]] == TRUE & zeroRating[[i]] == FALSE) {
				factor[[i]] <- 3 
			}
			if (bundling[[i]] == FALSE & zeroRating[[i]] == TRUE) {
				factor[[i]] <- 4 
			}
			if (bundling[[i]] == TRUE & zeroRating[[i]] == TRUE) {
				factor[[i]] <- 5 
			}


		}
	}
	return(factor)
}

foo <- dummies_to_factor(combined$ispContent, combined$bundling, combined$zeroRating)
bar <- factor(foo,labels = c("Separation","Restricted", "Bundling", "ZeroRating", "Both"))
combined$Condition <- bar
combined <- as.data.table(combined)

#
# Interesting Plots
#


# Alpha vs. hhiVideo under various conditions
ggplot(combined[Condition=="Separation"], aes(y=hhiVideo, x=log(alpha), color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = Separation")

ggplot(combined[Condition=="Restricted"], aes(y=hhiVideo, x=log(alpha), color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = Restricted")

ggplot(combined[Condition=="Bundling"], aes(y=hhiVideo, x=log(alpha), color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = Bundling")

ggplot(combined[Condition=="ZeroRating"], aes(y=hhiVideo, x=log(alpha), color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = ZeroRating")

ggplot(combined[Condition=="Both"], aes(y=hhiVideo, x=log(alpha), color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = Both")

#combined
ggplot(combined, aes(y=hhiVideo, x=log(alpha), color=numNSPs)) + geom_point()


ggplot(combined[Condition=="Bundling"], aes(y=vcpKa+nspKa, x=hhiVideo, color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = Both")


ggplot(combined, aes(y=vcpKa, x=hhiVideo, color=log(alpha))) + geom_point()


ggplot(combined[numNSPs == 1], aes(y=hhiVideo, x=Condition, color=numNSPs)) + geom_point()

ggplot(combined[numNSPs == 1], aes(y=hhiVideo, x=Condition)) + geom_boxplot()






## Interesting Regressions

# Without condition
#lm_hhiVideo <- lm(hhiVideo ~ numNSPs + log(alpha) + log(beta) + bundling * zeroRating * log(alpha) + bundling * zeroRating * log(beta), data=combined[ispContent == TRUE])
#
#summary(lm_hhiVideo)

# With Condition

# Completely Interacted
lm_hhiVideo <- lm(hhiVideo ~ numNSPs + log(alpha) * log(beta) * Condition, data=combined)
summary(lm_hhiVideo)

# No interaction between alpha and beta.

lm_hhiVideo <- lm(hhiVideo ~ numNSPs + log(alpha) * Condition + log(beta) * Condition, data=combined)
summary(lm_hhiVideo)


# On consumer utility
lm_hhiVideo <- lm(utilityTotal ~ numNSPs + log(alpha) * Condition + log(beta) * Condition, data=combined)
summary(lm_hhiVideo)


