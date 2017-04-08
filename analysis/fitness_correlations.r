require(ggplot2)
require(data.table)
require(broom)
source("http://home.kkoning.net/misc/multiplot.r")

args <- commandArgs(TRUE)

filename <- as.character(args[1])
num_loci <- as.integer(args[2])
output_prefix <- as.character(args[3])
labels_file <- as.character(args[4])
source(labels_file)


#num_loci <- 10
data <- read.csv(filename, header=TRUE)
data <- as.data.table(data)
png_width <- 3840
png_height <- 2160


colnames <- c()
for (i in 1:num_loci) {
  colnames[[i]] <- paste("loc",i-1,sep="_")
}

# Calculate correlation coefficients with fitness
cor_df <- data.frame('generation' = unique(data$generation))
for (loci in 1:num_loci) {
  fit_cor = c()
  index <- 1
  for (gen in unique(data$generation)) {
    fit_cor[[index]] <- cor(data[generation==gen]$'fitness', data[generation==gen,get(colnames[loci])])
    index <- index + 1

  }
  cor_df[paste(colnames[loci],"corr",sep="_")] <- fit_cor
}

#ggplot(cor_df, aes_string(y = "loc_2_corr", x = "generation")) + geom_point()


# That's it for correlation plots, now how about lm results?

## Need to compose the regression model string
lm_string <- 'fitness ~ '
for (i in 1:num_loci) {
  lm_string <- sprintf('%s%s',lm_string, colnames[i])
  if (i < num_loci)
    lm_string <- sprintf('%s + ', lm_string)
}
lm_string


sdev <- list()
fit_coef <- list()
fit_z <- list()
for (i in 1:num_loci) {
  sdev[[i]] <- list()
  fit_coef[[i]] <- list()
  fit_z[[i]] <- list()
}

index <- 1
for (gen in unique(data$generation)) {
  model <- lm(as.formula(lm_string), data[generation==gen])
  m2 <- tidy(model)
  for (i in 1:num_loci) {
    fit_coef[[i]][[index]] <- m2$estimate[1+i]
    sdev[[i]][[index]] <- m2$std.error[1+i]
    fit_z[[i]][[index]] <- m2$statistic[1+i]
  }
  index <- index + 1
}
for (i in 1:num_loci) {
  cor_df[paste(colnames[[i]],'estimate',sep='_')] <- unlist(fit_coef[[i]])
  cor_df[paste(colnames[[i]],'sdev',sep='_')] <- unlist(sdev[[i]])
  cor_df[paste(colnames[[i]],'z',sep='_')] <- unlist(fit_z[[i]])
}

### Finally done with computations, let's make some plots

num_cols <- as.integer(num_loci^0.5) + 1

raw_plots <- c()
z_plots <- c()
cor_plots <- c()
plot_index <- 1
for (colname in colnames) {
  z_plots[[plot_index]] <- ggplot(cor_df,aes_string(x = "generation", y = paste(colname,"z",sep='_'))) + 
    geom_point(shape=1) +
    geom_smooth(method=loess) + 
    ggtitle(sprintf("Z score of %s in \nlm(fitness) of all loci", genome_labels[[plot_index]]))
  cor_plots[[plot_index]] <- ggplot(cor_df,aes_string(x = "generation", y = paste(colname,"corr",sep='_'))) + 
    geom_point(shape=1) +
    geom_smooth(method=loess) + 
    ggtitle(sprintf("corr(fitness,%s)", genome_labels[[plot_index]]))

  # done in raw_values.r
  #  raw_plots[[plot_index]] <- ggplot(data,aes_string(x = "generation", y = colname)) + 
#    geom_point(shape=1) +
#    geom_smooth(method=loess) + 
#    ggtitle(sprintf("corr(fitness,%s)", genome_labels[[plot_index]]))
  
  plot_index <- plot_index + 1
}

## Plots are done, let's write them to files
png(paste(output_prefix, "_correlations.png", sep=''), height=png_height, width=png_width, res=(300/(num_cols/2)))
multiplot(plotlist=cor_plots,cols=num_cols)
dev.off()

## Plots are done, let's write them to files
png(paste(output_prefix, "_z_scores.png",sep=''), height=png_height, width=png_width, res=(300/(num_cols/2)))
multiplot(plotlist=z_plots,cols=num_cols)
dev.off()

