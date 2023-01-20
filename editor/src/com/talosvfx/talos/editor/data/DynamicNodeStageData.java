package com.talosvfx.talos.editor.data;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeGroup;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import lombok.Data;

@Data
public class DynamicNodeStageData implements Json.Serializable {

	public Array<NodeBoard.NodeConnection> nodeConnections = new Array<>();
	public Array<NodeWidget> nodes = new Array<>();

	public Array<NodeGroup> groups = new Array<>();
	private JsonValue jsonNodes;
	private JsonValue jsonConnections;
	private JsonValue jsonGroups;

	@Override
	public void write (Json json) {

		json.writeArrayStart("list");
		for (NodeWidget node : nodes) {
			json.writeValue(node);
		}
		json.writeArrayEnd();

		json.writeArrayStart("connections");
		for (NodeBoard.NodeConnection connection : nodeConnections) {
			json.writeObjectStart();
			json.writeValue("fromNode", connection.fromNode.getUniqueId());
			json.writeValue("toNode", connection.toNode.getUniqueId());
			json.writeValue("fromSlot", connection.fromId);
			json.writeValue("toSlot", connection.toId);
			json.writeObjectEnd();
		}
		json.writeArrayEnd();

		json.writeArrayStart("groups");
		for (NodeGroup group : groups) {
			json.writeObjectStart();
			json.writeValue("name", group.getText());
			json.writeValue("color", group.getFrameColor());
			json.writeArrayStart("nodes");

			for (NodeWidget nodeWidget : group.getNodes()) {
				json.writeValue(nodeWidget.getUniqueId());
			}

			json.writeArrayEnd();
			json.writeObjectEnd();
		}
		json.writeArrayEnd();
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		jsonNodes = jsonData.get("list");
		jsonConnections = jsonData.get("connections");
		jsonGroups = jsonData.get("groups");
	}


	public <T extends DynamicNodeStageData> void constructForUI (DynamicNodeStage<T> dynamicNodeStage) {
		NodeBoard<T> nodeBoard = dynamicNodeStage.getNodeBoard();

		Json json = new Json();

		int idCounter = 0;

		IntMap<NodeWidget> nodeMap = new IntMap<>();

		if (jsonNodes == null) {
			return;
		}

		for (JsonValue nodeData : jsonNodes) {
			String nodeName = nodeData.getString("name");

			Class clazz = dynamicNodeStage.getNodeListPopup().getNodeClassByName(nodeName);
			String nodeClassName = dynamicNodeStage.getNodeListPopup().getClassNameFromModuleName(nodeName);
			if (clazz != null) {
				NodeWidget node = dynamicNodeStage.createNode(nodeName, 0, 0);
				node.constructNode(dynamicNodeStage.getNodeListPopup().getModuleByName(nodeName));
				node.read(json, nodeData);
				idCounter = Math.max(idCounter, node.getUniqueId());
				nodeMap.put(node.getUniqueId(), node);
			}
		}

		nodeBoard.clearMap();
		for (IntMap.Entry<NodeWidget> entry : nodeMap) {
			nodeBoard.registerNodeId(entry.value);
		}

		nodeBoard.globalNodeCounter = idCounter + 1;

		if (jsonConnections != null) {
			for (JsonValue connectionData : jsonConnections) {
				int fromNode = connectionData.getInt("fromNode");
				int toNode = connectionData.getInt("toNode");
				String fromSlot = connectionData.getString("fromSlot");
				String toSlot = connectionData.getString("toSlot");

				NodeWidget fromWidget = nodeMap.get(fromNode);
				NodeWidget toWidget = nodeMap.get(toNode);

				nodeBoard.makeConnection(fromWidget, toWidget, fromSlot, toSlot);
			}
		}

		ObjectSet<NodeWidget> subNodeList = new ObjectSet<>();
		if (jsonGroups != null) {
			for (JsonValue groupData : jsonGroups) {
				String name = groupData.getString("name");
				Color color = json.readValue(Color.class, groupData.get("color"));
				JsonValue childNodeIds = groupData.get("nodes");
				subNodeList.clear();
				for (JsonValue idVal : childNodeIds) {
					int id = idVal.asInt();
					subNodeList.add(nodeMap.get(id));
				}
				NodeGroup nodeGroup = nodeBoard.createGroupForNodes(subNodeList);
				nodeGroup.setText(name);
				nodeGroup.setColor(color);
			}
		}
	}

	public void clear () {
		System.out.println("DynamicNodeStageData.java\t nodeConnections cleared\t");
		nodeConnections.clear();
		System.out.println("DynamicNodeStageData.java\t nodes cleared\t");
		nodes.clear();

		System.out.println("DynamicNodeStageData.java\t groups cleared\t");
		groups.clear();
	}
}
