package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.HillClimberDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * factory that creates an hill-climber strategy with a random starting point
 */
public class HillClimberDestinationFactory implements StrategyFactory<HillClimberDestinationStrategy>
{

    DoubleParameter stepSize = new FixedDoubleParameter(5d);



    @Override
    public HillClimberDestinationStrategy apply(FishState state) {

        MersenneTwisterFast random = state.random;
        NauticalMap map = state.getMap();

        final HillClimberDestinationStrategy strategy = new HillClimberDestinationStrategy(map, random);
        strategy.setMaxStepSize(stepSize.apply(random).intValue());
        return strategy;

    }

    public DoubleParameter getStepSize() {
        return stepSize;
    }

    public void setStepSize(DoubleParameter stepSize) {
        this.stepSize = stepSize;
    }
}