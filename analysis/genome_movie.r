library(ggplot2)
library(data.table)

args <- commandArgs(TRUE)

filename <- as.character(args[1])

start_gen <- as.integer(args[2])
end_gen <- as.integer(args[3])
every_gen <- as.integer(args[4])
num_loci <- as.integer(args[5])
num_cols <- as.integer(args[6])
image_prefix <- as.character(args[7])

data <- read.csv(filename,header=TRUE)
data <- as.data.table(data)

#4k resolution movie
image_width <- 3840
image_height <- 2160

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


frame_num <- 1;

colnames <- c()
for (i in 1:num_loci) {
  colnames[[i]] <- paste("loc",i-1,sep="_")
}


for (gen in start_gen:end_gen) {
  if ((gen %% every_gen) == 0) {

    plots <- c()
    plot_num <- 1
    for (colname in colnames) {
      plot <- ggplot(data[generation==gen], aes_string(y = "fitness", x = colname)) + 
        geom_point(shape=1) + 
        geom_smooth(method=lm) + 
        xlab(paste(colname, ", sd=",format(sd(data[generation==gen,get(colname)])),sep="")) +
        ylab(paste("fitness, sd=",format(sd(data[generation==gen]$fitness)),sep="")) +
        ggtitle(paste("Generation",gen)) +
        theme(text = element_text(size=40))
      plots[[plot_num]] <- plot
      plot_num <- plot_num + 1
    }
    filename <- sprintf("%s_%05d.png",image_prefix,frame_num)
    png(filename,width=image_width,height=image_height)
    multiplot(plotlist=plots,cols=num_cols)
    dev.off()
    frame_num = frame_num + 1
  }
}

