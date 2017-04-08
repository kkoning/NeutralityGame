require(data.table)
source("http://home.kkoning.net/misc/prot_mean.r")


args <- commandArgs(TRUE)
num_param_sets <- as.integer(args[1])
num_replicates <- as.integer(args[2])
filename <- as.character(args[3])
start_generation <- as.integer(args[4])


# test: filename <- "devcp.csv"
# test: start_generation <- 20000
# test: num_param_sets <- 5

# object for collectiong results
results <- NULL

# loop over param sets and replicates
# test: param_set <- 42
# test: replicate <- 7
for (param_set in 1:num_param_sets) {
  for (replicate in 1:num_replicates) {
 
    # load file
    #test: data <- read.csv("devcp.csv", header=TRUE)
    replicate_filename <- sprintf("%s/%s/%s", param_set, replicate, filename)
    raw_data <- read.csv(replicate_filename, header=TRUE)
    raw_data <- as.data.table(raw_data)

    # summarize by subset of generations
    sum_data <- raw_data[generation > start_generation, lapply(.SD, prot_mean, na.rm=TRUE)]
    
    # add indexes
    tagged <- sum_data[, c("params", "replicate") := list(param_set,replicate) ]
    
    # combine with larger table
    results <- rbind(results,tagged)
    rm(tagged,sum_data)

  } # end for replicate

} # end for param_set

#output results
write.table(results, file = sprintf("%s_summary.csv", filename) )