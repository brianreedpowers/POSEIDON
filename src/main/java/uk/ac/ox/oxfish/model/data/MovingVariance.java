package uk.ac.ox.oxfish.model.data;

import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Deque;
import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p>  Here I am using the algorithm from: http://stackoverflow.com/a/14638138/975904
 * <p>  Basically a "fast" way to keep the variance measured
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-06
 * @see
 */
public class MovingVariance<T extends Number>{

    private double average = Double.NaN;

    private double variance = Double.NaN;


    private final Deque<T> observations = new LinkedList<>();


    private final int size;


    public MovingVariance(int size) {
        this.size = size;
    }

    /**
     * adds a new observation to the filter!
     *
     * @param newObservation a new observation!
     */
    public void addObservation(T newObservation) {

        assert observations.size() < size || (observations.size() == size && Double.isFinite(variance)) :
                variance +"----" + newObservation + " ---- " + (observations.size() < size);

        observations.addLast(newObservation);
        if(observations.size() == size && Double.isNaN(average))
        {
            average = computeBatchAverage();
            variance = computeInitialVarianceThroughCompensatedSummation(average);
        }
        else if(observations.size() > size)
        {
            //need to correct!
            double oldestValue = observations.pop().doubleValue();
            final double newValue = newObservation.doubleValue();
            double oldAverage = average;
            average = average + (newValue -oldestValue)/size;
            variance = variance +  (newValue-average + oldestValue-oldAverage)*(newValue - oldestValue)/(size);
             //might have to add a Max(0,variance) if there are numerical issues!
            assert Double.isFinite(variance) : average;
        }
    }

    /**
     * the variance. If variance is below .0001 it returns 0.
     *
     * @return the smoothed observation
     */
    public double getSmoothedObservation() {


        double currentVariance = variance;

        //use preliminary variance
        if(observations.size() >= 2 && Double.isNaN(variance))
            variance = computeInitialVarianceThroughCompensatedSummation(computeBatchAverage());

        //if variance is very small,
        if(variance< FishStateUtilities.EPSILON)
            return 0;
        return variance;
    }

    /**
     * If it can predict (that is, if I call getSmoothedObservation i don't get a NaN)
     *
     */
    public boolean isReady() {
        return Double.isFinite(variance);
    }


    private double computeBatchAverage(){
        double sum = 0;
        for(T n : observations )
            sum+= n.doubleValue();

        return sum/observations.size();
    }

    //from the wikipedia.
    private double computeInitialVarianceThroughCompensatedSummation(final double currentAverage){
        assert observations.size() == size ^ Double.isNaN(average);
        assert Double.isFinite(currentAverage);

        double squaredSum=0;
        double compensatingSum=0;
        for(T observation : observations )
        {
            squaredSum +=  Math.pow(observation.doubleValue()- currentAverage, 2);
            compensatingSum +=  observation.doubleValue()- currentAverage;
        }

        return (double) ((squaredSum-Math.pow(compensatingSum,2)/observations.size())/observations.size());

    }

    public double getAverage() {

        if(Double.isFinite(average))
            return average;
        else
            return computeBatchAverage();
    }


    public int getSize() {
        return size;
    }
}