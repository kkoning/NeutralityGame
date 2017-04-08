args <- commandArgs(TRUE)
data_file <- as.character(args[1])
labels_file <- as.character(args[2])
x_var <- as.integer(args[3])
y_var < as.integer(args[4])
output_file <- as.character(args[5])

require(ggplot2)
require(data.table)
source("http://home.kkoning.net/misc/multiplot.r")

png_width <- 1024
png_height <- 768


data <- read.csv(data_file, header=TRUE)
data <- as.data.table(data)

source(labels_file)

plots <- list()
plot_num <- 1

plots[[plot_num]] <- ggplot(data,
                 aes_string(
                   x = sprintf("loc_%d",x_var), 
                   y = sprintf("loc_%d",y_var),
                   color = "fitness")) +
  geom_point() +
  scale_color_gradientn(colors = rainbow(6)) +
  ggtitle(sprintf("Fitness Landscape for %s",data_file)) +
  xlab(genome_labels[[x_var+1]]) +
  ylab(genome_labels[[y_var+1]]) 

plot_num <- plot_num + 1

if (max(data$fitness) > 1) {
  plots[[plot_num]] <- ggplot(data,
                aes_string(
                  x = sprintf("loc_%d",x_var), 
                  y = sprintf("loc_%d",y_var),
                  color = "log(fitness)")) +
    geom_point() +
    scale_color_gradientn(colors = rainbow(6)) +
    ggtitle(sprintf("log(Fitness) Landscape for %s",data_file)) +
    xlab(genome_labels[[x_var+1]]) +
    ylab(genome_labels[[y_var+1]]) 
  
  plot_num <- plot_num + 1
}

if (min(data$fitness < -1)) {
  plots[[plot_num]] <- ggplot(data,
                         aes_string(
                           x = sprintf("loc_%d",x_var), 
                           y = sprintf("loc_%d",y_var),
                           color = "log(-fitness)")) +
    geom_point() +
    scale_color_gradientn(colors = rainbow(6)) +
    ggtitle(sprintf("log(-Fitness) Landscapefor %s",data_file)) +
    xlab(genome_labels[[x_var+1]]) +
    ylab(genome_labels[[y_var+1]]) 

  plot_num <- plot_num + 1
}

png(filename = output_file, height=png_height, width=png_width)
multiplot(plotlist = plots, cols = 2)
dev.off()


