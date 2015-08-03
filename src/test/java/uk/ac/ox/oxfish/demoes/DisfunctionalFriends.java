package uk.ac.ox.oxfish.demoes;

import org.junit.Assert;
import org.junit.Test;

import static uk.ac.ox.oxfish.demoes.FunctionalFriendsDemo.stepsItTook;

/**
 * Created by carrknight on 8/3/15.
 */
public class DisfunctionalFriends {



    //another result I have is that exploration-imitation works poorly if everybody spends too much time
    //imitating and too little time exploring
    @Test
    public void disfunctionalFriends() throws Exception {


        long seed = System.currentTimeMillis();
        int stepsAlone = stepsItTook(Double.NaN,0,5000, seed);
        int stepsWithManyFriends = stepsItTook(.1,20,5000, seed);

        Assert.assertTrue(stepsAlone + " ---- " + stepsWithManyFriends, stepsAlone < stepsWithManyFriends);

        //in fact with many friends you don't even clear all the map after 3500 days
        Assert.assertEquals(5000, stepsWithManyFriends);
    }

}
