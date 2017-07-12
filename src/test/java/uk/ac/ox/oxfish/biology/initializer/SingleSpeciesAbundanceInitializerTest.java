package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.factory.NoDiffuserFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SingleSpeciesAbundanceInitializerTest {


    @Test
    public void allocatesRecruitsCorrectly() throws Exception {
        FishState model = MovingTest.generateSimple4x4Map();
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        SingleSpeciesAbundanceFactory factory = new SingleSpeciesAbundanceFactory();
        factory.setDiffuser(new NoDiffuserFactory());
        factory.setRecruitAllocator(
                new AlgorithmFactory<BiomassAllocator>() {
                    @Override
                    public BiomassAllocator apply(FishState state) {
                        return new BiomassAllocator() {
                            @Override
                            public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {
                                if(tile.getGridX()==0 && tile.getGridY()==0)
                                    return 1d;
                                else
                                    return 0d;
                            }
                        };
                    }
                }
        );

        SingleSpeciesAbundanceInitializer initializer = factory.apply(model);


        //put biology in there
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        NauticalMap map = model.getMap();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            element.setBiology(initializer.generateLocal(biology,
                                                         element, new MersenneTwisterFast(),4, 4,
                                                         mock(NauticalMap.class)
            )); //put new biology in
        }
        //by default the abundance initializer splits total count uniformly
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);


        //starts with all the same
        for(int x=0; x<map.getWidth(); x++)
            for(int y=0; y<map.getWidth(); y++)
            {
                if( x==0 && y==0 )
                    continue;
                assertEquals(map.getSeaTile(x,y).getBiomass(biology.getSpecie(0)),
                                   map.getSeaTile(0,0).getBiomass(biology.getSpecie(0)),
                             .01

                );
            }

        initializer.getProcesses().start(model);
        initializer.getProcesses().step(model);

        //recruits congregate in that one spot, so there ought to be more fish over there
        for(int x=0; x<map.getWidth(); x++)
            for(int y=0; y<map.getWidth(); y++)
            {
                if( x==0 && y==0 )
                    continue;
                assertTrue(map.getSeaTile(x,y).getBiomass(biology.getSpecie(0))<
                                   map.getSeaTile(0,0).getBiomass(biology.getSpecie(0))
                );
            }
    }

    @Test
    public void readsCorrectly() throws Exception {


        //create a 4x4 map of the world.
        FishState model = MovingTest.generateSimple4x4Map();
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());

        Path testInput = Paths.get("inputs","tests","abundance","fake");
        Log.info("I pass the directory " + testInput + " to the single species initializer. That directory contains a fake simple species of which I know all the characteristic" +
                         "and I make sure the initializer instantiates correctly.");

        //create an initializer (scales to double the number from file)
        SingleSpeciesAbundanceInitializer initializer = new SingleSpeciesAbundanceInitializer(testInput,"fake",2.0,model);
        //create biology object
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        assertEquals(1,biology.getSpecies().size());
        Species fakeSpecies = biology.getSpecie(0);
        assertEquals("fake", fakeSpecies.getName());
        assertEquals(3, fakeSpecies.getMaxAge());


        //put biology in there
        NauticalMap map = model.getMap();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            element.setBiology(initializer.generateLocal(biology,
                    element, new MersenneTwisterFast(),4, 4,
                                                         mock(NauticalMap.class)
            )); //put new biology in
        }
        //by default the abundance initializer splits total count uniformly
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(1,1).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(2,3).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        assertEquals(325,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);
        assertEquals(325,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);

    }
}