package uk.ac.ox.oxfish.maximization;

import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CaliforniaDerisoOptimization extends SimpleProblemDouble {


    ///home/carrknight/code/oxfish/docs/groundfish/calibration/step1_catchability/logit
    private String scenarioFile =
            Paths.get("docs","groundfish","calibration","step1_catchability",
                    "logit","start_garbage.yaml").toString();


    private String summaryDirectory =
            Paths.get("docs","groundfish","calibration",
                    "step1_catchability","logit").toString();

    private long seed = 2;



    private int yearsToRun = 5;

    private int yearsToIgnore = 1;


    //TARGETS
    private static final double YELLOW_QUOTA = 600.0;
    private static final double DOVER_QUOTA = 22234500;
    private static final double LONGSPINE_QUOTA = 1966250.0;
    private static final double SABLEFISH_QUOTA = 2724935;
    private static final double SHORTSPINE_QUOTA = 1481600.056;
    private static final double[] YELLOW_ATTAINMENT = new double[]{6.6, 2};
    private static final double[] DOVER_ATTAINMENT = new double[]{33.25 , 3.09};
    private static final double[] LONGSPINE_ATTAINMENT = new double[]{51.5 , 5.06 };
    private static final double[] SHORTSPINE_ATTAINMENT = new double[]{52.5 , 5.06 };
    private static final double[] SABLEFISH_ATTAINMENT = new double[]{83.65, 6.181};
    private static final double[] HOURS_AT_SEA = new double[]{999.936, 120.382023907226};
    private static final double[] PROFITS = new double[]{89308, 21331};
    private static final double[] DISTANCE = new double[]{90.88762, 32};
    private static final double[] DURATION = new double[]{69.097625, 33};



    private int runsPerSetting = 1;



    /**
     * list of all parameters that can be changed
     */
    private List<OptimizationParameter> parameters = new LinkedList<>();

    private static final double MINIMUM_CATCHABILITY = 1.0e-05;
    private static final double MAXIMUM_CATCHABILITY = 1.0e-03;

    {

        parameters.add(new SimpleOptimizationParameter(
                "gear.delegate.gears~Dover Sole.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));

        parameters.add(new SimpleOptimizationParameter(
                "gear.delegate.gears~Longspine Thornyhead.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));

        parameters.add(new SimpleOptimizationParameter(
                "gear.delegate.gears~Sablefish.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));

        parameters.add(new SimpleOptimizationParameter(
                "gear.delegate.gears~Shortspine Thornyhead.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));

        parameters.add(new SimpleOptimizationParameter(
                "gear.delegate.gears~Yelloweye Rockfish.averageCatchability",
                MINIMUM_CATCHABILITY,
                MAXIMUM_CATCHABILITY
        ));

//        parameters.add(new SimpleOptimizationParameter(
//                "gear.proportionSimulatedToGarbage",
//                0,
//                0.5
//        ));


        parameters.add(new SimpleOptimizationParameter(
                "holdSizePerBoat",
                1500,
                15000
        ));

                for (OptimizationParameter parameter : parameters) {
            ((SimpleOptimizationParameter) parameter).setAlwaysPositive(true);
        }


//        parameters.add(new SimpleOptimizationParameter(
//                "destinationStrategy.betaDistance",
//
//                -0.01,
//                -0.001
//        ));
//
//        parameters.add(new SimpleOptimizationParameter(
//                "destinationStrategy.intercept",
//                0,
//                100
//        ));
//
//        parameters.add(new SimpleOptimizationParameter(
//                "destinationStrategy.betaHabit",
//                -3,
//                3
//        ));
//        parameters.add(new SimpleOptimizationParameter(
//                "destinationStrategy.betaRevenue",
//                0,
//                1
//        ));
//        parameters.add(new SimpleOptimizationParameter(
//                "destinationStrategy.betaCPUE~Dover Sole",
//                -1,
//                1
//        ));
//        parameters.add(new SimpleOptimizationParameter(
//                "destinationStrategy.betaCPUE~Sablefish",
//                -1,
//                1
//        ));
//        parameters.add(new SimpleOptimizationParameter(
//                "destinationStrategy.betaCPUE~Yelloweye Rockfish",
//                -1,
//                1
//        ));


    }

    @Override
    public double[] evaluate(double[] x) {

        try {
            double error = 0;
            Path scenarioPath = Paths.get(scenarioFile);


            for(int i=0; i<runsPerSetting; i++)
            {
                FishYAML yaml = new FishYAML();



                Scenario scenario = yaml.loadAs(new FileReader(Paths.get(scenarioFile).toFile()),Scenario.class);
                prepareScenario(x, scenario);


                FishState model = new FishState(System.currentTimeMillis());
                model.setScenario(scenario);
                model.start();
                System.out.println("starting run");
                while (model.getYear() < yearsToRun) {
                    model.schedule.step(model);
                }
                model.schedule.step(model);


                //catches errors
                double soleError = deviationAttainment(
                        model.getYearlyDataSet().getColumn("Dover Sole Landings"),
                        DOVER_QUOTA,
                        DOVER_ATTAINMENT[0],
                        DOVER_ATTAINMENT[1],
                        1);
                error +=
                        soleError;
                double longspineLandings = deviationAttainment(
                        model.getYearlyDataSet().getColumn("Longspine Thornyhead Landings"),
                        LONGSPINE_QUOTA,
                        LONGSPINE_ATTAINMENT[0],
                        LONGSPINE_ATTAINMENT[1],
                        1);
                error +=
                        longspineLandings;
                double shortspineLanding = deviationAttainment(
                        model.getYearlyDataSet().getColumn("Shortspine Thornyhead Landings"),
                        SHORTSPINE_QUOTA,
                        SHORTSPINE_ATTAINMENT[0],
                        SHORTSPINE_ATTAINMENT[1],
                        1);
                error +=
                        shortspineLanding;

                double rockfishLandings = deviationAttainment(
                        model.getYearlyDataSet().getColumn("Yelloweye Rockfish Landings"),
                        YELLOW_QUOTA,
                        YELLOW_ATTAINMENT[0],
                        YELLOW_ATTAINMENT[1],
                        1);
                error +=
                        rockfishLandings;

                double sablefishLandings = deviationAttainment(
                        model.getYearlyDataSet().getColumn("Sablefish Landings"),
                        SABLEFISH_QUOTA,
                        SABLEFISH_ATTAINMENT[0],
                        SABLEFISH_ATTAINMENT[1],
                        1);
                error +=
                        sablefishLandings;


                double actualAverageHoursOut = deviation(
                        model.getYearlyDataSet().getColumn("Actual Average Hours Out"),
                        HOURS_AT_SEA[0],
                        HOURS_AT_SEA[1],
                        1
                );
                error +=
                        actualAverageHoursOut;

                double cashflow = deviation(
                        model.getYearlyDataSet().getColumn("Average Cash-Flow"),
                        PROFITS[0],
                        PROFITS[1],
                        1
                );
                error +=
                        cashflow;

                double duration = deviation(
                        model.getYearlyDataSet().getColumn("Average Trip Duration"),
                        DURATION[0],
                        DURATION[1],
                        1
                );
                error +=
                        duration;

                double distanceFromPort = deviation(
                        model.getYearlyDataSet().getColumn("Average Distance From Port"),
                        DISTANCE[0],
                        DISTANCE[1],
                        1
                );
                error +=
                        distanceFromPort;


                Files.write(
                        Paths.get(summaryDirectory).resolve(
                                scenarioPath.getFileName() + "_all_errors_"+seed+".csv"
                        ),
                        (
                                soleError +"," +
                                        longspineLandings +"," +
                                        shortspineLanding +"," +
                                        rockfishLandings +"," +
                                        sablefishLandings +"," +
                                        actualAverageHoursOut +"," +
                                        cashflow +"," +
                                        duration +"," +
                                        distanceFromPort +"," +

                                        Arrays.toString(x).
                                                replace("[","").
                                                replace("]","") +"\n").getBytes(),
                        StandardOpenOption.WRITE,StandardOpenOption.CREATE,StandardOpenOption.APPEND
                );

            }

            error /= (double) runsPerSetting;
            //write summary file
            Files.write(
                    Paths.get(summaryDirectory).resolve(
                            scenarioPath.getFileName() + "_"+seed+".csv"
                    ),
                    (error +"," + Arrays.toString(x).
                            replace("[","").
                            replace("]","") +"\n").getBytes(),
                    StandardOpenOption.WRITE,StandardOpenOption.CREATE,StandardOpenOption.APPEND
            );


            return new double[]{error};


        } catch (IOException  e) {
            e.printStackTrace();
            throw new RuntimeException("failed to read input file!");
        }


    }

    public void prepareScenario(double[] evaParameters, Scenario justReadScenario) {
        int parameter=0;
        for (OptimizationParameter optimizationParameter : parameters)
        {
            optimizationParameter.parametrize(justReadScenario,
                    Arrays.copyOfRange(evaParameters,parameter,
                            parameter+optimizationParameter.size()));
            parameter+=optimizationParameter.size();
        }
    }


    public static void main(String[] args) throws IOException {
        double[] best = new double[]{
                -6.54989698953394,
                2.4101791878656	,
                7.36414452079267,
                2.18409562587165,
                -2.38602534330794,
                5.31933183001022,
                2.06909287377259


        };

        CaliforniaDerisoOptimization optimization = new CaliforniaDerisoOptimization();
        FishYAML yaml = new FishYAML();



        Scenario scenario = yaml.loadAs(
                new FileReader(Paths.get(optimization.scenarioFile).toFile()),
                Scenario.class);
        optimization.prepareScenario(best,scenario);
        yaml.dump(scenario,
                new FileWriter(Paths.get(optimization.summaryDirectory).resolve("best_garbage_fleetwide.yaml").toFile()));


    }


    //computes abs(x-mu)/sigma
    public static double deviation(DataColumn data,
                                   double target,
                                   double standardDeviation,
                                   int yearsToSkip)
    {

        return Math.abs(
                FishStateUtilities.getAverage(data,yearsToSkip) - target
        )/standardDeviation;

    }

    // abs(100*data/quota-attainment)/standardDeviation
    public static double deviationAttainment(DataColumn data,
                                             double quota,
                                             double attainment,
                                             double standardDeviation,
                                             int yearsToSkip)
    {

        attainment = attainment/100d;
        standardDeviation = standardDeviation/100d;
        return Math.abs(
                FishStateUtilities.getAverage(data,yearsToSkip)/quota - attainment
        )/standardDeviation;



    }

    @Override
    public int getProblemDimension() {
        //5 catchabilities + (1 garbage) + 1 hold size
        return parameters.size();
    }

    public String getScenarioFile() {
        return scenarioFile;
    }

    public void setScenarioFile(String scenarioFile) {
        this.scenarioFile = scenarioFile;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public String getSummaryDirectory() {
        return summaryDirectory;
    }

    public void setSummaryDirectory(String summaryDirectory) {
        this.summaryDirectory = summaryDirectory;
    }


    public int getYearsToRun() {
        return yearsToRun;
    }

    public void setYearsToRun(int yearsToRun) {
        this.yearsToRun = yearsToRun;
    }

    public int getYearsToIgnore() {
        return yearsToIgnore;
    }

    public void setYearsToIgnore(int yearsToIgnore) {
        this.yearsToIgnore = yearsToIgnore;
    }



    public int getRunsPerSetting() {
        return runsPerSetting;
    }

    public void setRunsPerSetting(int runsPerSetting) {
        this.runsPerSetting = runsPerSetting;
    }


    public List<OptimizationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<OptimizationParameter> parameters) {
        this.parameters = parameters;
    }
}

