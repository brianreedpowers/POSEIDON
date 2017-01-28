package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * The knife-edge version of cashflow objective. +1 utility if the cashflow changed by threshold or more, -1 otherwise
 * Created by carrknight on 1/28/17.
 */
public class KnifeEdgeCashflowObjective implements  ObjectiveFunction<Fisher>{


    private final double threshold;

    private final CashFlowObjective delegate;


    public KnifeEdgeCashflowObjective(double threshold, CashFlowObjective delegate) {
        this.threshold = threshold;
        this.delegate = delegate;
    }

    /**
     * compute current fitness of the agent
     *
     * @param observed agent whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    @Override
    public double computeCurrentFitness(Fisher observed) {
        return delegate.computeCurrentFitness(observed) >= threshold ? +1 : -1;
    }

    /**
     * compute the fitness of the agent "in the previous step"; How far back that is
     * depends on the objective function itself
     *
     * @param observed the agent whose fitness we want
     * @return a fitness value: the higher the better
     */
    @Override
    public double computePreviousFitness(Fisher observed) {
        return delegate.computePreviousFitness(observed) >= threshold ? +1 : -1;
    }


}
