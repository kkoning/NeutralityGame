
d <- data[generation < 7000,.(videoKa = mean(videoKa)),by=generation]

phase_mapping <- function(generation) {
	factor <- rep(0,length(generation))
	for (i in 1:length(generation) ) {
		if (generation[[i]] < 3050) {
			factor[[i]] <- 1
		}
		if (generation[[i]] >= 3050 & generation[[i]] <= 4000) {
			factor[[i]] <- 2
		}
		if (generation[[i]] > 4000) {
			factor[[i]] <- 3
		}
	}
	return(factor)
}

d$Phase <- factor(phase_mapping(d$generation),labels=c("Growth", "Adjustment", "Drift"))

ggplot(d, aes(x=generation, y=videoKa, group=Phase, color=Phase, linetype=Phase)) + 
	geom_smooth(
		method="loess",span=0.2, 
		se=FALSE) + 
	theme_minimal() + 
	xlab("Generation") + 
	ylab("Investment") + 
	theme(plot.title=element_text(family="CMU Serif", face="bold", size=14), 
		axis.title=element_text(family="CMU Serif", face="bold", size=12))


pdf(file="figure_evo_phases.pdf", width=5.5, height=4)


