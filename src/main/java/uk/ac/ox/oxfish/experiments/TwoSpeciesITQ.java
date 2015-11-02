package uk.ac.ox.oxfish.experiments;

import sim.display.Console;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.regs.factory.ITQMultiFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 2 Species ITQ in a well-mixed world
 * Created by carrknight on 10/9/15.
 */
public class TwoSpeciesITQ
{

    public static void firstdemo(String[] args)
    {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        ITQMultiFactory multiFactory = new ITQMultiFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
        //biomass ratio: 70-30
        WellMixedBiologyFactory biologyFactory = new WellMixedBiologyFactory();
        biologyFactory.setCapacityRatioSecondToFirst(new FixedDoubleParameter(.3));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);


        scenario.setUsePredictors(true);

        //make species 2 worthless
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                List<Market> markets = state.getAllMarketsForThisSpecie(state.getSpecies().get(1));
                assert markets.size() == 1;
                ((FixedPriceMarket) markets.get(0)).setPrice(0d);
            }

            @Override
            public void turnOff() {

            }
        });


        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);

        FishStateUtilities.printCSVColumnToFile(state.getYearlyDataSet().getColumn(state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME),
                                                Paths.get("docs","20151009 lambda3","red_landings.csv").toFile());

        FishStateUtilities.printCSVColumnToFile(state.getYearlyDataSet().getColumn(state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME),
                                                Paths.get("docs","20151009 lambda3","blue_landings.csv").toFile());

        FishStateUtilities.printCSVColumnToFile(state.getDailyDataSet().getColumn("ITQ Last Closing Price Of Specie " + 0),
                                                Paths.get("docs","20151009 lambda3","red_quotas.csv").toFile());

        FishStateUtilities.printCSVColumnToFile(state.getDailyDataSet().getColumn("ITQ Last Closing Price Of Specie " + 1),
                                                Paths.get("docs","20151009 lambda3","blue_quotas.csv").toFile());
    }




    public static void firstGui(String[] args)
    {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        ITQMultiFactory multiFactory = new ITQMultiFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
        //biomass ratio: 70-30
        WellMixedBiologyFactory biologyFactory = new WellMixedBiologyFactory();
        biologyFactory.setCapacityRatioSecondToFirst(new FixedDoubleParameter(.3));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);


        scenario.setUsePredictors(true);

        //make species 2 worthless
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                List<Market> markets = state.getAllMarketsForThisSpecie(state.getSpecies().get(1));
                assert markets.size() == 1;
                ((FixedPriceMarket) markets.get(0)).setPrice(0d);
            }

            @Override
            public void turnOff() {

            }
        });

        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);

    }


    public static void main(String[] args)
    {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        ITQMultiFactory multiFactory = new ITQMultiFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));

        HalfBycatchFactory biologyFactory = new HalfBycatchFactory();
        biologyFactory.setCarryingCapacity(new FixedDoubleParameter(5000));
        biologyFactory.setSteepness(new FixedDoubleParameter(.9));
        biologyFactory.setDifferentialPercentageToMove(new FixedDoubleParameter(.2));
        biologyFactory.setPercentageLimitOnDailyMovement(new FixedDoubleParameter(.2));

        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);

        scenario.setCoastalRoughness(0);
        scenario.forcePortPosition(new int[]{40,25});
        //try also 40,25



        scenario.setUsePredictors(true);



        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);

    }


        public static void secondDemo(String[] args)
    {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        ITQMultiFactory multiFactory = new ITQMultiFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));

        HalfBycatchFactory biologyFactory = new HalfBycatchFactory();
        biologyFactory.setCarryingCapacity(new FixedDoubleParameter(5000));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);

        scenario.setCoastalRoughness(0);
        scenario.forcePortPosition(new int[]{40,25});
        //try also 40,25



        scenario.setUsePredictors(true);



        state.start();
        while(state.getYear()<10)
            state.schedule.step(state);

        Path directory = Paths.get("docs", "20151014 corollaries");

        //show the effect on catches
        FishStateUtilities.printCSVColumnToFile(state.getYearlyDataSet().getColumn(state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME),
                                                directory.resolve("geo_red_landings.csv").toFile());

        FishStateUtilities.printCSVColumnToFile(state.getYearlyDataSet().getColumn(state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME),
                                                directory.resolve("geo_blue_landings.csv").toFile());

        FishStateUtilities.printCSVColumnToFile(state.getYearlyDataSet().getColumn("# of North Tows"),
                                                directory.resolve("north.csv").toFile());

        FishStateUtilities.printCSVColumnToFile(state.getYearlyDataSet().getColumn("# of South Tows"),
                                                directory.resolve("south.csv").toFile());





    }

    public static void thirdGUI(String[] args)
    {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        ITQMultiFactory multiFactory = new ITQMultiFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));

        HalfBycatchFactory biologyFactory = new HalfBycatchFactory();
        biologyFactory.setCarryingCapacity(new FixedDoubleParameter(5000));
        biologyFactory.setSteepness(new FixedDoubleParameter(.9));
        biologyFactory.setDifferentialPercentageToMove(new FixedDoubleParameter(.2));
        biologyFactory.setPercentageLimitOnDailyMovement(new FixedDoubleParameter(.2));

        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);

        scenario.setCoastalRoughness(0);
        scenario.forcePortPosition(new int[]{40,25});
        //try also 40,25



        scenario.setUsePredictors(true);



        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);

    }

}