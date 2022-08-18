package com.talosvfx.talos.editor.addons.scene.utils.scriptProperties;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.ScriptMetadata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptMetadataParser {

    private final Array<Class<? extends ScriptPropertyWrapper<?>>> registeredTypes = new Array<>();
    private final ObjectMap<String, Class<? extends ScriptPropertyWrapper<?>>> typeObjectMap = new ObjectMap<>();

    BufferedReader reader;

    public ScriptMetadataParser () {
        registerSupportedClasses();
        fillObjectParsingData();
    }

    private void registerSupportedClasses () {
        registeredTypes.add(ScriptPropertyFloatWrapper.class);
    }

    private void fillObjectParsingData () {
        for (Class<? extends ScriptPropertyWrapper<?>> registeredType : registeredTypes) {
            try {
                typeObjectMap.put(registeredType.newInstance().getTypeName(), registeredType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                throw new ScriptMetadataParserException("The field should have at least one [type] and [name] parameters");
            }

            String nameParameter = attributes.first();
            if (!nameParameter.equals("name")) {
                throw new ScriptMetadataParserException("First parameter must be [name]");
            }

            String parameterName = attributes.get(1);
            attributes.removeRange(0, 1);

            String typeParameter = attributes.first();
            if (!typeParameter.equals("type")) {
                throw new ScriptMetadataParserException("First parameter must be [type]");
            }

            String parameterClassName = attributes.get(1);
            Class<? extends ScriptPropertyWrapper<?>> typeClass = typeObjectMap.get(parameterClassName);
            if (typeClass == null) {
                throw new ScriptMetadataParserException("Unsupported type - " + parameterClassName);
            }

            //remove type and leave only arguments
            attributes.removeRange(0, 1);

            try {
                ScriptPropertyWrapper<?> scriptPropertyWrapper = typeClass.newInstance();
                scriptPropertyWrapper.collectAttributes(attributes);
                metadata.scriptPropertyWrappers.add(scriptPropertyWrapper);
                scriptPropertyWrapper.propertyName = parameterName;
            } catch (InstantiationException | IllegalAccessException e) {
                // probably you are screwed
                e.printStackTrace();
            }
        }
    }

    private static class ScriptMetadataParserException extends Exception {
        public ScriptMetadataParserException(String message) {
            super(message);
        }
    }

}
