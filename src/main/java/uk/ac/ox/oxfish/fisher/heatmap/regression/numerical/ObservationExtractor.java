package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * class that gets an observation (Seatile,Time,Fisher,Fishstate) and returns a number representing the feature to extract
 * Created by carrknight on 8/18/16.
 */
public interface ObservationExtractor
{

    /**
     * takes a series of extractors and returns it as an array of numerical features
     */
    static  double[] convertToFeatures(
            SeaTile tile, double time,
            Fisher fisher, ObservationExtractor[] extractors)
    {
        double[] observation = new double[extractors.length];
        for(int i=0; i<observation.length; i++)
            observation[i] = extractors[i].extract(tile, time, fisher);
        return observation;
    }

    public double extract(SeaTile tile, double timeOfObservation, Fisher agent);

}