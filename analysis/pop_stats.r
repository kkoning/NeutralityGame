require(ggplot2)
require(data.table)
source("http://home.kkoning.net/misc/multiplot.r")

png_width <- 3840
png_height <- 2160

# requres revision if data output format changes.
pop_stats <- read.csv('DefaultEnvironmentStatistics.csv', header=TRUE)
pop_stats <- data.table(pop_stats)
summary(pop_stats)


png("population_nsps.png", height=png_height, width=png_width, res=300)
ggplot(pop_stats[populationGroup == "nsp"], 
  aes(x=generation, color=population, y=numIndividuals)) +
geom_point(shape=1, alpha=0.5)
dev.off()

png("population_video.png", height=png_height, width=png_width, res=300)
ggplot(pop_stats[populationGroup == "videoContent"], 
  aes(x=generation, color=population, y=numIndividuals)) +
geom_point(shape=1, alpha = 0.5)
dev.off()

png("population_other.png", height=png_height, width=png_width, res=300)
ggplot(pop_stats[populationGroup == "otherContent"], 
  aes(x=generation, color=population, y=numIndividuals)) +
geom_point(shape=1, alpha = 0.5)
dev.off()
