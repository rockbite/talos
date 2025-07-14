package com.talosvfx.talos.runtime;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.assets.BaseAssetRepository;
import com.talosvfx.talos.runtime.routine.RoutineDefaultEventInterface;
import com.talosvfx.talos.runtime.routine.RoutineEventInterface;
import com.talosvfx.talos.runtime.scene.SceneData;
import com.talosvfx.talos.runtime.utils.ConfigData;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class RuntimeContext {
    private static RuntimeContext context;


    public static class TalosContext {

        @Getter
        private final String identifier;

        @Getter
        private BaseAssetRepository baseAssetRepository;

        @Getter@Setter
        private RoutineDefaultEventInterface routineDefaultEventInterface;

        @Getter@Setter
        private ConfigData configData = new ConfigData();

        public TalosContext (String identifier) {
            this.identifier = identifier;
        }

        public void setBaseAssetRepository (BaseAssetRepository baseAssetRepository) {
            this.baseAssetRepository = baseAssetRepository;
            this.baseAssetRepository.setTalosContext(this);
        }
    }

    public static RuntimeContext getInstance () {
        if (context == null) {
            context = new RuntimeContext();
        }
        return context;
    }

    @Getter
    @Setter
    public SceneData sceneData;


    private ObjectMap<String, TalosContext> talosContextMap = new ObjectMap<>();

    public TalosContext getTalosContext (String key) {
        if (key == null) {
            throw new GdxRuntimeException("trying to access with null key");
        }
        if (talosContextMap.containsKey(key)) {
            return talosContextMap.get(key);
        } else {
            throw new GdxRuntimeException("No context found for identifier " + key);
        }
    }

    public ObjectMap<String, TalosContext> getTalosContextMap () {
        return talosContextMap;
    }

    public void disposeContext () {
        talosContextMap.clear();
        sceneData = null;
        context = null;
    }

    public void registerContext (String talosProjectIdentifier, TalosContext context) {
        talosContextMap.put(talosProjectIdentifier, context);
    }


    @Getter
    @Setter
    private TalosContext editorContext;

}
