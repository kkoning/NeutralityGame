import csv
import os
import sys

run_dir = "run"

if os.path.exists(run_dir):
    print("Run directory already exists")
    exit()

if len(sys.argv) != 2:
    print "Requires argument of template config file"
    exit()

if os.path.isfile(sys.argv[1]) == False:
    print "Parameter isn't a file?"
    exit()


param_template_filename = sys.argv[1]
print("Input config file was " + param_template_filename)

# template_file = open(param_template_filename, 'r')
# s = template_file.read()
# print(s)


# exit()

os.makedirs(run_dir)

# List of parameters / header of CSV file
headers = ['ID',
           'NUM_GENERATIONS',
		   'CONTENT_BUNDLING',
           'ZERO_RATING',
           'FORCED_ZERO_IC',
           'ALPHA',
           'BETA',
           'PSI',
           'TAU',
           'THETA',
           'NUM_STEPS',
           'NUM_CONSUMERS',
           'TOP_INCOME']

# Constants
psi = 0.4
tau = 0.4
theta = 0.2
num_steps = 10
num_consumers = 200
top_income = 1
num_generations = 400

# Variables
all_content_bundling = [0, 1]
all_zero_rating = [0, 1]
all_forced_zero_ic = [0, 1]
all_alpha = [0.1, 0.2, 0.5, 1, 2, 5, 10]
all_beta = [0.1, 0.2, 0.5, 1, 2, 5, 10]

# index
id = 1

with open('parameters.csv', 'wb') as csvfile:
    parmwriter = csv.writer(csvfile, quoting=csv.QUOTE_MINIMAL)
    parmwriter.writerow(headers)

    for content_bunding in all_content_bundling:
        for zero_rating in all_zero_rating:
            for forced_zero_ic in all_forced_zero_ic:
                for alpha in all_alpha:
                    for beta in all_beta:
                        parms = []
                        parms.append(id)
                        parms.append(content_bunding)
                        parms.append(num_generations)
                        parms.append(zero_rating)
                        parms.append(forced_zero_ic)
                        parms.append(alpha)
                        parms.append(beta)
                        parms.append(psi)
                        parms.append(tau)
                        parms.append(theta)
                        parms.append(num_steps)
                        parms.append(num_consumers)
                        parms.append(top_income)
                        parmwriter.writerow(parms)

                        m = {}
                        m['__NUM_GENERATIONS__'] = num_generations

                        if (content_bunding > 0):
                            m['__CONTENT_BUNDLING__'] = 'true'
                        else:
                            m['__CONTENT_BUNDLING__'] = 'false'

                        if (zero_rating > 0):
                            m['__ZERO_RATING__'] = 'true'
                        else:
                            m['__ZERO_RATING__'] = 'false'

                        if (forced_zero_ic > 0):
                            m['__FORCED_ZERO_IC__'] = 'true'
                        else:
                            m['__FORCED_ZERO_IC__'] = 'false'

                        m['__ALPHA__'] = alpha
                        m['__BETA__'] = beta
                        m['__PSI__'] = psi
                        m['__TAU__'] = tau
                        m['__THETA__'] = theta
                        m['__NUM_STEPS__'] = num_steps
                        m['__NUM_CONSUMERS__'] = num_consumers
                        m['__TOP_INCOME__'] = top_income

                        os.makedirs(run_dir + "/" + str(id))

                        template_file = open(param_template_filename, 'r')
                        run_file = open(run_dir + "/" + str(id) + "/Run.xml", 'wb')

                        template = template_file.read()
                        for k,v in m.iteritems():
                            template = template.replace(str(k),str(v))

                        run_file.write(template)

                        id += 1



# headers = ['ID',
#            'NUM_GENERATIONS',
# 		   'CONTENT_BUNDLING',
#            'ZERO_RATING',
#            'FORCED_ZERO_IC',
#            'ALPHA',
#            'BETA',
#            'PSI',
#            'TAU',
#            'THETA',
#            'NUM_STEPS',
#            'NUM_CONSUMERS',
#            'TOP_INCOME']
