library(data.table)
source("http://home.kkoning.net/misc/R/protected_summary_functions.r")

summarize <- function(input_file, id, replicate) {
  summaries <- list()
  data <- read.csv(input_file, header=TRUE)
  data <- as.data.table(data)
  first_generation <- as.integer(max(data$generation) * 0.9)
  summaries[[1]] <- data[generation >= first_generation, lapply(.SD, prot_mean, na.rm=TRUE) ]
  summaries[[2]] <- data[generation >= first_generation, lapply(.SD, prot_sd, na.rm=TRUE) ]
  summaries[[1]]$ID = id
  summaries[[1]]$REP = replicate
  summaries[[2]]$ID = id
  summaries[[2]]$REP = replicate
  summaries
}

args <- commandArgs(TRUE)
filename <- as.character(args[1])
first_id <- as.integer(args[2])
last_id <- as.integer(args[3])
first_rep <- as.integer(args[4])
last_rep <- as.integer(args[5])

ids <- seq(first_id,last_id)
replicates <- seq(first_rep,last_rep)

accumulate <- list(NULL,NULL)
for (id in ids) {
  for (replicate in replicates) {
    file <- sprintf("%d/%d/%s", id, replicate, filename)
    result <- summarize(file, id, replicate)
    accumulate[[1]] <- rbind(accumulate[[1]],result[[1]])
    accumulate[[2]] <- rbind(accumulate[[2]],result[[2]])
  }
}

write.table(accumulate[[1]],file=sprintf("%s.%d-%d.%d-%d.means.table",
  filename,first_id,last_id,first_rep,last_rep))
write.table(accumulate[[2]],file=sprintf("%s.%d-%d.%d-%d.sd.table",
  filename,first_id,last_id,first_rep,last_rep))




