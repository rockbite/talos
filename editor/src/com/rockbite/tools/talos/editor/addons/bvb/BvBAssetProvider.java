package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.runtime.assets.AssetProvider;

import java.io.File;

public class BvBAssetProvider implements AssetProvider {
    ObjectMap<String, TextureRegion> regionMap = new ObjectMap<String, TextureRegion>();

    private String lookupPath;

    @Override
    public TextureRegion findRegion (String s) {
        TextureRegion region = regionMap.get(s);
        if(region == null) {
            FileHandle handle = findFile(s);
            if(handle != null) {
                region = new TextureRegion(new Texture(handle));
                regionMap.put(s, region);
            }
        }

        return region;
    }

    private FileHandle findFile(String regionName) {
        String fileName = regionName + ".png";
        FileHandle handle = Gdx.files.absolute(lookupPath + File.separator + fileName);
        return TalosMain.Instance().ProjectController().findFile(handle);
    }

    public void setParticleFolder(String path) {
        lookupPath = path;
    }
}
