package uk.ac.ox.oxfish.gui.drawing;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.metawidget.inspector.annotation.UiHidden;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.grid.FastObjectGridPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.gui.MetaInspector;
import uk.ac.ox.oxfish.gui.TriColorMap;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Basically a transformer that changes color mapping according to species.
 * Created by carrknight on 4/22/15.
 */
public class ColorfulGrid extends FastObjectGridPortrayal2D {



    private ObservableMap<String,ColorEncoding> encodings;

    private ColorEncoding selected;

    private String selectedName;

    /**
     * the default encoder just returns altitude
     */

    private final ColorMap depthColor = new TriColorMap(-6000, 0, 6000, Color.BLUE, Color.CYAN, Color.GREEN, Color.RED);


    private final Queue<Color> defaultFishColors = new LinkedList<>();
    /**
     * the specie currently selected, no selection means depth
     */


    /**
     * when drawing biomass use the transform of the current biomass rather than the biomass itself (to avoid large numbers dominating everything)
     */
    private final static Function<Double,Double> BIOMASS_TRANSFORM = aDouble -> Math.sqrt(aDouble);

    private final static double MAX_BIOMASS =  5000;

    @UiHidden
    private MersenneTwisterFast random;

    public ColorfulGrid(MersenneTwisterFast random)
    {
        encodings = FXCollections.observableHashMap();
        this.random = random;
        //add the default color map showing depth
        encodings.put("Depth", new ColorEncoding(
                new TriColorMap(-6000, 0, 6000, Color.BLUE, Color.CYAN, Color.GREEN, Color.RED),
                seaTile -> seaTile.isProtected() ? Double.NaN : seaTile.getAltitude(), true));

        //add the default color map showing rocky areas
        encodings.put("Habitat",new ColorEncoding(
                new TriColorMap(-1,0,1,Color.black,new Color(237, 201, 175), new Color(69, 67, 67)),
                seaTile -> seaTile.getAltitude() >=0 ? Double.NaN : seaTile.getRockyPercentage(),
                true));

        setSelectedEncoding("Depth");

        defaultFishColors.add(Color.RED);
        defaultFishColors.add(Color.BLUE);
        defaultFishColors.add(Color.BLUE);
        defaultFishColors.add(Color.ORANGE);
        defaultFishColors.add(Color.YELLOW);
    }


    /**
     * create a colormap for each specie
     * @param biology
     * @param seaTiles
     */
    public void initializeGrid(GlobalBiology biology,
                               List<SeaTile> seaTiles)
    {

        double max = BIOMASS_TRANSFORM.apply(MAX_BIOMASS);
        for(Species species : biology.getSpecies())
        {
            max = Math.max(max,
                           BIOMASS_TRANSFORM.apply(
                                   seaTiles.stream().mapToDouble(value -> value.getBiomass(species)).
                                           filter(
                                                   Double::isFinite).max().orElse(MAX_BIOMASS)));
        }

        for(Species species : biology.getSpecies())
        {



            Color color =  defaultFishColors.size() == 0 ? Color.RED : defaultFishColors.poll();
            encodings.put(species.getName(), new SelfAdjustingColorEncoding(
                    new SimpleColorMap(0, max, Color.WHITE, color),
                    seaTile ->
                            BIOMASS_TRANSFORM.apply(
                                    seaTile.getBiomass(species)),
                    false,
                    max,
                    0));
        }

    }


    /**
     * turn the seatile into a double that can be coded into color by the portrayal
     * @param tile the seatile
     * @return a double that should be drawn by the color map
     */
    public double encodeSeaTile(SeaTile tile)
    {

        return selected.getEncoding().apply(tile);
    }

    @Override
    public double doubleValue(Object obj) {
        return encodeSeaTile((SeaTile) obj);
    }

    /**
     * set the correct transform
     * @param encodingName the name of the correct encoding
     */
    public void setSelectedEncoding(String encodingName) {

        selectedName = encodingName;
        selected = encodings.get(encodingName);
        assert selected != null;
        this.setMap(selected.getMap());
        this.setImmutableField(selected.isImmutable());


    }


    public void addEnconding(String encodingName,ColorEncoding encoding)
    {

        Preconditions.checkArgument(!encodings.containsKey(encodingName), "Already present color encoding!");
        encodings.put(encodingName,encoding);

    }

    public void removeEncoding(String encodingName)
    {
        encodings.remove(encodingName);
    }



    @Override
    public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
        if(wrapper == null) {
            return null;
        } else {
            return new MetaInspector(wrapper.getObject(), ((FishGUI) state));
        }
    }


    /**
     * Getter for property 'encodings'.
     *
     * @return Value for property 'encodings'.
     */
    public ObservableMap<String, ColorEncoding> getEncodings() {
        return encodings;
    }

    public String getSelectedName() {
        return selectedName;
    }
}
