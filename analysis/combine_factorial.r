
args <- commandArgs(TRUE)
filename <- as.character(args[1])
first_id <- as.integer(args[2])
last_id <- as.integer(args[3])

ids <- seq(first_id, last_id)

accumulated <- NULL
for (id in ids) {
	f <- sprintf("%i/%s",id,filename)
	if (file.exists(f)) {
		data <- read.table(f, header=TRUE)
		accumulated <- rbind(accumulated,data)
	} else {
		print(id)
	}
}

out_filename <- sprintf("%s_%i-%i.table",filename,first_id,last_id)

write.table(accumulated, file = out_filename)
