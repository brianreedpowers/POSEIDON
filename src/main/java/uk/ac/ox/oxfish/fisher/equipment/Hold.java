package uk.ac.ox.oxfish.fisher.equipment;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

/**
 * The inventory object of the ship. It has a maximum capacity. Any fish caught after reaching capacity is thrown overboard
 * Created by carrknight on 4/21/15.
 */
public class Hold {


    /**
     * pounds of fish currently transported
     */
    private double tonnesCarried = 0;

    /**
     * maximum pounds that can be held
     */
    private double maximumLoad = 0;

    private double[] fishHold;


    /**
     * create a new empty fishHold
     * @param maximumLoadInTonnes maximum capacity
     * @param numberOfSpecies number of species in the simulation
     */
    public Hold(double maximumLoadInTonnes, int numberOfSpecies)
    {
        this.maximumLoad = maximumLoadInTonnes;
        fishHold = new double[numberOfSpecies];
    }

    /**
     * store the catch
     * @param caught the catch
     */
    public void load(Catch caught)
    {
        //start loading up
        for(int i=0; i< fishHold.length; i++)
        {
            double poundsCaught = caught.getWeightCaught(i);
            fishHold[i] += poundsCaught;
            tonnesCarried += poundsCaught;
            assert  poundsCaught >=0;
        }

        assert tonnesCarried >=0;
        assert consistencyCheck();
        if(tonnesCarried > maximumLoad + FishStateUtilities.EPSILON/2)
            throwOverboard();
        assert  maximumLoad >= tonnesCarried || Math.abs(maximumLoad-tonnesCarried) <= FishStateUtilities.EPSILON;
    }

    /**
     * call this if you have loaded more than what you can carry. throws overboard catch proportionally
     */
    private void throwOverboard() {



        throwOverboard(fishHold,maximumLoad);
        tonnesCarried = Arrays.stream(fishHold).sum();
        assert Math.abs(tonnesCarried -maximumLoad)<=FishStateUtilities.EPSILON;
        assert Math.abs(getPercentageFilled() -1.0)<=FishStateUtilities.EPSILON;
    }

    /**
     * CAREFUL: modifies the argument!
     * @param fishHold
     * @param maximumLoad
     */
    public static void throwOverboard(double[] fishHold,double maximumLoad)
    {
        double currentLoad=  Arrays.stream(fishHold).sum();
        if(currentLoad>maximumLoad)
        {
            double proportionToKeep = maximumLoad / (currentLoad);
            assert proportionToKeep <= 1 && proportionToKeep >= 0 : proportionToKeep;
            for (int i = 0; i < fishHold.length; i++) {
                fishHold[i] *= proportionToKeep;
            }
        }
    }

    public double getTotalWeightOfCatchInHold() {
        return tonnesCarried;
    }

    public double getWeightOfCatchInHold(Species species) {
        return fishHold[species.getIndex()];
    }


    public double getMaximumLoad() {
        return maximumLoad;
    }

    public double getPercentageFilled()
    {
        return tonnesCarried /maximumLoad;
    }

   private boolean consistencyCheck()
   {
       double sum = 0;
       for(double pounds : fishHold)
           sum+=pounds;
       return  Math.abs(tonnesCarried-sum) < FishStateUtilities.EPSILON;
   }

    public Catch  unload()
    {
        Catch toReturn = new Catch(fishHold);
        fishHold = new double[fishHold.length];
        tonnesCarried = 0;
        assert consistencyCheck();
        return toReturn;
    }


    public Hold makeCopy()
    {
        Hold toReturn = new Hold(maximumLoad,0);
        toReturn.fishHold = Arrays.copyOf(fishHold,fishHold.length);
        toReturn.tonnesCarried = this.tonnesCarried;
        toReturn.maximumLoad = this.maximumLoad;
        return toReturn;
    }
}
