package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.assets.BaseAssetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SEAssetProvider extends BaseAssetProvider {

    private static final Logger logger = LoggerFactory.getLogger(SEAssetProvider.class);

    Sprite defaultSprite;

    ObjectMap<String, Sprite> sprites = new ObjectMap<>();

    public SEAssetProvider() {
        defaultSprite = new Sprite();
        defaultSprite.setRegion(SharedResources.skin.getRegion("white"));

        setAssetHandler(Sprite.class, new AssetHandler<Sprite>() {
            @Override
            public Sprite findAsset (String assetName) {
                if(!sprites.containsKey(assetName)) {
                    FileHandle file = searchForFile(assetName + ".png");
                    TextureRegion region = new TextureRegion(new Texture(file));
                    Sprite sprite = new Sprite(region);
                    sprites.put(assetName, sprite);
                }

                return sprites.get(assetName);
            }
        });
    }

    @Override
    public <T> T findAsset (String assetName, Class<T> clazz) {
        if(clazz.equals(Sprite.class) && assetName.equals("white")) {
            return (T) defaultSprite;
        }
        return super.findAsset(assetName, clazz);
    }

    private FileHandle searchForFile(String name) {

        logger.info("Redo search for file");
//        FileHandle projectFolder = SceneEditorAddon.get().workspace.getProjectFolder();
//        return recursiveSearch(projectFolder, name);
        return Gdx.files.local(".");
    }

    private FileHandle recursiveSearch(FileHandle dir, String name) {
        if(dir.child(name).exists()) return dir.child(name);

        FileHandle[] list = dir.list();
        FileHandle found;
        for(FileHandle child: list) {
            if(child.isDirectory()) {
                found = recursiveSearch(child, name);
                if(found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}
