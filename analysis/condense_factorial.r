library(data.table)
source("http://home.kkoning.net/misc/R/protected_summary_functions.r")

args <- commandArgs(TRUE)
filename <- as.character(args[1])
final_percent <- as.numeric(args[2]) / 100
id <- as.integer(args[3])
replicate <- as.integer(args[4])

data <- read.csv(filename, header=TRUE)
data <- as.data.table(data)
first_generation <- as.integer(max(data$generation) * (1-final_percent))
summary <- data[generation >= first_generation, lapply(.SD, prot_mean, na.rm=TRUE) ]
summary$ID = id
summary$REP = replicate

write.table(summary,file=sprintf("%s.means.table", filename))
#file.remove(filename)




