package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.ParticleComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.TlsMetadata;
import com.talosvfx.talos.editor.project.TalosProject;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;

public class TlsImporter extends AbstractImporter<ParticleEffectDescriptor> {

    public void exportTlsFile(FileHandle tlsHandle) {
        TalosMain.Instance().errorReporting.enabled = false;
        FileHandle exportLocation = AssetImporter.makeSimilar(tlsHandle, "p");
        TalosProject talosProject = new TalosProject();
        talosProject.loadProject(tlsHandle, tlsHandle.readString(), true);
        talosProject.exportProject(exportLocation);
        TalosMain.Instance().errorReporting.enabled = true;
    }

    @Override
    public void makeInstance (GameAsset<ParticleEffectDescriptor> asset, GameObject parent) {


        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        Vector2 sceneCords = workspace.getMouseCordsOnScene();
        GameObject gameObject = workspace.createParticle(asset, sceneCords, parent);

        ParticleComponent component = new ParticleComponent();
        component.setGameAsset(asset);
        gameObject.addComponent(component);
    }


}
