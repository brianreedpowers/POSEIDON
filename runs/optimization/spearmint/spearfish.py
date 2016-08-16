from __future__ import print_function
import subprocess
import os
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/spearmint"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/fronts"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/mpa"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/hyperstability"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/gas_prices"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/gearopt"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/hardswitch/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/itq_mileage/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/race/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/location_itq/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/gear_itq/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/optimiser/tac-separated/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/kalman_tune/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/kalman_tune/kernel/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/rbf/"
EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/rbf/itq_plan/"

SPEARMINT_DIRECTORY = "/home/carrknight/code/Spearmint/spearmint"



def default_scorer(yamlfile):
    '''
    given the yaml file containing the results, extract a number representing the score for the run!
    :param yamlfile: the results.yaml created by the simulation
    :return: the score of this simulation, the lower the better.
    '''
    return -float(yamlfile["FishState"]["Biomass Species 1"][-1])

# one dimensional function
def run_experiment(input2yaml,
                   experiment_title,
                   scorer=default_scorer,
                   jarfile="yamler.jar",
                   main_directory="/home/carrknight/code/oxfish/runs/optimization",
                   years_to_run=20,
                   additional_data=False,
                   policy_file = None):
    import os
    import subprocess
    os.chdir(main_directory)
    # each row is a run
    print("running input2yaml")
    input2yaml(main_directory + "/" + experiment_title + ".yaml")
    print("calling java!")
# feed it into the simulation and run it
    args = ["java", "-jar", jarfile, experiment_title + ".yaml", "--years", str(years_to_run)]
    if additional_data:
        args.append("--data")
    if policy_file is not None:
        args.append("--policy")
        args.append(policy_file)
    subprocess.call(args)
    # read up the results
    os.remove(experiment_title + ".yaml")
    print("reading results")
    import yaml
    results = yaml.load(open(main_directory + "/output/" + experiment_title + "/result.yaml"))
    result = scorer(results)
    print("result " + str(result))
    # append them in a list of outputs
    return result


# find the experiment name and create directory from it
def main():
    import json
    os.chdir(EXPERIMENT_DIRECTORY)
    experiment_name = json.load(open("config.json"))["experiment-name"]
    print("starting " + experiment_name)


    ##connect mongo to itx
  # subprocess.call(["mongod", "--fork", "--logpath", dbDirectory + "/log_"+experiment_name+".txt", "--dbpath", dbDirectory])

    ##now run spearmint
    os.chdir(SPEARMINT_DIRECTORY)
    subprocess.call(["python2", "main.py", EXPERIMENT_DIRECTORY])

    #now plot
    #output_to_r.plot()

if __name__ == "__main__":
    main()