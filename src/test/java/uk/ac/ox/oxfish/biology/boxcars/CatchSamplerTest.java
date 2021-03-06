package uk.ac.ox.oxfish.biology.boxcars;

import com.beust.jcommander.internal.Lists;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CatchSamplerTest {


    @Test
    public void catchSampler() {

        Fisher yesOne = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Fisher yesTwo = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Fisher wrong = mock(Fisher.class,RETURNS_DEEP_STUBS);

        Species species = new Species(
                "test",
                new FromListMeristics(new double[]{1,2},new double[]{10,100},1)
        );

        FishState model = mock(FishState.class);
        ObservableList fisherList = FXCollections.observableList(Lists.newArrayList(yesOne,yesTwo,wrong));
        when(model.getFishers()).thenReturn(fisherList);

        //one caught 10 small ones
        //two caught 5 big ones
        //wrong caught 100 of both
        when(yesOne.getDailyCounter().getSpecificLandings(species,0,0)).thenReturn(10d); //WEIGHT 10 individual
        when(yesOne.getDailyCounter().getSpecificLandings(species,0,1)).thenReturn(0d);
        when(yesTwo.getDailyCounter().getSpecificLandings(species,0,0)).thenReturn(0d);
        when(yesTwo.getDailyCounter().getSpecificLandings(species,0,1)).thenReturn(10d); //WEIGHT 5 individual
        when(wrong.getDailyCounter().getSpecificLandings(species,0,0)).thenReturn(100d);
        when(wrong.getDailyCounter().getSpecificLandings(species,0,1)).thenReturn(200d);

        CatchSampler sampler = new CatchSampler(new Predicate<Fisher>() {
            @Override
            public boolean test(Fisher fisher) {
                return fisher!=wrong;
            }
        },species,null);

        sampler.checkWhichFisherToObserve(model);
        sampler.observe();
        double[][] sampledAbundance = sampler.getAbundance();
        assertEquals(sampledAbundance[0][0],10,.01);
        assertEquals(sampledAbundance[0][1],5,.01);

        //doesn't reset automatically
        sampler.observe();
        sampledAbundance = sampler.getAbundance();
        assertEquals(sampledAbundance[0][0],20,.01);
        assertEquals(sampledAbundance[0][1],10,.01);

        //feed it the wrong weight and you get the wrong count
        sampledAbundance = sampler.getAbundance(new Function<Pair<Integer, Integer>, Double>() {
            @Override
            public Double apply(Pair<Integer, Integer> integerIntegerPair) {
                return 1d;
            }
        });
        assertEquals(sampledAbundance[0][0],20,.01);
        assertEquals(sampledAbundance[0][1],20,.01);

        //reset works
        sampler.resetLandings();
        sampledAbundance = sampler.getAbundance();
        assertEquals(sampledAbundance[0][0],0,.01);
        assertEquals(sampledAbundance[0][1],0,.01);
    }
}