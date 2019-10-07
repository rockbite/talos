package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StreamUtils;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.runtime.values.ColorPoint;
import com.rockbite.tools.talos.editor.wrappers.*;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.modules.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LegacyImporter {

    private NodeStage stage;

    private Array<String> scaleTimes = new Array<>();

    private float nextY;
    float leftX;
    float rightX;
    float yStart;
    int iter;

    private String path;

    private boolean globalContinuous = false;

    public LegacyImporter(NodeStage stage) {
        this.stage = stage;

        scaleTimes.addAll("delay", "duration", "life", "lifeOffset");
    }

    public void read(FileHandle effectFile) {
        globalContinuous = false;
        leftX = 200;
        rightX =500;
        yStart = 400;
        iter = 0;
        nextY = 700;

        InputStream input = effectFile.read();

        path = effectFile.parent().path();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(input), 512);
            while (true) {
                processEmitter(reader);
                if (reader.readLine() == null) break;
            }
        } catch (IOException ex) {
            //throw new GdxRuntimeException("Error loading effect: " + effectFile, ex);
        } finally {
            StreamUtils.closeQuietly(reader);
        }
    }

    String readString (String line) throws IOException {
        return line.substring(line.indexOf(":") + 1).trim();
    }

    String readString (BufferedReader reader, String name) throws IOException {
        String line = reader.readLine();
        if (line == null) throw new IOException("Missing value: " + name);
        return readString(line);
    }

    boolean readBoolean (String line) throws IOException {
        return Boolean.parseBoolean(readString(line));
    }

    boolean readBoolean (BufferedReader reader, String name) throws IOException {
        return Boolean.parseBoolean(readString(reader, name));
    }

    int readInt (BufferedReader reader, String name) throws IOException {
        return Integer.parseInt(readString(reader, name));
    }

    float readFloat (BufferedReader reader, String name) throws IOException {
        return Float.parseFloat(readString(reader, name));
    }

    private Array<String> activeArr = new Array<>();


    ModuleWrapper readScaledNumbericalValue(BufferedReader reader, ModuleWrapper toModule, int toSlot, String varName, boolean skip, boolean independant) {
        ModuleWrapper wrapper = null;
        DynamicRangeModuleWrapper dynamic;
        RandomRangeModuleWrapper randomRange;
        try {
            if(isActive(reader, varName)) {
                float lowMin = readFloat(reader, "lowMin");
                float lowMax = readFloat(reader, "lowMax");
                float highMin = readFloat(reader, "highMin");
                float highMax = readFloat(reader, "highMax");

                if(scaleTimes.contains(varName, false)) {
                    lowMin /= 1000f;
                    lowMax /= 1000f;
                    highMin /= 1000f;
                    highMax /= 1000f;
                }

                if(varName.equals("emission")) {
                    //1 value hack
                    if(lowMin == 1) lowMin = 2;
                    if(lowMax == 1) lowMax = 2;
                    if(highMin == 1) highMin = 2;
                    if(highMax == 1) highMax = 2;
                }

                boolean relative = readBoolean(reader, "relative");
                float[] scaling = new float[readInt(reader, "scalingCount")];
                for (int i = 0; i < scaling.length; i++) {
                    scaling[i] = readFloat(reader, "scaling" + i);
                }
                float[] timeline = new float[readInt(reader, "timelineCount")];
                for (int i = 0; i < timeline.length; i++) {
                    timeline[i] = readFloat(reader, "timeline" + i);
                }

                Array<Vector2> points = new Array<>();
                for(int i = 0; i < scaling.length; i++) {
                    points.add(new Vector2(timeline[i], scaling[i]));
                }

                if(!skip) {
                    if(varName.equals("xOffset") || varName.equals("yOffset")) { // special shitty case
                        randomRange = (RandomRangeModuleWrapper) stage.moduleBoardWidget.createModule(RandomRangeModule.class, leftX, getNextY());
                        randomRange.setData(lowMin, lowMax);
                        if(toModule != null) {
                            stage.moduleBoardWidget.makeConnection(randomRange, toModule, RandomRangeModule.OUTPUT, toSlot);
                        }
                        wrapper = randomRange;
                    } else {
                        dynamic = (DynamicRangeModuleWrapper) stage.moduleBoardWidget.createModule(DynamicRangeModule.class, leftX, getNextY());
                        dynamic.setData(lowMin, lowMax, highMin, highMax, points);
                        if(toModule != null) {
                            stage.moduleBoardWidget.makeConnection(dynamic, toModule, DynamicRangeModule.OUTPUT, toSlot);
                        }
                        wrapper = dynamic;
                    }
                }
            }

            if(independant) {
                String line = reader.readLine();
                if (line != null && line.contains("independent")) {
                    boolean independent = Boolean.parseBoolean(readString(line));
                } else {
                    reader.reset();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wrapper;
    }

    void readGradientValue(BufferedReader reader, ModuleWrapper toModule, int toSlot, String varName) {
        try {
            if(isActive(reader, varName)) {
                float[] colors = new float[readInt(reader, "colorsCount")];
                for (int i = 0; i < colors.length; i++)
                    colors[i] = readFloat(reader, "colors" + i);
                float[] timeline = new float[readInt(reader, "timelineCount")];
                for (int i = 0; i < timeline.length; i++)
                    timeline[i] = readFloat(reader, "timeline" + i);


                // now normal code...
                Array<ColorPoint> points = new Array<>();
                for(int i = 0; i < timeline.length; i++) {
                    ColorPoint point = new ColorPoint();
                    point.pos = timeline[i];
                    point.color.set(colors[i*3], colors[i*3+1], colors[i*3+2], 1f);
                    points.add(point);
                }

                GradientColorModuleWrapper wrapper = (GradientColorModuleWrapper) stage.moduleBoardWidget.createModule(GradientColorModule.class, leftX, getNextY());
                wrapper.setData(points);
                stage.moduleBoardWidget.makeConnection(wrapper, toModule, RandomRangeModule.OUTPUT, toSlot);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void readRangedNumericValue(BufferedReader reader, ModuleWrapper toModule, int toSlot, String varName, String key1, String key2, boolean skip) {
        try {
            if(isActive(reader, varName)) {
                float min = readFloat(reader, key1);
                float max = readFloat(reader, key2);
                if(scaleTimes.contains(varName, false)) {
                    min /= 1000f;
                    max /= 1000f;
                }
                if(!skip) {
                    RandomRangeModuleWrapper wrapper = (RandomRangeModuleWrapper) stage.moduleBoardWidget.createModule(RandomRangeModule.class, leftX, getNextY());
                    wrapper.setData(min, max);
                    stage.moduleBoardWidget.makeConnection(wrapper, toModule, RandomRangeModule.OUTPUT, toSlot);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readRangedNumericValue(BufferedReader reader, ModuleWrapper toModule, int toSlot, String varName, boolean skip) {
        readRangedNumericValue(reader, toModule, toSlot, varName, "lowMin", "lowMax", skip);
    }

    void readRangedNumericValue(BufferedReader reader, ModuleWrapper toModule, int toSlot, String varName) {
        readRangedNumericValue(reader, toModule, toSlot, varName, "lowMin", "lowMax", false);
    }

    void readSpawnShapeValue(BufferedReader reader, String varName, RandomRangeModuleWrapper xOffsetWrapper, RandomRangeModuleWrapper yOffsetWrapper, ParticleModuleWrapper particleModuleWrapper, InputModuleWrapper durationWrapper, Vector2ModuleWrapper vector2ModuleWrapper) {
        boolean edges = false;
        String side = "both";
        String shape = "point";
        OffsetModuleWrapper offsetModuleWrapper = null;

        Vector2 minBound = new Vector2();
        Vector2 maxBound = new Vector2();
        try {
            if(true) {
                shape = readString(reader, "shape");
                if(shape.equals("point")) {
                    // keep the old vector stuff, but in other cases we invoke the offset module
                } else {
                    float offsetXMin = 0;
                    float offsetXMax = 0;
                    float offsetYMin = 0;
                    float offsetYMax = 0;
                    if(xOffsetWrapper != null) {
                        offsetXMin = xOffsetWrapper.getModule().getMin();
                        offsetXMax = xOffsetWrapper.getModule().getMax();

                        stage.moduleBoardWidget.deleteWrapper(xOffsetWrapper);

                    }
                    if(yOffsetWrapper != null) {
                        offsetYMin = yOffsetWrapper.getModule().getMin();
                        offsetYMax = yOffsetWrapper.getModule().getMax();

                        stage.moduleBoardWidget.deleteWrapper(yOffsetWrapper);
                    }

                    if(offsetXMin < minBound.x) minBound.x = offsetXMin;
                    if(offsetYMin < minBound.y) minBound.y = offsetYMin;
                    if(offsetXMax >  maxBound.x) maxBound.x = offsetXMax;
                    if(offsetYMax > maxBound.y) maxBound.y = offsetYMax;

                    // remove this both wrappers here, and move on to shape
                    stage.moduleBoardWidget.deleteWrapper(vector2ModuleWrapper);

                    // shape stuff
                    offsetModuleWrapper = (OffsetModuleWrapper) stage.moduleBoardWidget.createModule(OffsetModule.class, leftX, getNextY());
                    offsetModuleWrapper.getModule().setLowPos(new Vector2(offsetXMin, offsetYMin));
                    offsetModuleWrapper.getModule().setHighPos(new Vector2(offsetXMax, offsetYMax));

                    if(shape.equals("ellipse")) {
                        edges = readBoolean(reader, "edges");
                        side = readString(reader, "side");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            reader.readLine();
            DynamicRangeModuleWrapper spawnWidthWrapper = (DynamicRangeModuleWrapper) readScaledNumbericalValue(reader, null, 0,"spawnWidth", false, false);
            reader.readLine();
            DynamicRangeModuleWrapper spawnHeightWrapper = (DynamicRangeModuleWrapper) readScaledNumbericalValue(reader, null, 0,"spawnHeight", false, false);

            // now time to get the values and ditch this modules
            float widthLowMin = spawnWidthWrapper.getModule().getLowMin();
            float widthLowMax = spawnWidthWrapper.getModule().getLowMax();
            float widthHighMin = spawnWidthWrapper.getModule().getHightMin();
            float widthHighMax = spawnWidthWrapper.getModule().getHightMax();

            float heightLowMin = spawnHeightWrapper.getModule().getLowMin();
            float heightLowMax = spawnHeightWrapper.getModule().getLowMax();
            float heightHighMin = spawnHeightWrapper.getModule().getHightMin();
            float heightHighMax = spawnHeightWrapper.getModule().getHightMax();

            float lowScl = Math.max((maxBound.x - minBound.x) + widthLowMax, (maxBound.y - minBound.y) + heightLowMax) * 2f;
            float highScl = Math.max((maxBound.x - minBound.x) + widthHighMax, (maxBound.y - minBound.y) + heightHighMax) * 2f;

            Array<Vector2> points = spawnWidthWrapper.getPoints();

            // ditch this modules.
            stage.moduleBoardWidget.deleteWrapper(spawnWidthWrapper);
            stage.moduleBoardWidget.deleteWrapper(spawnHeightWrapper);

            // use values
            if(offsetModuleWrapper != null) {

                offsetModuleWrapper.setScaleValues(lowScl, highScl);

                offsetModuleWrapper.setPoints(points);

                offsetModuleWrapper.getModule().setLowSize(new Vector2((widthLowMin + widthLowMax)/2f, (heightLowMin + heightLowMax)/2f)); // best we can do
                offsetModuleWrapper.getModule().setHighSize(new Vector2((widthHighMin + widthHighMax)/2f, (heightHighMin + heightHighMax)/2f)); // best we can do

                offsetModuleWrapper.getModule().setLowEdge(edges);
                offsetModuleWrapper.getModule().setHighEdge(edges);
                if(side.equals("both")) {
                    offsetModuleWrapper.getModule().setLowSide(OffsetModule.SIDE_ALL);
                    offsetModuleWrapper.getModule().setHighSide(OffsetModule.SIDE_ALL);
                }
                if(side.equals("top")) {
                    offsetModuleWrapper.getModule().setLowSide(OffsetModule.SIDE_TOP);
                    offsetModuleWrapper.getModule().setHighSide(OffsetModule.SIDE_TOP);
                }
                if(side.equals("bottom")) {
                    offsetModuleWrapper.getModule().setLowSide(OffsetModule.SIDE_BOTTOM);
                    offsetModuleWrapper.getModule().setHighSide(OffsetModule.SIDE_BOTTOM);
                }

                if(shape.equals("square")) {
                    offsetModuleWrapper.getModule().setLowShape(OffsetModule.TYPE_SQUARE);
                    offsetModuleWrapper.getModule().setHighShape(OffsetModule.TYPE_SQUARE);
                }
                if(shape.equals("ellipse")) {
                    offsetModuleWrapper.getModule().setLowShape(OffsetModule.TYPE_ELLIPSE);
                    offsetModuleWrapper.getModule().setHighShape(OffsetModule.TYPE_ELLIPSE);
                }
                if(shape.equals("line")) {
                    offsetModuleWrapper.getModule().setLowShape(OffsetModule.TYPE_LINE);
                    offsetModuleWrapper.getModule().setHighShape(OffsetModule.TYPE_LINE);
                }


                // finally make the connections
                stage.moduleBoardWidget.makeConnection(offsetModuleWrapper, particleModuleWrapper, OffsetModule.OUTPUT, ParticleModule.OFFSET);

                //connect to duration alpha
                stage.moduleBoardWidget.makeConnection(durationWrapper, offsetModuleWrapper, InputModule.OUTPUT, OffsetModule.ALPHA);

                // update visual data
                offsetModuleWrapper.setEquals(false);
                offsetModuleWrapper.updateWidgetsFromModuleData();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isActive(BufferedReader reader, String varName) {
        activeArr.clear();

        activeArr.addAll("duration", "emission", "life", "xScaleValue", "transparency", "spawnShape", "spawnWidth", "spawnHeight", "count", "tint");

        if(!activeArr.contains(varName, false)) {
            try {
                return readBoolean(reader, "active");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return true;
        }

        return true;
    }

    private float getNextY() {
        iter++;
        nextY-= 200f;

        if(iter > 2) {
            iter = 0;
            leftX -= 400;
            nextY = 700;
        }

        return nextY;
    }

    private void processEmitter(BufferedReader reader) {
        leftX = 200;
        rightX =500;
        yStart = 400;
        iter = 0;

        try {
            String emitterName = readString(reader, "name");
            TalosMain.Instance().Project().createNewEmitter(emitterName);
            ParticleModuleWrapper particleModuleWrapper = (ParticleModuleWrapper) stage.moduleBoardWidget.createModule(ParticleModule.class, rightX, yStart);
            EmitterModuleWrapper emitterModuleWrapper = (EmitterModuleWrapper) stage.moduleBoardWidget.createModule(EmitterModule.class, rightX, yStart+200);

            //InputModuleWrapper lifeInput = (InputModuleWrapper) stage.moduleBoardWidget.createModule(InputModule.class, rightX-300, yStart+200);
            InputModuleWrapper durationInput = (InputModuleWrapper) stage.moduleBoardWidget.createModule(InputModule.class, rightX-300, yStart+200);

            Vector2ModuleWrapper offsetVector = (Vector2ModuleWrapper) stage.moduleBoardWidget.createModule(Vector2Module.class, leftX, getNextY());
            stage.moduleBoardWidget.makeConnection(offsetVector, particleModuleWrapper, Vector2Module.OUTPUT, ParticleModule.OFFSET);

            reader.readLine();
            readRangedNumericValue(reader, emitterModuleWrapper, EmitterModule.DELAY, "delay");
            reader.readLine();
            readRangedNumericValue(reader, emitterModuleWrapper, EmitterModule.DURATION,"duration");
            reader.readLine();
            readRangedNumericValue(reader, null, 0,"count", "min", "max", true); // we don't have this yet
            reader.readLine();
            ModuleWrapper emissionWrapper = readScaledNumbericalValue(reader, emitterModuleWrapper, EmitterModule.RATE,"emission", false, false);
            reader.readLine();
            ModuleWrapper lifeWrapper = readScaledNumbericalValue(reader, particleModuleWrapper, ParticleModule.LIFE,"life", false, true);
            reader.readLine();
            readScaledNumbericalValue(reader, null, 0,"lifeOffset", true, true);
            reader.readLine();
            RandomRangeModuleWrapper xOffsetWrapper = (RandomRangeModuleWrapper) readScaledNumbericalValue(reader, offsetVector, Vector2Module.X, "xOffset", false, false);
            reader.readLine();
            RandomRangeModuleWrapper yOffsetWrapper = (RandomRangeModuleWrapper) readScaledNumbericalValue(reader, offsetVector, Vector2Module.Y, "yOffset", false, false);
            reader.readLine();
            readSpawnShapeValue(reader, "spawnShape", xOffsetWrapper, yOffsetWrapper, particleModuleWrapper, durationInput, offsetVector);
            String line = reader.readLine();
            if (line.trim().equals("- Scale -")) {
                readScaledNumbericalValue(reader, particleModuleWrapper, ParticleModule.SIZE,"xScaleValue", false, false);
                reader.readLine();
            } else {
                ModuleWrapper xval = readScaledNumbericalValue(reader, null, Vector2Module.X,"xScaleValue", false, false);
                reader.readLine();
                ModuleWrapper yval = readScaledNumbericalValue(reader, null, Vector2Module.Y,"yScaleValue", false, false);

                if(yval == null) {
                    stage.moduleBoardWidget.makeConnection(xval, particleModuleWrapper, DynamicRangeModule.OUTPUT, ParticleModule.SIZE);
                } else {
                    Vector2ModuleWrapper vectorWrapper = (Vector2ModuleWrapper) stage.moduleBoardWidget.createModule(Vector2Module.class, leftX, getNextY());
                    stage.moduleBoardWidget.makeConnection(vectorWrapper, particleModuleWrapper, Vector2Module.OUTPUT, ParticleModule.SIZE);

                    stage.moduleBoardWidget.makeConnection(xval, vectorWrapper, DynamicRangeModule.OUTPUT, Vector2Module.X);
                    stage.moduleBoardWidget.makeConnection(yval, vectorWrapper, DynamicRangeModule.OUTPUT, Vector2Module.Y);
                }
            }
            reader.readLine();
            readScaledNumbericalValue(reader, particleModuleWrapper, ParticleModule.VELOCITY,"velocity", false, false);
            reader.readLine();
            ModuleWrapper angleWrapper = readScaledNumbericalValue(reader, particleModuleWrapper, ParticleModule.ANGLE,"angle", false, false);
            reader.readLine();
            readScaledNumbericalValue(reader, particleModuleWrapper, ParticleModule.ROTATION,"rotation", false, false);
            reader.readLine();
            readScaledNumbericalValue(reader, null, 0,"wind", true, false);
            reader.readLine();
            readScaledNumbericalValue(reader, particleModuleWrapper, ParticleModule.GRAVITY,"gravity", false, false);
            reader.readLine();
            readGradientValue(reader, particleModuleWrapper, ParticleModule.COLOR,"tint");
            reader.readLine();
            readScaledNumbericalValue(reader, particleModuleWrapper, ParticleModule.TRANSPARENCY,"transparency", false, false);
            reader.readLine();

            if(angleWrapper == null) {
                StaticValueModuleWrapper angleVal = (StaticValueModuleWrapper) stage.moduleBoardWidget.createModule(StaticValueModule.class, leftX, getNextY());
                angleVal.setValue(0);
                stage.moduleBoardWidget.makeConnection(angleVal, particleModuleWrapper, 0, ParticleModule.ANGLE);
            }

            boolean attached = readBoolean(reader, "attached");
            boolean continuous = readBoolean(reader, "continuous");
            boolean aligned = readBoolean(reader, "aligned");
            boolean additive = readBoolean(reader, "additive");
            boolean behind = readBoolean(reader, "behind");

            if(continuous) globalContinuous = true;

            if(globalContinuous) {
                continuous = true; // if at least one effect is continuous then all of them will be imported as true.
                // it's long to explain why, but it's because legacy editor and legacy runtime do ont agree on each other on this.
            }

            EmConfigModuleWrapper config = (EmConfigModuleWrapper) stage.moduleBoardWidget.createModule(EmConfigModule.class, leftX, getNextY());
            stage.moduleBoardWidget.makeConnection(config, emitterModuleWrapper, EmConfigModule.OUTPUT, EmitterModule.CONFIG);

            config.setAttached(attached);
            config.setContinuous(continuous);
            config.setAligned(aligned);
            config.setAdditive(additive);

            // Backwards compatibility
            line = reader.readLine();
            if (line.startsWith("premultipliedAlpha")) {
                boolean premultipliedAlpha = readBoolean(line);
                line = reader.readLine();
            }
            if (line.startsWith("spriteMode")) {
                String spriteMode = readString(line);
                line = reader.readLine();
            }

            Array<String> imagePaths = new Array<String>();
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                imagePaths.add(line);
            }

            if(imagePaths.size > 0) {
                // then it's just drawable
                TextureModuleWrapper textureModule = (TextureModuleWrapper) stage.moduleBoardWidget.createModule(TextureModule.class, leftX, getNextY());
                String filePath = imagePaths.get(0);
                if(filePath.contains("/")) {
                    if(!Gdx.files.absolute(filePath).exists()) {
                        filePath = path + "\\" + filePath.substring(filePath.lastIndexOf("/"));
                    }
                } else {
                    filePath = path + "\\" + filePath;
                }
                textureModule.setTexture(filePath);
                stage.moduleBoardWidget.makeConnection(textureModule, particleModuleWrapper, TextureModule.OUTPUT, ParticleModule.DRAWABLE);
            }


            //duration connects to  life and emission
            durationInput.setKey(ScopePayload.EMITTER_ALPHA);
            stage.moduleBoardWidget.makeConnection(durationInput, lifeWrapper, InputModule.OUTPUT, DynamicRangeModule.ALPHA);
            stage.moduleBoardWidget.makeConnection(durationInput, emissionWrapper, InputModule.OUTPUT, DynamicRangeModule.ALPHA);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
