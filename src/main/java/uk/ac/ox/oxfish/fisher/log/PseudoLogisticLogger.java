package uk.ac.ox.oxfish.fisher.log;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticInputMaker;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.bandit.BanditSwitch;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Tries to fill up a LogisticLog when you are not actually using a LogitDestination
 * Created by carrknight on 1/21/17.
 */
public class PseudoLogisticLogger implements TripListener {

    /**
     * the discretization allegedly used to make the logit choice
     */
    private final MapDiscretization discretization;

    /**
     * Needed to have an idea on the choices possible and to give them a number
     */
    private final BanditSwitch switcher;

    /**
     * you use this to simulate what the "x" would be when observed by the
     * destination strategies
     */
    private final LogisticInputMaker inputter;

    /**
     * you update this
     */
    private final LogisticLog log;


    private final Fisher fisher;

    private final FishState state;


    public PseudoLogisticLogger(
            MapDiscretization discretization,
            LogisticInputMaker inputter, LogisticLog log,
            Fisher fisher,
            FishState state,
            Set<Integer> allowedGroups) {
        this.discretization = discretization;
        //only model arms for which we have both at least a tile in the map AND is listed in the input file
        switcher = new BanditSwitch(discretization.getNumberOfGroups(),
                                    integer -> discretization.isValid(integer) && allowedGroups.contains(integer));

        this.inputter = inputter;
        this.log = log;
        this.fisher = fisher;
        this.state = state;
    }

    public PseudoLogisticLogger(
            MapDiscretization discretization,
            ObservationExtractor[] commonExtractors,
            LogisticLog log,
            Fisher fisher,
            FishState state,
            MersenneTwisterFast random) {
        this.discretization = discretization;
        //only model arms for which we have both at least a tile in the map AND is listed in the input file
        switcher = new BanditSwitch(discretization.getNumberOfGroups(),
                                    integer -> discretization.isValid(integer) );
        ObservationExtractor[][] extractors = new ObservationExtractor[switcher.getNumberOfArms()][];
        for(int arm = 0; arm<extractors.length; arm++)
            extractors[arm] = commonExtractors;

        this.inputter = new LogisticInputMaker(extractors, new Function<Integer, SeaTile>() {
            @Override
            public SeaTile apply(Integer arm) {
                List<SeaTile> group = discretization.getGroup(switcher.getGroup(arm));
                return group.get(random.nextInt(group.size()));
            }
        });
        this.log = log;
        this.fisher = fisher;
        this.state = state;
    }

    @Override
    public void reactToFinishedTrip(TripRecord record)
    {
        //if we recorded an input at the end of the last trip, now we reveal the choice
        if(log.waitingForChoice()) {
            //you have to turn the tile fished into the map group first and then from that to the bandit arm
            log.recordChoice(
                    switcher.getArm(
                            discretization.getGroup(
                                    record.getMostFishedTileInTrip())
                    )
            );
        }
        assert !log.waitingForChoice();
        log.recordInput(inputter.getRegressionInput(fisher,state));



    }

    /**
     * Getter for property 'discretization'.
     *
     * @return Value for property 'discretization'.
     */
    public MapDiscretization getDiscretization() {
        return discretization;
    }

    /**
     * Getter for property 'switcher'.
     *
     * @return Value for property 'switcher'.
     */
    public BanditSwitch getSwitcher() {
        return switcher;
    }

    /**
     * Getter for property 'inputter'.
     *
     * @return Value for property 'inputter'.
     */
    public LogisticInputMaker getInputter() {
        return inputter;
    }

    /**
     * Getter for property 'log'.
     *
     * @return Value for property 'log'.
     */
    public LogisticLog getLog() {
        return log;
    }
}