package com.talosvfx.talos.editor.xmldefinitions;

import java.util.List;

public class ModuleDefinition {

    private String moduleName;
    private String moduleTitle;
    private String identifierName;
    private String displayFriendlyIdentifierName;
    private Class objectClazz;
    private String selectBoxDataSource;

    public void setSelectBoxDataSource (String selectBoxDataSource) {
        this.selectBoxDataSource = selectBoxDataSource;
    }

    public String getSelectBoxDataSource () {
        return selectBoxDataSource;
    }

    public String getDisplayFriendlyIdentifierName () {
        return displayFriendlyIdentifierName;
    }

    public void setDisplayFriendlyIdentifierName (String displayFriendlyIdentifierName) {
        this.displayFriendlyIdentifierName = displayFriendlyIdentifierName;
    }

    public String getIdentifierName () {
        return identifierName;
    }

    public void setIdentifierName (String identifierName) {
        this.identifierName = identifierName;
    }

    public void setModuleName (String moduleName) {
        this.moduleName = moduleName;
    }

    public void setModuleTitle (String moduleTitle) {
        this.moduleTitle = moduleTitle;
    }

    public void setObjectClazz (Class objectClazz) {
        this.objectClazz = objectClazz;
    }

    public void setInputPorts (List<ModuleDefinition> inputPorts) {
        this.inputPorts = inputPorts;
    }

    private List<ModuleDefinition> inputPorts;

    public String getModuleName () {
        return moduleName;
    }

    public String getModuleTitle () {
        return moduleTitle;
    }

    public Class getObjectClazz () {
        return objectClazz;
    }


    public List<ModuleDefinition> getInputPorts () {
        return inputPorts;
    }

    //<modules classPath="com.talosvfx.talos.editor.addons.treedata.nodes">
    //    <category name="general" title="General">
    //        <module name = "UnderwellConfigNode" title = "Underwell Config" class = "BasicDataNode">
    //            <value port="input" name="device" type="fluid">Device</value>
    //        </module>
    //        <module name = "BuildingLevel" title = "Building Level" class = "BasicDataNode">
    //            <value port="input" name="device" type="fluid">Device</value>
    //        </module>
    //        <module name = "Device" title = "Device" class = "BasicDataNode">
    //        	<value port="output" name="output" type="fluid">output</value>
    //        	<group>
    //	            <dynamicValue port="input" name="hpMultiplier" type="float" max = "10" progress="true">hpMultiplier</dynamicValue>
    //	            <dynamicValue port="input" name="dpsMultiplier" type="float" max = "10" progress="true">dpsMultiplier</dynamicValue>
    //	            <dynamicValue port="input" name="speedMultiplier" type="float" max = "10" progress="true">speedMultiplier</dynamicValue>
    //	            <dynamicValue port="input" name="resonatorCount" type="float" max = "10" progress="true">resonatorCount</dynamicValue>
    //	            <dynamicValue port="input" name="spawnPerSecond" type="float" max = "10" progress="true">spawnPerSecond</dynamicValue>
    //	            <dynamicValue port="input" name="suze" type="size" max = "10" progress="true">size</dynamicValue>
    //	            <dynamicValue port="input" name="gameDuration" type="float" max = "10" progress="true">gameDuration</dynamicValue>
    //	            <dynamicValue port="input" name="mobMaxCount" type="float" max = "10" progress="true">mobMaxCount</dynamicValue>
    //	            <dynamicValue port="input" name="warmUp" type="float" max = "10" progress="true">warmUp</dynamicValue>
    //	            <dynamicValue port="input" name="everStonePerSecond" type="float" max = "10" progress="true">everStonePerSecond</dynamicValue>
    //	        </group>
    //        </module>
    //         <module name = "SizeNode" title = "Size" class = "BasicDataNode">
    //            <value port="output" name="output" type="fluid">output</value>
    //            <group>
    //            	<dynamicValue name="width" type="float" step = "1" max="48" progress="true">Width</dynamicValue>
    //            	<dynamicValue name="height" type="float" step = "1" max="48" progress="true">Height</dynamicValue>
    //        	</group>
    //        </module>
    //    </category>


}
