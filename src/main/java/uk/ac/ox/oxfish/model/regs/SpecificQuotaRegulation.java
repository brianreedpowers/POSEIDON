package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Quota for a single specie. If the specie sold is not the right one, then ignore
 * Created by carrknight on 9/22/15.
 */
public class SpecificQuotaRegulation extends MonoQuotaRegulation {


    /**
     * specie to protect by quota
     */
    private final Specie protectedSpecie;

    /**
     * when created it sets itself to step every year to reset the quota
     *
     * @param yearlyQuota the yearly quota
     * @param state       the model link to schedule on
     */
    public SpecificQuotaRegulation(double yearlyQuota, FishState state, Specie specie) {
        super(yearlyQuota, state);
        this.protectedSpecie = specie;
    }


    /**
     * Can this fisher be at sea?
     *
     */
    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model) {
        return true;
    }


    /**
     * You are allowed to fish, just never to sell the protected quota if you are wrong
     */
    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model) {
        return true;
    }

    /**
     * ignore if wrong specie.
     */
    @Override
    public void reactToSale(Specie specie, Fisher seller, double biomass, double revenue) {
        if(specie == protectedSpecie)
            super.reactToSale(specie, seller, biomass, revenue);
    }


    public Specie getProtectedSpecie() {
        return protectedSpecie;
    }
}