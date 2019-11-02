package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.Skin;

import java.util.Objects;

public class SkinAnimationPair {
    public Skin skinName;
    public Animation animationName;

    public static ObjectMap<Integer, SkinAnimationPair> map = new ObjectMap<>();

    public static SkinAnimationPair make(Skin skinName, Animation animationName) {
        int hash = Objects.hash(skinName, animationName);
        if(map.containsKey(hash)) return map.get(hash);

        SkinAnimationPair pair = new SkinAnimationPair();
        pair.skinName = skinName;
        pair.animationName = animationName;
        map.put(hash, pair);

        return pair;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skinName, animationName);
    }
}