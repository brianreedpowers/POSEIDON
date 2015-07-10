package uk.ac.ox.oxfish.utility.yaml;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Constructor useful to implement YAML objects back into the Fishstate. I modify it so that it does the following things:
 * <ul>
 *     <li> FixedDoubleParameters can be input as numbers and it is still valid</li>
 *     <li> Algorithm Factories can be input as map and it's still valid</li>
 * </ul>
 * Created by carrknight on 7/10/15.
 */
public class YamlConstructor extends  Constructor {


    public YamlConstructor()
    {

        //intercept the scalar nodes to see if they are actually Factories or DoubleParameters
        this.yamlClassConstructors.put(NodeId.scalar, new Constructor.ConstructScalar(){
            @Override
            public Object construct(Node nnode) {
                //if the field you are trying to fill is a double parameter
                if(nnode.getType().equals(DoubleParameter.class))
                    //then a simple scalar must be a fixed double parameter. Build it
                    return new FixedDoubleParameter((Double.parseDouble((String) constructScalar((ScalarNode) nnode))));
                else
                    //it's also possible that the scalar is an algorithm factory without any settable field
                    //this is rare which means that factories are represented as maps, but this might be one of the simple
                    //ones like AnarchyFactory
                    if(AlgorithmFactory.class.isAssignableFrom(nnode.getType()))
                        return AlgorithmFactories.constructorLookup((String) constructScalar((ScalarNode) nnode));
                    //otherwise I guess it's really a normal scalar!
                    else
                        return super.construct(nnode);                }
        });

        //intercept maps as well, some of them could be factories
        this.yamlClassConstructors.put(NodeId.mapping, new Constructor.ConstructMapping(){

            @Override
            public Object construct(Node node) {
                if(AlgorithmFactory.class.isAssignableFrom(node.getType())) {
                    //try super constructor first, most of the time it works
                    try {
                        return super.construct(node);
                    } catch (YAMLException e) {
                        //the original construct failed, hopefully this means it's an algorithm factory
                        //written as a map, so get its name and look it up
                        final AlgorithmFactory toReturn = AlgorithmFactories.constructorLookup(
                                ((ScalarNode) ((MappingNode) node).getValue().get(0).getKeyNode()).getValue());
                        //now take all the elements of the submap, we are going to place them by setter
                        //todo might have to flatten here!
                        ((MappingNode) node).setValue(
                                ((MappingNode)((MappingNode) node).getValue().get(0).getValueNode()).getValue());
                        assert toReturn != null;
                        //need to set the node to the correct return or the reflection magic of snakeYAML wouldn't work
                        node.setType(toReturn.getClass());
                        //use beans to set all the properties correctly
                        constructJavaBean2ndStep((MappingNode) node, toReturn);
                        //done!
                        return toReturn;
                    }
                }
                else
                    return super.construct(node);
            }
        });
    }






}
