package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TlsImporter extends AbstractImporter<ParticleEffectDescriptor> {

    private static final Logger logger = LoggerFactory.getLogger(TlsImporter.class);

    public void exportTlsFile(FileHandle tlsHandle) {
        TalosMain.Instance().errorReporting.enabled = false;
        FileHandle exportLocation = AssetImporter.makeSimilar(tlsHandle, "p");
//        TalosProject talosProject = new TalosProject();
//        talosProject.loadProject(tlsHandle, tlsHandle.readString(), true);
//        talosProject.exportProject(exportLocation);
        TalosMain.Instance().errorReporting.enabled = true;
    }

    @Override
    public GameObject makeInstance (GameAsset<ParticleEffectDescriptor> asset, GameObject parent) {

        logger.info("Needs reimplementing for context");

//        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
//        Vector2 sceneCords = workspace.getMouseCordsOnScene();
//        GameObject gameObject = workspace.createParticle(asset, sceneCords, parent);
//
//        ParticleComponent component = new ParticleComponent();
//        component.setGameAsset(asset);
//        gameObject.addComponent(component);
//        return gameObject;
        return null;
    }


}
