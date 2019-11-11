package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.runtime.modules.AbstractModule;

public class GraphCompiler {

    private final ParticleEmitterDescriptor graph;

    private ObjectMap<String, IntMap<String>>  valueDeclarations = new ObjectMap<>();

    public GraphCompiler(ParticleEmitterDescriptor graph) {
        this.graph = graph;
    }

    public String compile() {
        valueDeclarations.clear();
        String mainCode =  processModule("", graph.getParticleModule());

        String declarations = "";
        for(String key: valueDeclarations.keys()) {
            for(int i = 0; i < valueDeclarations.get(key).size; i++) {
                String type = valueDeclarations.get(key).get(i);
                declarations += type + " " + key + "_"+i+" = new " + type + "();\n";
            }
        }

        String result = declarations + mainCode;

        return result;
    }

    private String processModule(String code, AbstractModule module) {
        IntMap<Slot> inputSlots = module.getInputSlots();
        ObjectMap<Integer, String> inputMap = new ObjectMap<>();

        String moduleName = module.getClass().getSimpleName().toLowerCase();

        if(module.getOutputSlots().size > 0) {
            String key = moduleName + module.getIndex() + "_o";
            IntMap<String> types = new IntMap<>();
            for(Slot outputSlot: module.getOutputSlots().values()) {
                types.put(outputSlot.getIndex(), outputSlot.getValue().getClass().getSimpleName());
            }
            valueDeclarations.put(key, types);
        }

        for(Slot slot : inputSlots.values()) {
            Slot targetSlot = slot.getTargetSlot();
            if(targetSlot == null) continue;
            AbstractModule targetModule = slot.getTargetModule();
            code = processModule(code, targetModule);

            String targetModuleName = targetModule.getClass().getSimpleName().toLowerCase();
            int index = targetModule.getIndex();
            String outputName = targetModuleName + index + "_o_" + targetSlot.getIndex();
            inputMap.put(slot.getIndex(), outputName);
        }

        String codeTemplate = module.getJavaTemplate();
        int index = module.getIndex();
        String outputName = moduleName + index + "_o";
        // replacing output names
        codeTemplate = codeTemplate.replaceAll("\\{\\$[o]([0-9]*)\\}", outputName + "\\_$1");

        // replace inputs
        for(Integer key: inputMap.keys()) {
            String varname = inputMap.get(key);
            codeTemplate = codeTemplate.replaceAll("\\{\\$[i]([" + key + "]*)\\}", varname);
        }

        code += codeTemplate + "\n";

        return code;
    }
}
