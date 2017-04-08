
args <- commandArgs(TRUE)
filename <- as.character(args[1])
first_id <- as.integer(args[2])
last_id <- as.integer(args[3])

ids <- seq(first_id, last_id)

accumulated <- NULL
for (id in ids) {
	data <- read.table(sprintf("%i/%s",id,filename), header=TRUE)
	accumulated <- rbind(accumulated,data)
}

out_filename <- sprintf("%s_%i-%i.table",filename,first_id,last_id)

write.table(accumulated, file = out_filename)
