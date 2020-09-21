package com.talosvfx.talos.editor.addons.treedata.workspace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlWriter;
import com.talosvfx.talos.editor.addons.treedata.nodes.BasicDataNode;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.Notifications;

import java.io.IOException;
import java.io.StringWriter;

public class TreeDataNodeStage extends DynamicNodeStage implements Notifications.Observer {

    private  XmlReader.Element coreData;
    private  XmlReader.Element secondaryData;
    private  XmlReader.Element lockedData;

    public TreeDataNodeStage (Skin skin) {
        super(skin);
    }

    @Override
    protected XmlReader.Element loadData () {

        FileHandle list = Gdx.files.internal("addons/treedata/nodes.xml");
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root = xmlReader.parse(list);

        coreData = root;

        return coreData;
    }

    @Override
    public void setData(XmlReader.Element nodeData) {
        secondaryData = nodeData;
    }

    @Override
    public void init() {
        loadNodeList();
    }

    @Override
    protected void loadNodeList() {
        super.loadNodeList();
        if(secondaryData != null) {
            nodeListPopup.addData(secondaryData);
        }
    }

    public String getExportData() {
        StringWriter writer = new StringWriter();
        XmlWriter xml = new XmlWriter(writer);

        // gather all nodes that don't have any outputs connected
        Array<NodeWidget> nodes = nodeBoard.getNodes();
        Array<BasicDataNode> finalNodeList = new Array<>();

        for (NodeWidget node: nodes) {
            if (node instanceof BasicDataNode && (node.getOutputs() == null || node.getOutputs().size == 0)) {
                finalNodeList.add((BasicDataNode) node);
            }
        }

        try {

            if (finalNodeList.size == 1) {
                finalNodeList.first().writeXML(xml);
            } else {
                XmlWriter root = xml.element("list");

                for (BasicDataNode node: finalNodeList) {
                    node.writeXML(root);
                }

                root.pop();
            }

            return writer.toString();

        } catch (IOException e) {
            e.printStackTrace();

            return "";
        }

    }
}
