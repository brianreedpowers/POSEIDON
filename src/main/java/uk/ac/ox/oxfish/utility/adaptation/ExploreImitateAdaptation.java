package uk.ac.ox.oxfish.utility.adaptation;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A general algorithm to perform exploration/imitation/exploitation decisions possibly on a specific variable
 * Created by carrknight on 8/6/15.
 */
public class ExploreImitateAdaptation<T> extends AbstractAdaptation<T> {


    /**
     * function to grab eligible friends
     */
    private Function<Pair<Fisher,MersenneTwisterFast>,Collection<Fisher>> friendsExtractor;



    /**
     * what the agent ought to do to adapt
     */
    private final AdaptationAlgorithm<T> algorithm;

    /**
     * how the agent should judge himself and others
     */
    private final ObjectiveFunction<Fisher>  objective;



    private final AdaptationProbability probability;




    /**
     * holds the starting point of a randomization
     */
    private Pair<T,Double> explorationStart;

    private ImitationStart<T> imitationStart;



    /**
     * checks that the T generated by exploration is valid
     */
    private final Predicate<T> explorationCheck ;


    /**
     * the last action taken was this kind of action
     *
     */
    private ExploreImitateStatus status;

    @Override
    public T concreteAdaptation(Fisher toAdapt, FishState state, MersenneTwisterFast random) {

        //check your fitness and where you are
        double fitness = objective.computeCurrentFitness(toAdapt);
        T current =  getSensor().scan(toAdapt);

        //if you explored in the previous step
        if(explorationStart != null)
        {
            assert imitationStart == null;
            assert status == ExploreImitateStatus.EXPLORING;


            Double previousFitness = explorationStart.getSecond();
            T previous = explorationStart.getFirst();
            T decision = this.algorithm.judgeRandomization(random, toAdapt,
                                                           previousFitness,
                                                           fitness,
                                                           previous,
                                                           current);

            this.probability.judgeExploration(previousFitness, fitness);

            explorationStart = null;


            if(decision!=null) {
                if(decision == previous )
                    fitness = previousFitness;
                current = decision;
            }
            assert current != null;
        }

        //if you imitated in the previous step
        else if(imitationStart != null)
        {
            assert explorationStart == null;
            assert status == ExploreImitateStatus.IMITATING;
            double previousFitness = imitationStart.getPreviousFitness();
            T previous = imitationStart.getPreviousDecision();
            T decision = this.algorithm.judgeImitation(random, toAdapt,
                                                       imitationStart.getFriend(),
                                                       previousFitness,
                                                       fitness,
                                                       previous,
                                                       current);

            imitationStart = null;
            if(decision!=null) {
                if(decision == previous )
                    fitness = previousFitness;
                current = decision;

            }

        }

        //you have no previous decisions to judge or you had but decided not to act upon them
        //you are now ready to check whether to explore or exploit

        double explorationProbability = probability.getExplorationProbability();
        //explore?
        if(explorationProbability>0 && random.nextBoolean(explorationProbability)) {

            // reject failed attempts up to a limit of 20
            int attempts = 0;

            while(attempts < 20) {
                T future = algorithm.randomize(random, toAdapt, fitness, current);
                if(explorationCheck.test(future)) {
                    explorationStart = new Pair<>(current, fitness);
                    status = ExploreImitateStatus.EXPLORING;
                    return future;
                }
                attempts++;
            }
            //failed: nowhere valid to explore!
            status = ExploreImitateStatus.EXPLOITING;
            return null;

        }

        assert  explorationStart==null;

        //imitate?
        double imitationProbability = probability.getImitationProbability();

        //get your friends (but not those that have been banned from fishing)
        //todo might want to make this as a funtion of time since last out rather than allowed at sea
        Collection<Fisher> friends = friendsExtractor.apply(new Pair<>(toAdapt, random));
        if(friends!=null) {
            List<Fisher> list = new ArrayList<>();
            for (Fisher friend : friends) {
                if (friend.isAllowedAtSea()) {
                    list.add(friend);
                }
            }
            friends = list;
        }

        if(imitationProbability>0 && friends!=null &&
                !friends.isEmpty() && random.nextBoolean(imitationProbability))
        {


            Pair<T, Fisher> imitation = algorithm.imitate(random,
                                                          toAdapt, fitness, current,
                                                          friends, objective, getSensor());
            //if there is somebody to imitate and the imitation does not involve just doing what I am doing anyway
            if(imitation.getSecond() != null && !imitation.getFirst().equals(current) && explorationCheck.test(imitation.getFirst()))
            {
                imitationStart = new ImitationStart<>(imitation.getSecond(),fitness,imitation.getFirst());

                status = ExploreImitateStatus.IMITATING;
                return imitation.getFirst();
            }

        }

        //exploit
        status=ExploreImitateStatus.EXPLOITING;
        return current;
    }

    public ExploreImitateAdaptation(
            Predicate<Fisher> validator,
            AdaptationAlgorithm<T> decision,
            Actuator<Fisher, T> actuator,
            Sensor<Fisher, T> sensor,
            ObjectiveFunction<Fisher> objective, double explorationProbability,
            double imitationProbability, final Predicate<T> explorationValidator) {

        this(validator,
             decision, actuator, sensor, objective,
             new FixedProbability(explorationProbability,imitationProbability),
             explorationValidator,
             new Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>>() {
                 @Override
                 public Collection<Fisher> apply(
                         Pair<Fisher, MersenneTwisterFast> input) {
                     return input.getFirst().getDirectedFriends();
                 }
             }
        );

    }

    public ExploreImitateAdaptation(
            Predicate<Fisher> validator,
            AdaptationAlgorithm<T> decision,
            Actuator<Fisher, T> actuator,
            Sensor<Fisher, T> sensor,
            ObjectiveFunction<Fisher> objective,
            AdaptationProbability probability, final Predicate<T> explorationValidator) {
        this(validator, decision, actuator, sensor, objective, probability,
             explorationValidator,
             new Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>>() {
                 @Override
                 public Collection<Fisher> apply(
                         Pair<Fisher, MersenneTwisterFast> input) {
                     return input.getFirst().getDirectedFriends();
                 }
             });

    }

    public ExploreImitateAdaptation(
            Predicate<Fisher> validator,
            AdaptationAlgorithm<T> decision,
            Actuator<Fisher, T> actuator, Sensor<Fisher, T> sensor,
            ObjectiveFunction<Fisher> objective,
            AdaptationProbability probability,
            Predicate<T> explorationValidator,
            Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>> friendsExtractor) {
        super(sensor, actuator, validator);
        this.friendsExtractor = friendsExtractor;
        this.algorithm = decision;
        this.objective = objective;
        this.probability = probability;
        this.explorationCheck = explorationValidator;
    }

    public void onStart(FishState state, Fisher toAdapt){
        algorithm.start(state, toAdapt,
                        getSensor().scan(toAdapt) );
        probability.start(state,toAdapt);
    }



    @Override
    public void turnOff(Fisher fisher) {

        probability.turnOff(fisher);
    }


    public Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>> getFriendsExtractor() {
        return friendsExtractor;
    }

    public void setFriendsExtractor(
            Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>> friendsExtractor) {
        this.friendsExtractor = friendsExtractor;
    }

    public AdaptationAlgorithm<T> getAlgorithm() {
        return algorithm;
    }


    public ObjectiveFunction<Fisher> getObjective() {
        return objective;
    }



    public Pair<T, Double> getExplorationStart() {
        return explorationStart;
    }

    public void setExplorationStart(Pair<T, Double> explorationStart) {
        this.explorationStart = explorationStart;
    }


    private class ImitationStart<K>
    {
        private final Fisher friend;
        private final double previousFitness;
        private final K previousDecision;

        public ImitationStart(Fisher friend, double previousFitness, K previousDecision) {
            this.friend = friend;
            this.previousFitness = previousFitness;
            this.previousDecision = previousDecision;
        }

        public Fisher getFriend() {
            return friend;
        }

        public double getPreviousFitness() {
            return previousFitness;
        }

        public K getPreviousDecision() {
            return previousDecision;
        }
    }

    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public ExploreImitateStatus getStatus() {
        return status;
    }
}
