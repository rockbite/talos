package com.talosvfx.talos.editor.notifications.actions;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.notifications.actions.enums.Actions;


public class ActionParser {
    public static ObjectMap<Actions.ActionEnumInterface, IAction> defaultConfiguration = new ObjectMap<>();
    public static ObjectMap<Actions.ActionEnumInterface, IAction> overriddenConfiguration = new ObjectMap<>();

    public void parseDefaultActions(FileHandle file) {
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(file);

        parseDefaultActions(root);
    }

//    public static void generateEnumFile(XmlReader.Element root, FileHandle destination) {
//        TypeSpec.Builder builder = TypeSpec.classBuilder("Actions").addModifiers(Modifier.PUBLIC)
//                .addJavadoc("This is a generated class. It shouldn't be modified by hand, as the changes would be " +
//                        "overridden.\n")
//                .addJavadoc("To regenerate this class, call generateActionsEnum task from Gradle.\n " +
//                        "The XML file is located in editor/assets/actions.xml");
//
//        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder("ActionEnumInterface");
//        TypeSpec interfaceBuilt = interfaceBuilder.build();
//        builder.addType(interfaceBuilt);
//
//        String interfacePackageName = "com.talosvfx.talos.editor.notifications.actions";
//        JavaFile interfaceJavaFile = JavaFile.builder(interfacePackageName, interfaceBuilt).build();
//
//        ClassName className = ClassName.get(interfaceJavaFile.packageName, interfaceJavaFile.typeSpec.name);
//
//        for (XmlReader.Element actionGroup : root.getChildrenByName("actionGroup")) {
//            String packageName = actionGroup.getAttribute("package");
//            TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(packageName).addSuperinterface(className);
//            enumBuilder.addField(FieldSpec.builder(String.class, "uniqueName", Modifier.PUBLIC, Modifier.FINAL).build());
//            MethodSpec uniqueName = MethodSpec.constructorBuilder().addParameter(String.class, "uniqueName").addStatement("this.unqiqueName = uniqueName;").build();
//            enumBuilder.addMethod(uniqueName);
//            for (XmlReader.Element action : actionGroup.getChildrenByName("action")) {
//                enumBuilder.addEnumConstant(action.getAttribute("name"),
//                        TypeSpec.anonymousClassBuilder("$L",
//                                action.getAttribute("uniqueName")).build());
//            }
//            TypeSpec enumType = enumBuilder.build();
//            builder.addType(enumType);
//        }
//
//        TypeSpec clazzBuild = builder.build();
//        final JavaFile.Builder fileBuilder = JavaFile.builder("com.talosvfx.talos.editor.notification.actions.enums", clazzBuild);
//        JavaFile javaFile = fileBuilder.build();
//
//        try {
//            javaFile.writeTo(destination.file());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void parseDefaultActions(XmlReader.Element root) {
        Array<XmlReader.Element> groups = root.getChildrenByName("actionGroup");
        for (XmlReader.Element group : groups) {

        }
    }
}
