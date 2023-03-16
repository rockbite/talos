package com.talosvfx.talos.runtime.scene.utils.propertyWrappers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.runtime.assets.meta.ScriptMetadata;
import com.talosvfx.talos.runtime.scene.GameObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptMetadataParser {
    private PropertyWrappers scriptPropertyWrappers = new PropertyWrappers();

    BufferedReader reader;

    public ScriptMetadataParser () {
        registerSupportedClasses();
    }

    private void registerSupportedClasses () {
        scriptPropertyWrappers.registerPropertyWrapper(Float.class, PropertyFloatWrapper.class);
        scriptPropertyWrappers.registerPropertyWrapper(Boolean.class, PropertyBooleanWrapper.class);
        scriptPropertyWrappers.registerPropertyWrapper(Integer.class, PropertyIntegerWrapper.class);
        scriptPropertyWrappers.registerPropertyWrapper(String.class, PropertyStringWrapper.class);
        scriptPropertyWrappers.registerPropertyWrapper(GameObject.class, PropertyGameObjectWrapper.class);
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

            PropertyWrapper<?> propertyWrapper = scriptPropertyWrappers.createPropertyWrapperForClazzName(parameterClassName);
            propertyWrapper.collectAttributes(attributes);
            propertyWrapper.propertyName = parameterName;
            metadata.scriptPropertyWrappers.add(propertyWrapper);
        }
    }

    private static class ScriptMetadataParserException extends Exception {
        public ScriptMetadataParserException(String message) {
            super(message);
        }
    }

}
