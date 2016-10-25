package uk.ac.ox.oxfish.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LeastSquareFilter;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;

/**
 * Basically the poor man's version of approximate post-decision dynamic programming using the dumbest state approximation (linear)
 * and the worst post-decision conflation ( a different linear function per action taken). <br>
 *     While I see this being useful only once (and thus, a separate object being more of an overkill) I want to keep this
 *     object separate so that it's easily removed when a grown-up library is used.
 * Created by carrknight on 10/12/16.
 */
public class AmateurishApproximateDynamicProgram {


    /**
     * how many actions are possible
     */
    private final int possibleActions;

    /**
     * each array is the "beta"s of the linear approximation
     */
    private final LeastSquareFilter[] filters;

    /**
     * Usually called "alpha"
     */
    private double learningRate;

    public AmateurishApproximateDynamicProgram(int possibleActions, int valueFunctionDimension, double learningRate) {
        this.possibleActions = possibleActions;
        filters = new LeastSquareFilter[possibleActions];
        for(int i=0; i<possibleActions; i++)
            filters[i] = new LeastSquareFilter(valueFunctionDimension,10000,new double[valueFunctionDimension],.999);
        this.learningRate = learningRate;
    }

    /**
     * returns the value function of being at a state (summarized by the state variables) and taking an action (identified by the action number)
     * @param action the action id
     * @param stateFeatures the measurements summarising the state we are at
     * @return a number, the higher the better
     */
    public double judgeAction(int action, double... stateFeatures){

        Preconditions.checkArgument(stateFeatures.length == filters[action].getBeta().length, "dimension mismatch");
        double value = 0;
        for(int i=0; i<stateFeatures.length; i++)
            value+= stateFeatures[i] * filters[action].getBeta()[i];
        return value;
    }


    /**
     * go through all the actions and choose the one with highest value given the current features
     * @param stateFeatures the features of the state
     * @return the action commanding the highest value and the value function
     */
    public Pair<Integer,Double> chooseBestAction(double... stateFeatures)
    {
        int bestAction = 0;
        double highestValue = judgeAction(0,stateFeatures);
        for(int i=1; i<possibleActions; i++)
        {
            double value = judgeAction(i,stateFeatures);
            if(value > highestValue)
            {
                bestAction=i;
                highestValue = value;
            }
        }

        return  new Pair<>(bestAction,highestValue);

    }


    /**
     * just an easier to use version of updateAction() which this method actually call.
     * @param actionTaken the action you took
     * @param instantaneousReward just the instantaneous reward achieved after having taken actionTake
     * @param featuresAtTheTime the state of the world when you took your previous action
     */
    public void updateActionDueToImmediateReward(int actionTaken, double instantaneousReward, double discountFactor,
                                                 double[] featuresAtTheTime, double[] featuresNow)
    {
        Pair<Integer, Double> currentBest = chooseBestAction(featuresNow);
        updateAction(actionTaken,instantaneousReward + discountFactor *  currentBest.getFirst(),featuresAtTheTime);

    }

    /**
     * you previously took "actionTaken", the reward observation from doing that (reward + max best state now) helps us
     * update the linear parameters associated with the previous action
     * @param actionTaken the action you took last time
     * @param rewardObservation the immediate reward you got plus the max value of the state where you are now
     * @param featuresAtTheTime the features describing the state when the last decision was taken!
     */
    public void updateAction(int actionTaken, double rewardObservation,
                             double... featuresAtTheTime)
    {


        //get back your old prediction
        double predictedValue = judgeAction(actionTaken,featuresAtTheTime);


        filters[actionTaken].addObservation(featuresAtTheTime,rewardObservation,1/learningRate);
        //w = w + a * (sample - prediction) * feature
        double[] parameters = filters[actionTaken].getBeta();
        for(int i=0; i<parameters.length; i++)
            Preconditions.checkState (Double.isFinite(parameters[i]), rewardObservation + " " + predictedValue + " " + featuresAtTheTime[i] + "\n " + this);
        Preconditions.checkArgument(featuresAtTheTime.length == parameters.length, "dimension mismatch");


    }




    @Override
    public String toString() {
        return FishStateUtilities.deepToStringArray(filters," ","\n");
    }

    /**
     * Getter for property 'linearParameters'.
     *
     * @return Value for property 'linearParameters'.
     */
    public double[][] getLinearParameters() {

        double[][] toReturn = new double[possibleActions][];
        for(int i=0; i<possibleActions; i++)
            toReturn[i] = filters[i].getBeta();

        return toReturn;
    }
}