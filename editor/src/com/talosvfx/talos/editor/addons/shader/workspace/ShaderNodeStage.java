package com.talosvfx.talos.editor.addons.shader.workspace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rockbite.bongo.engine.render.PolygonSpriteBatchMultiTextureMULTIBIND;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.runtime.shaders.ShaderBuilder;
import com.talosvfx.talos.editor.addons.shader.nodes.ColorOutput;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeRemovedEvent;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

public class ShaderNodeStage extends DynamicNodeStage implements Observer {

    private ColorOutput colorOutput;

    FrameBuffer frameBuffer;
    PolygonBatch spriteBatch;
    Viewport viewport;

    class ExportSequencePayload {
        public int width;
        public int height;
        public float duration;
        public int fps;
        public float timer = 0;
        public float totalTimer = 0;
        public Array<Pixmap> frames = new Array<>();
        public String name;
        public String path;
    }

    ExportSequencePayload exportSequencePayload = null;

    public ShaderNodeStage (Skin skin) {
        super(skin);

        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 240, 240, false);
        spriteBatch = new PolygonSpriteBatchMultiTextureMULTIBIND();
        viewport = new FitViewport(240, 240);
        viewport.apply(true);

        Notifications.registerObserver(this);
    }

    @Override
    protected XmlReader.Element loadData () {
        FileHandle list = Gdx.files.internal("addons/shader/nodes.xml");
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(list);

        return root;
    }

    @Override
    public NodeWidget createNode (String nodeName, float screenX, float y) {

        if(!nodeName.equals("ColorOutput")) {
            return super.createNode(nodeName, screenX, y);
        } else {
            if(colorOutput == null) {
                NodeWidget node = super.createNode(nodeName, screenX, y);
                colorOutput = (ColorOutput) node;
                return node;
            }
        }

        return null;
    }

    @Override
    public void reset () {
        super.reset();
        colorOutput = null;
    }

    @EventHandler
    public void onNodeRemoved(NodeRemovedEvent event) {
       if (event.getNode() == colorOutput) {
           colorOutput = null;
       }
    }

    public String getFragShader() {
        ShaderBuilder builder = new ShaderBuilder();

        if(colorOutput == null) return "";

        colorOutput.buildFragmentShader(builder);

        return builder.getFragmentString();
    }

    public String getShaderData() {
        ShaderBuilder builder = new ShaderBuilder();

        if(colorOutput == null) return "";

        colorOutput.buildFragmentShader(builder);

        String methods = builder.generateMethods();
        String main = builder.getMainContent();

        StringWriter writer = new StringWriter();
        XmlWriter xml = new XmlWriter(writer);

        try {
            XmlWriter shader = xml.element("shader");
            XmlWriter uniforms = shader.element("uniforms");

            ObjectMap<String, ShaderBuilder.UniformData> declaredUniforms = builder.getDeclaredUniforms();
            for(String uniformName: declaredUniforms.keys()) {
                XmlWriter uniform = uniforms.element("uniform");

                uniform.attribute("name", uniformName);
                uniform.attribute("type", declaredUniforms.get(uniformName).type.getTypeString());
                ShaderBuilder.UniformData uniformData = declaredUniforms.get(uniformName);

                if(uniformData.type == ShaderBuilder.Type.TEXTURE) {
                    uniform.text(uniformData.payload.getValueDescriptor());
                }

                uniform.pop();
            }
            uniforms.pop();
            XmlWriter methodsElem = shader.element("methods");
            methodsElem.text("<![CDATA[" + methods + "]]>");
            methodsElem.pop();
            XmlWriter mainElem = shader.element("main");
            mainElem.text("<![CDATA[" + main + "]]>");
            mainElem.pop();
            shader.pop();

            return writer.toString();
        } catch (IOException e) {
            e.printStackTrace();

            return "";
        }
    }

    public Pixmap exportPixmap() {
        if (colorOutput == null) return null;

        frameBuffer.begin();
        spriteBatch.begin();

        Vector2 tmp = new Vector2();
        Vector2 targetSize = new Vector2(64, 64);

//        for(NodeWidget nodeWidget : nodeBoard.nodes) {
//            if(nodeWidget instanceof SampleTextureNode) {
//                SampleTextureNode node = (SampleTextureNode) nodeWidget;
//                Texture texture = node.getValue();
//
//                if(texture.getWidth() > targetSize.x) {
//                    targetSize.x = texture.getWidth();
//                }
//
//                if(texture.getHeight() > targetSize.y) {
//                    targetSize.y = texture.getHeight();
//                }
//            }
//
//        }

        viewport.update((int)targetSize.x, (int)targetSize.y);
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        tmp.set(colorOutput.getShaderBox().getX(), colorOutput.getShaderBox().getY());
        colorOutput.getShaderBox().setPosition(0, 0);
        colorOutput.getShaderBox().draw(spriteBatch, 1f);
        colorOutput.getShaderBox().setPosition(tmp.x, tmp.y);

        spriteBatch.end();

        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, (int)targetSize.x, (int)targetSize.y);

        frameBuffer.end();

        return pixmap;
    }

    public void exportSequence(String name, String path, int width, int height, float duration, int fps) {
        exportSequencePayload = new ExportSequencePayload();
        exportSequencePayload.width = width;
        exportSequencePayload.height = height;
        exportSequencePayload.duration = duration;
        exportSequencePayload.fps = fps;
        exportSequencePayload.timer = 0;
        exportSequencePayload.totalTimer = 0;
        exportSequencePayload.frames.clear();
        exportSequencePayload.name = name;
        exportSequencePayload.path = path;
    }

    @Override
    public void act() {
        super.act();

        if(exportSequencePayload != null) {
            float delta = Gdx.graphics.getDeltaTime();
            exportSequencePayload.timer += delta;
            exportSequencePayload.totalTimer += delta;

            if(exportSequencePayload.totalTimer >= exportSequencePayload.duration) {
                // then we are finished
                finishSequenceExport();
                return;
            }

            float frameLength = exportSequencePayload.duration / exportSequencePayload.fps;

            if(exportSequencePayload.timer > frameLength) {
                exportSequencePayload.timer = frameLength - exportSequencePayload.timer;
                // time to snap a frame
                snapFrameToSequence();
            }
        }
    }

    private void snapFrameToSequence() {
        if (colorOutput == null) return;

        frameBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.begin();

        Vector2 tmp = new Vector2();
        Vector2 targetSize = new Vector2(exportSequencePayload.width, exportSequencePayload.height);


        viewport.update((int)targetSize.x, (int)targetSize.y);
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        tmp.set(colorOutput.getShaderBox().getX(), colorOutput.getShaderBox().getY());
        colorOutput.getShaderBox().setPosition(0, 0);
        colorOutput.getShaderBox().draw(spriteBatch, 1f);
        colorOutput.getShaderBox().setPosition(tmp.x, tmp.y);

        spriteBatch.end();

        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, (int)targetSize.x, (int)targetSize.y);

        frameBuffer.end();

        pixmap = flipPixmap(pixmap);

        exportSequencePayload.frames.add(pixmap);
    }

    private Pixmap flipPixmap(Pixmap src) {
        final int width = src.getWidth();
        final int height = src.getHeight();
        Pixmap flipped = new Pixmap(width, height, src.getFormat());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                flipped.drawPixel(x, y, src.getPixel(x, height - y - 1));
            }
        }
        src.dispose();

        return flipped;
    }

    private void finishSequenceExport() {
        int frameIndex = 9;
        for(Pixmap pixmap : exportSequencePayload.frames) {
            frameIndex++;
            if(pixmap != null) {
                FileHandle file = Gdx.files.absolute(exportSequencePayload.path + File.separator + exportSequencePayload.name + frameIndex + ".png");
//                PixmapIO.writePNG(file, pixmap);
                pixmap.dispose();
            }
        }


        exportSequencePayload = null;
    }
}
