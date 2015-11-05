package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;


public class IQMonoFactoryTest {
    @Test
    public void testITQ() throws Exception {

        //similar to the TAC test, but the results are different


        IQMonoFactory factory = new IQMonoFactory();
        FishState state = new FishState(System.currentTimeMillis());

        factory.setIndividualQuota(new FixedDoubleParameter(200));
        final MonoQuotaRegulation tac1 = factory.apply(state);
        assertEquals(tac1.getYearlyQuota(),200,.0001);
        assertEquals(tac1.getQuotaRemaining(0),200,.0001);
        //create a second one, same parameters but they aren't the same object
        final MonoQuotaRegulation tac2 = factory.apply(state);
        assertEquals(tac1.getYearlyQuota(),200,.0001);
        assertEquals(tac1.getQuotaRemaining(0),200,.0001);
        assertEquals(tac2.getYearlyQuota(),200,.0001);
        assertEquals(tac2.getQuotaRemaining(0),200,.0001);
        assertNotEquals(tac1,tac2);

        //consume a bit of the second, it will NOT affect the first
        tac2.reactToSale(mock(Species.class), mock(Fisher.class) , 100, 1234);
        assertEquals(tac1.getYearlyQuota(),200,.0001);
        assertEquals(tac1.getQuotaRemaining(0),200,.0001);
        assertEquals(tac2.getYearlyQuota(),200,.0001);
        assertEquals(tac2.getQuotaRemaining(0),100,.0001);

        //if I create a third tac, it will still be full and the second one will not replenish
        final MonoQuotaRegulation tac3 = factory.apply(state);
        assertEquals(tac1.getYearlyQuota(),200,.0001);
        assertEquals(tac1.getQuotaRemaining(0),200,.0001);
        assertEquals(tac2.getYearlyQuota(),200,.0001);
        assertEquals(tac2.getQuotaRemaining(0),100,.0001);
        assertEquals(tac3.getYearlyQuota(),200,.0001);
        assertEquals(tac3.getQuotaRemaining(0),200,.0001);

        //if I increase the yearly quota, it will affect only the new one
        factory.setIndividualQuota(new FixedDoubleParameter(300));
        final MonoQuotaRegulation tac4 = factory.apply(state);
        assertEquals(tac1.getYearlyQuota(),200,.0001);
        assertEquals(tac1.getQuotaRemaining(0),200,.0001);
        assertEquals(tac2.getYearlyQuota(),200,.0001);
        assertEquals(tac2.getQuotaRemaining(0),100,.0001);
        assertEquals(tac3.getYearlyQuota(),200,.0001);
        assertEquals(tac3.getQuotaRemaining(0),200,.0001);
        assertEquals(tac4.getYearlyQuota(),300,.0001);
        assertEquals(tac4.getQuotaRemaining(0),300,.0001);





    }
}