package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.ScriptMetadata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptMetadataParser {


    private static class ScriptPropertyWrappers {
        private final ObjectMap<Class, Class<? extends ScriptPropertyWrapper<?>>> registeredTypes = new ObjectMap<>();

        private ObjectMap<String, String> primitiveReplacementMap = new ObjectMap<>();

        ScriptPropertyWrappers () {
            primitiveReplacementMap.put("float", Float.class.getName());
            primitiveReplacementMap.put("int", Integer.class.getName());
            primitiveReplacementMap.put("boolean", Boolean.class.getName());
            primitiveReplacementMap.put("GameObject", GameObject.class.getName());
            primitiveReplacementMap.put("String", String.class.getName());
        }

        <T> void registerPropertyWrapper (Class<T> clazz, Class<? extends ScriptPropertyWrapper<T>> wrapperClazz) {
            this.registeredTypes.put(clazz, wrapperClazz);
        }

        String parseName (String className) {
            if (primitiveReplacementMap.containsKey(className)) {
                className = primitiveReplacementMap.get(className);
            }
            return className;
        }

        boolean supportsProperty (String property) {
            property = parseName(property);
            try {
                Class classForName = ClassReflection.forName(property);
                return registeredTypes.containsKey(classForName);
            } catch (ReflectionException e) {
                e.printStackTrace();
                return false;
            }

        }

        @SuppressWarnings("unchecked")
        <T> ScriptPropertyWrapper<T> createPropertyWrapperForClazz (Class<T> clazz) {
            Class<ScriptPropertyWrapper<T>> aClass = (Class<ScriptPropertyWrapper<T>>)registeredTypes.get(clazz);
            try {
                return ClassReflection.newInstance(aClass);
            } catch (ReflectionException e) {
                throw new RuntimeException(e);
            }
        }

        public <T> ScriptPropertyWrapper<T> createPropertyWrapperForClazzName (String parameterClassName) {
            String clazzName = parseName(parameterClassName);
            Class aClass = null;
            try {
                aClass = ClassReflection.forName(clazzName);
            } catch (ReflectionException e) {
                throw new RuntimeException(e);
            }
            return createPropertyWrapperForClazz(aClass);
        }
    }

    private ScriptPropertyWrappers scriptPropertyWrappers = new ScriptPropertyWrappers();


    BufferedReader reader;

    public ScriptMetadataParser () {
        registerSupportedClasses();
    }

    private void registerSupportedClasses () {
        scriptPropertyWrappers.registerPropertyWrapper(Float.class, ScriptPropertyFloatWrapper.class);
        scriptPropertyWrappers.registerPropertyWrapper(Boolean.class, ScriptPropertyBooleanWrapper.class);
        scriptPropertyWrappers.registerPropertyWrapper(Integer.class, ScriptPropertyIntegerWrapper.class);
        scriptPropertyWrappers.registerPropertyWrapper(String.class, ScriptPropertyStringWrapper.class);
        scriptPropertyWrappers.registerPropertyWrapper(GameObject.class, ScriptPropertyGameObjectWrapper.class);
    }

    public void processHandle(FileHandle handle, ScriptMetadata metadata) {
        try {
            reader = new BufferedReader(new FileReader(handle.file()));
            String line = reader.readLine();
            while (line != null) {
                // read next line
                processLine(line, metadata);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException | ScriptMetadataParserException e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line, ScriptMetadata metadata) throws ScriptMetadataParserException {
        if (line.contains("//@exposed")) {
            String groupRegex = "\\[(.*?)\\]";
            // Create a Pattern object
            Matcher m = Pattern.compile(groupRegex).matcher(line);
            Array<String> attributes = new Array<>();

            while (m.find()) {//Finds Matching Pattern in String
                attributes.add(m.group(1));//Fetching Group from String
            }

            int keyWordsCount = attributes.size;
            if (keyWordsCount % 2 != 0) {
                throw new ScriptMetadataParserException("Invalid contruction of type or arguments");
            }
            if (keyWordsCount < 4) {
                throw new ScriptMetadataParserException("The field must have  [name] and [type] parameters");
            }

            // define name
            String nameParameter = attributes.first();
            if (!nameParameter.equals("name")) {
                throw new ScriptMetadataParserException("First parameter must be [name]");
            }

            String parameterName = attributes.get(1);
            attributes.removeRange(0, 1);

            // define type
            String typeParameter = attributes.first();
            if (!typeParameter.equals("type")) {
                throw new ScriptMetadataParserException("Second parameter must be [type]");
            }

            String parameterClassName = attributes.get(1);
            if (!scriptPropertyWrappers.supportsProperty(parameterClassName)) {
                throw new ScriptMetadataParserException("Unsupported type - " + parameterClassName);
            }

            //remove type and leave only arguments
            attributes.removeRange(0, 1);

            ScriptPropertyWrapper<?> scriptPropertyWrapper = scriptPropertyWrappers.createPropertyWrapperForClazzName(parameterClassName);
            scriptPropertyWrapper.collectAttributes(attributes);
            scriptPropertyWrapper.propertyName = parameterName;
            metadata.scriptPropertyWrappers.add(scriptPropertyWrapper);
        }
    }

    private static class ScriptMetadataParserException extends Exception {
        public ScriptMetadataParserException(String message) {
            super(message);
        }
    }

}
