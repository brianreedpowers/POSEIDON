package uk.ac.ox.oxfish.model;

import com.esotericsoftware.minlog.Log;
import org.junit.Test;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.ScenarioEssentials;
import uk.ac.ox.oxfish.model.scenario.ScenarioPopulation;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class FishStateTest {

    @Test
    public void testScheduleEveryYear() throws Exception {

        //steps every 365 steps starting from 365
        Steppable steppable = mock(Steppable.class);

        FishState state = new FishState(1l);
        Scenario scenario = mock(Scenario.class);
        ScenarioEssentials result = mock(ScenarioEssentials.class);
        when(result.getBiology()).thenReturn(mock(GlobalBiology.class));
        when(scenario.start(state)).thenReturn(result);
        final ScenarioPopulation mock = mock(ScenarioPopulation.class);
        when(mock.getNetwork()).thenReturn(mock(SocialNetwork.class));
        when(scenario.populateModel(state)).thenReturn(mock);
        NauticalMap map = mock(NauticalMap.class); when(result.getMap()).thenReturn(map);
        when(map.getPorts()).thenReturn(new HashSet<>());

        state.setScenario(scenario);
        state.start();
        state.scheduleEveryYear(steppable, StepOrder.POLICY_UPDATE);
        state.scheduleEveryStep(simState -> {},StepOrder.AFTER_DATA);
        //should step twice
        for(int i=0; i<730; i++)
            state.schedule.step(state);
        verify(steppable,times(2)).step(state);

    }

    @Test
    public void testCreateFishers() throws Exception {

        Log.info("Testing that fishers can be created and destroyed");
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new FromLeftToRightFactory()); //faster
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(20));
        mapInitializer.setHeight(new FixedDoubleParameter(5));
        scenario.setMapInitializer(mapInitializer);
        scenario.setFishers(4);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();

        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);

        assertEquals(4,state.getFishers().size());
        assertTrue(state.canCreateMoreFishers());
        state.createFisher();
        state.createFisher();
        state.createFisher();
        state.schedule.step(state);
        assertEquals(7,state.getFishers().size());
        state.schedule.step(state);
        assertEquals(7,state.getFishers().size());
        state.killRandomFisher();
        state.killRandomFisher();
        assertEquals(5,state.getFishers().size());
        state.schedule.step(state);
        assertEquals(5,state.getFishers().size());

        Log.info("Testing that new fishers collect data just like the old ones");
        Fisher newguy = state.createFisher();
        for(int i=0; i<10; i++)
            state.schedule.step(state);
        assertEquals(10,newguy.getDailyData().numberOfObservations());



    }
}