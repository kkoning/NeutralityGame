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
	data=data[nspContent == TRUE]))

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




factorize_gamma <- function(gamma) {
	factor <- rep(0,length(gamma))
	for (i in 1:length(gamma) ) {
		if (gamma[[i]] < 0.5) {
			factor[[i]] <- 1
		} else {
			factor[[i]] <- 2
		}
	}
	return(factor)
}




foo <- dummies_to_factor(data$ispContent, data$bundling, data$zeroRating)
bar <- factor(foo,labels = c("Separation","Restricted", "Bundling", "ZeroRating", "Both"))
data$Condition <- bar
rm(foo,bar)


#
# Interesting Plots
#


# Alpha vs. hhiVideo under various conditions
ggplot(data[Condition=="Separation"], aes(y=hhiVideo, x=log(alpha), color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = Separation")

ggplot(data[Condition=="Restricted"], aes(y=hhiVideo, x=log(alpha), color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = Restricted")

ggplot(data[Condition=="Bundling"], aes(y=hhiVideo, x=log(alpha), color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = Bundling")

ggplot(data[Condition=="ZeroRating"], aes(y=hhiVideo, x=log(alpha), color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = ZeroRating")

ggplot(data[Condition=="Both"], aes(y=hhiVideo, x=log(alpha), color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = Both")

#combined
ggplot(data, aes(y=hhiVideo, x=log(alpha), color=numNSPs)) + geom_point()


ggplot(data[Condition=="Bundling"], aes(y=vcpKa+nspKa, x=hhiVideo, color=as.factor(numNSPs))) + 
	geom_point() + 
	ggtitle("Alpha's effect on hhiVideo, condition = Both")


ggplot(data, aes(y=vcpKa, x=hhiVideo, color=log(alpha))) + geom_point()


ggplot(data[numNSPs == 1], aes(y=hhiVideo, x=Condition, color=numNSPs)) + geom_point()

ggplot(data[numNSPs == 1], aes(y=hhiVideo, x=Condition)) + geom_boxplot()






## Interesting Regressions

# Without condition
#lm_hhiVideo <- lm(hhiVideo ~ numNSPs + log(alpha) + log(beta) + bundling * zeroRating * log(alpha) + bundling * zeroRating * log(beta), data=combined[ispContent == TRUE])
#
#summary(lm_hhiVideo)

# With Condition

lm_hhiVideoSimple <- lm(hhiVideo ~ Condition * log(alpha) + Condition * log(beta), data=data)
lm_hhiVideoSimpleHighGamma <- lm(hhiVideo ~ Condition * log(alpha) + Condition * log(beta), data=data[gamma > 0.6])
lm_hhiVideoSimpleLowGamma <- lm(hhiVideo ~ Condition * log(alpha) + Condition * log(beta), data=data[gamma < 0.6])
summary(lm_hhiVideoSimple)
stargazer(lm_hhiVideoSimple, no.space=TRUE, single.row=TRUE, 
	title='$\\alpha$ and $\\beta$ and HHI by Policy', label="lm_hhiVideoSimple",
	dep.var.labels=c("Video Market HHI"),
	out="lm_hhiVideoSimple.tex")
stargazer(lm_hhiVideoSimpleHighGamma, no.space=TRUE, single.row=TRUE, 
	title='$\\alpha$ and $\\beta$ and HHI by Policy, High $\\gamma$ only', 
	label="lm_hhiVideoSimpleHighGamma",
	dep.var.labels=c("Video Market HHI"),
	out="lm_hhiVideoSimpleHighGamma.tex")
stargazer(lm_hhiVideoSimpleLowGamma, no.space=TRUE, single.row=TRUE, 
	title='$\\alpha$ and $\\beta$ and HHI by Policy, Low $\\gamma$ only', 
	label="lm_hhiVideoSimpleLowGamma",
	dep.var.labels=c("Video Market HHI"),
	out="lm_hhiVideoSimpleLowGamma.tex")


lm_hhiVideo <- lm(hhiVideo ~ Condition * log(alpha) * zeroIXC + Condition * log(beta) * zeroIXC, data=data)
summary(lm_hhiVideo)

stargazer(lm_hhiVideo, no.space=TRUE, single.row=TRUE, 
	title='$\\alpha$ and $\\beta$ and HHI by Policy and IXC pricing', label="lm_hhiVideo",
	dep.var.labels=c("Video Market HHI"),
	out="lm_hhiVideo.tex")



lm_VideoInvestmentSimple <- lm(totalKaLog ~ Condition * log(alpha) + Condition * log(beta), data=data)
lm_VideoInvestmentSimpleHighGamma <- lm(totalKaLog ~ Condition * log(alpha) + Condition * log(beta), data=data[gamma > 0.6])
lm_VideoInvestmentSimpleLowGamma <- lm(ntotalKaLog ~ Condition * log(alpha) + Condition * log(beta), data=data[gamma < 0.6])
summary(lm_VideoInvestmentSimple)
summary(lm_VideoInvestmentSimpleHighGamma)
summary(lm_VideoInvestmentSimpleLowGamma)

stargazer(lm_VideoInvestmentSimple, no.space=TRUE, single.row=TRUE, 
	title='$\\alpha$ and $\\beta$ and Video Content Investment by Policy', 
	label="lm_VideoInvestmentSimple",
	dep.var.labels=c("Total Video Content Investment (log)"),
	out="lm_VideoInvestmentSimple.tex")

stargazer(lm_VideoInvestmentSimpleHighGamma, no.space=TRUE, single.row=TRUE, 
	title='$\\alpha$ and $\\beta$ and Video Content Investment by Policy, High $\\gamma$ only', 
	label="lm_VideoInvestmentSimple",
	dep.var.labels=c("Total Video Content Investment (log)"),
	out="lm_VideoInvestmentSimpleHighGamma.tex")

stargazer(lm_VideoInvestmentSimpleLowGamma, no.space=TRUE, single.row=TRUE, 
	title='$\\alpha$ and $\\beta$ and Video Content Investment by Policy, Low $\\gamma$ only', 
	label="lm_VideoInvestmentSimple",
	dep.var.labels=c("Total Video Content Investment (log)"),
	out="lm_VideoInvestmentSimpleLowGamma.tex")




lm_NetworkInvestmentSimple <- lm(log(nspKn) ~ Condition * log(alpha) + Condition * log(beta), data=data)
summary(lm_NetworkInvestmentSimple)
stargazer(lm_VideoInvestmentSimpleLowGamma, no.space=TRUE, single.row=TRUE, 
	title='$\\alpha$ and $\\beta$ and Network Investment by Policy', 
	label="lm_VideoInvestmentSimple",
	dep.var.labels=c("Total Network Investment (log)"),
	out="lm_NetworkInvestmentSimple.tex")



# No interaction between alpha and beta.

lm_hhiVideo <- lm(hhiVideo ~ numNSPs + log(alpha) * Condition + log(beta) * Condition, data=data)
summary(lm_hhiVideo)


# On consumer utility
lm_hhiVideo <- lm(utilityTotal ~ numNSPs + log(alpha) * Condition + log(beta) * Condition, data=data)
summary(lm_hhiVideo)














