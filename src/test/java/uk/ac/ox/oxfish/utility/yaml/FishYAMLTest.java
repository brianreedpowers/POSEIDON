package uk.ac.ox.oxfish.utility.yaml;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import static org.junit.Assert.*;


public class FishYAMLTest {


    @Test
    public void canReadAScenario() throws Exception
    {

        String scenarioFile = "!!uk.ac.ox.oxfish.model.scenario.PrototypeScenario\n" +
                //here i am just using the constructor name as in the CONSTRUCTOR_MAP and then a simple map
                "biologyInitializer:\n" +
                "  Diffusing Logistic:\n"+
                "    carryingCapacity: 14.0\n" +
                "    differentialPercentageToMove: 5.0E-4\n" +
                "    maxSteepness: 0.8\n" +
                "    minSteepness: 0.7\n" +
                "    percentageLimitOnDailyMovement: 0.01\n" +
                "coastalRoughness: 4\n" +
                "departingStrategy:\n" +
                "  Fixed Probability:\n" +
                "    probabilityToLeavePort: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "      fixedValue: 1.0\n" +
                "depthSmoothing: 1000000\n" +
                "destinationStrategy: !!uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripIterativeDestinationFactory\n" +
                "  stepSize: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "    fixedValue: 5.0\n" +
                "  tripsPerDecision: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "    fixedValue: 1.0\n" +
                "fishers: 1234\n" +
                "fishingEfficiency: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "  fixedValue: 0.01\n" +
                "fishingStrategy: !!uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory\n" +
                "  daysAtSea: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "    fixedValue: 10.0\n" +
                "gridSizeInKm: 10.0\n" +
                "height: 50\n" +
                "holdSize: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "  fixedValue: 100.0\n" +
                "networkBuilder: !!uk.ac.ox.oxfish.model.network.EquidegreeBuilder\n" +
                "  degree: 2\n" +
                "ports: 1\n" +
                //here i am calling the regulation object by !!
                "regulation:\n" +
                "  Anarchy\n" +
                "speedInKmh: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "  fixedValue: 5.0\n" +
                "width: 50";

        FishYAML yaml = new FishYAML();
        final Object loaded = yaml.load(scenarioFile);
        //read prototype scenario correctly
        Assert.assertTrue(loaded instanceof PrototypeScenario);
        PrototypeScenario scenario = (PrototypeScenario) loaded;
        //read initializer correctly
        Assert.assertTrue(scenario.getBiologyInitializer() instanceof DiffusingLogisticFactory);
        DiffusingLogisticFactory factory = (DiffusingLogisticFactory) scenario.getBiologyInitializer();
        //reads double parameters correctly
        Assert.assertTrue(factory.getCarryingCapacity() instanceof FixedDoubleParameter);
        Assert.assertEquals(((FixedDoubleParameter) factory.getCarryingCapacity()).getFixedValue(),14.0,.001);
        //reads normal doubles correctly
        Assert.assertEquals(((FixedDoubleParameter) factory.getMinSteepness()).getFixedValue(), .7, .0001);
        //reads anarchy factory just as well (it's a scalar algorithmFactory which is tricky)
        Assert.assertTrue(scenario.getRegulation() instanceof AnarchyFactory);

    }


    @Test
    public void writePrettily() throws Exception {

        DiffusingLogisticFactory factory = new DiffusingLogisticFactory();
        factory.setCarryingCapacity(new NormalDoubleParameter(10000, 10));
        factory.setMaxSteepness(new UniformDoubleParameter(0, 10));
        factory.setDifferentialPercentageToMove(new FixedDoubleParameter(.001));
        FishYAML yaml = new FishYAML();
        final String dumped = yaml.dump(factory);
        System.out.println(dumped);

    }
}