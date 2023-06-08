package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.Curve;
import com.talosvfx.talos.editor.addons.shader.nodes.ColorOutput;
import com.talosvfx.talos.editor.data.DynamicNodeStageData;
import com.talosvfx.talos.editor.notifications.*;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeConnectionCreatedEvent;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeConnectionRemovedEvent;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeDataModifiedEvent;
import com.talosvfx.talos.editor.notifications.events.dynamicnodestage.NodeRemovedEvent;
import com.talosvfx.talos.editor.render.Render;
import com.talosvfx.talos.runtime.vfx.Slot;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class NodeBoard<T extends DynamicNodeStageData> extends WidgetGroup implements Observer, EventContextProvider<DynamicNodeStage<?>> {

	private static final Logger logger = LoggerFactory.getLogger(NodeBoard.class);

	private Skin skin;

	ShapeRenderer shapeRenderer;
	private Curve activeCurve;
	private Bezier<Vector2> bezier = new Bezier<>();
	private Vector2[] curvePoints = new Vector2[4];
	Vector2 tmp = new Vector2();
	Vector2 tmp2 = new Vector2();
	Vector2 tmp3 = new Vector2();
	Vector3 vec3 = new Vector3();
	Vector2 prev = new Vector2();

	public static Color curveColor = new Color(1, 1, 1, 0.4f);
	public static Color curveColorSelected = new Color(1, 1, 1, 0.8f);

	public int globalNodeCounter = 0;
	private ObjectIntMap<Class<? extends NodeWidget>> nodeCounter = new ObjectIntMap<>();

	private ObjectSet<NodeWidget> selectedNodes = new ObjectSet<>();

	private DynamicNodeStage nodeStage;

	private NodeWidget ccFromNode = null;
	private String ccFromSlot = null;
	private boolean ccCurrentIsInput = false;
	public boolean ccCurrentlyRemoving = false;
	private NodeWidget wasNodeSelectedOnDown = null;
	private NodeWidget wasNodeDragged = null;

	public Group groupContainer = new Group();
	public Group mainContainer = new Group();
	private Color tmpColor = new Color();
	private NodeConnection hoveredConnection = null;
	private ObjectMap<Integer, NodeWidget> nodeMap = new ObjectMap<>();

	public void reset () {
		nodeCounter = new ObjectIntMap<>();
		selectedNodes.clear();
		if (nodeStage.data != null) {
			nodeStage.data.clear();
		}

		mainContainer.clearChildren();
		groupContainer.clearChildren();
	}

	public NodeConnection getHoveredConnection () {
		return hoveredConnection;
	}

	public void resetNodes() {
		for (ObjectMap.Entry<Integer, NodeWidget> entry : nodeMap) {
			entry.value.resetNode();
		}
	}

	@Override
	public DynamicNodeStage<?> getContext () {
		return getNodeStage();
	}

	public static class NodeConnection {
		public NodeWidget fromNode;
		public NodeWidget toNode;
		public String fromId;
		public String toId;

		@Getter
		private Actor dataActor = null;

		public boolean basic = false;

		public void setHighlightActor (Actor tmpActor) {
			dataActor = tmpActor;
			basic = false;
		}

		public void unsetHighlightActor () {
			dataActor = null;
		}

		public float getHighlight () {
			if (dataActor == null)
				return -1;
			return dataActor.getX();
		}

		public Color getHighlightColor () {
			if (dataActor == null)
				return NodeBoard.curveColor;
			return dataActor.getColor();
		}

		public void setHighlightActorBasic (Actor tmpActor) {
			dataActor = tmpActor;
			basic = true;
		}
	}

	public NodeBoard (Skin skin, DynamicNodeStage<T> nodeStage) {
		this.skin = skin;

		shapeRenderer = Render.instance().shapeRenderer();

		this.nodeStage = nodeStage;

		curvePoints[0] = new Vector2();
		curvePoints[1] = new Vector2();
		curvePoints[2] = new Vector2();
		curvePoints[3] = new Vector2();

		Notifications.registerObserver(this);

		addActor(groupContainer);
		addActor(mainContainer);
	}


	@Override
	public void draw (Batch batch, float parentAlpha) {
		if(nodeStage.data ==  null) return;
		batch.end();
		shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		drawCurves();
		shapeRenderer.end();
		batch.begin();

		super.draw(batch, parentAlpha);
	}


	private boolean isHoldingNode;
	@Override
	public void act(float delta) {
		super.act(delta);

		if (activeCurve != null || isHoldingNode) {
			nodeStage.shouldAutoMove = true;
		} else {
			nodeStage.shouldAutoMove = false;
		}
	}

	private void drawCurves () {
		// draw active curve
		if (activeCurve != null) {
			shapeRenderer.setColor(0, 203 / 255f, 124 / 255f, 1f);
			drawCurve(activeCurve.getFrom().x, activeCurve.getFrom().y, activeCurve.getTo().x, activeCurve.getTo().y, null, null);
		}

		NodeConnection hoveredConnectionRef = hoveredConnection;
		hoveredConnection = null;

		shapeRenderer.setColor(1, 1, 1, 0.4f);
		// draw nodes
		for (NodeConnection connection : nodeStage.data.nodeConnections) {
			connection.fromNode.getOutputSlotPos(connection.fromId, tmp);
			float x = tmp.x;
			float y = tmp.y;
			connection.toNode.getInputSlotPos(connection.toId, tmp);
			float toX = tmp.x;
			float toY = tmp.y;

			drawCurve(x, y, toX, toY, connection, hoveredConnectionRef);
		}
	}

	private void drawCurve (float x, float y, float toX, float toY, NodeConnection nodeConnection, NodeConnection hoveredConnectionRef) {

		float highlight = -1;
		Color highlightColor = NodeBoard.curveColor;

		if (nodeConnection != null) {
			highlight = nodeConnection.getHighlight();
			highlightColor = nodeConnection.getHighlightColor();
		}

		float minOffset = 10f;
		float maxOffset = 150f;

		float deltaX = Math.abs(toX - x);
		if (deltaX > maxOffset)
			deltaX = maxOffset;
		deltaX = deltaX / maxOffset;

		float offset = minOffset + (maxOffset - minOffset) * deltaX;

		curvePoints[0].set(x, y);
		curvePoints[1].set(x + offset, y);
		curvePoints[2].set(toX - offset, toY);
		curvePoints[3].set(toX + 20f, toY);

		bezier.set(curvePoints, 0, curvePoints.length);

		final float baseSamplesPerLine = 20f;
		final float baseThickness = 2f;
		float samplesPerLine = baseSamplesPerLine;
		float thickness = baseThickness;

		Camera camera = getStage().getCamera();
		if (camera instanceof OrthographicCamera) {
			OrthographicCamera orthographicCamera = (OrthographicCamera) camera;
			final float zoom = orthographicCamera.zoom;
			samplesPerLine = baseSamplesPerLine * (1f / MathUtils.clamp(zoom, 0.4f, 1.0f));
			thickness = baseThickness * MathUtils.clamp(zoom, 1.0f, 10.0f);
		}

		Color mainColor = NodeBoard.curveColor;
		if (hoveredConnectionRef == nodeConnection && nodeConnection != null) {
			thickness *= 1.25f;
			mainColor = curveColorSelected;
		}

		final float step = 1f / samplesPerLine;
		for (float i = 0; i < 1f; i += step) {

			if (highlight >= 0) {
				float highlightDist = Math.abs(i - highlight);

				if (nodeConnection.basic) {
					highlightDist = 0;
				}

				float clamp = 0.2f;
				float interpolationAlpha = MathUtils.clamp(highlightDist, 0, clamp) * (1f / clamp); // 0->1 value, where 0 is max highlight, 1 is default
				tmpColor.a = MathUtils.lerp(highlightColor.a, mainColor.a, interpolationAlpha);
				tmpColor.r = MathUtils.lerp(highlightColor.r, mainColor.r, interpolationAlpha);
				tmpColor.g = MathUtils.lerp(highlightColor.g, mainColor.g, interpolationAlpha);
				tmpColor.b = MathUtils.lerp(highlightColor.b, mainColor.b, interpolationAlpha);
				thickness = 2f + (1f - interpolationAlpha) * 6f;

				if (nodeConnection.basic) {
					thickness = 2f;
				}

				shapeRenderer.setColor(tmpColor);
			} else {
				shapeRenderer.setColor(mainColor);
			}

			bezier.valueAt(tmp, i);
			if (i > 0) {
				shapeRenderer.rectLine(prev.x, prev.y, tmp.x, tmp.y, thickness);
			}
			prev.set(tmp);

			boolean isSegmentHit = segmentHit(prev, tmp);

			if (isSegmentHit) {
				hoveredConnection = nodeConnection;
			}
		}
	}

	private boolean segmentHit (Vector2 p1, Vector2 p2) {

		tmp2.set(Gdx.input.getX(), Gdx.input.getY());
		screenToLocalCoordinates(tmp2);
		tmp3.set(tmp2.x, tmp2.y);

		float dist = Intersector.distanceSegmentPoint(p1, p2, tmp3);

		if (dist < 30) {
			return true; // it's a mario!
		}

		return false;
	}

	public NodeWidget createNode (Class<? extends NodeWidget> clazz, XmlReader.Element config, float screenX, float screenY) {
		NodeWidget node = null;
		try {
			tmp2.set(screenX, screenY);
			screenToLocalCoordinates(tmp2);

			node = ClassReflection.newInstance(clazz);
			node.init(skin, this);
			node.setConfig(config);

			mainContainer.addActor(node);

			node.setPosition(tmp2.x - node.getWidth() / 2f, tmp2.y - node.getHeight() / 2f);

			nodeStage.data.nodes.add(node);

			int counter = nodeCounter.getAndIncrement(clazz, 0, 1);
			node.setId(counter);
			node.setUniqueId(globalNodeCounter++);

		} catch (ReflectionException e) {
			e.printStackTrace();
		}

		return node;
	}

	public void deleteSelectedNodes () {
		// TODO: 23.02.23 dummy refactor
		if (nodeStage.data == null) {
			return;
		}

		NodeRemovedEvent nodeRemovedEvent = Notifications.obtainEvent(NodeRemovedEvent.class);

		for (NodeWidget node : selectedNodes) {
			deleteNode(node);
			nodeRemovedEvent.getNodes().add(node);
		}

		nodeRemovedEvent.setContext(nodeStage);
		Notifications.fireEvent(nodeRemovedEvent);
		clearSelection();
	}

	public void deleteNode (NodeWidget node) {
		for (int i = nodeStage.data.nodeConnections.size - 1; i >= 0; i--) {
			if (nodeStage.data.nodeConnections.get(i).toNode == node || nodeStage.data.nodeConnections.get(i).fromNode == node) {
				removeConnection(nodeStage.data.nodeConnections.get(i), false);
			}
		}

		nodeStage.data.nodes.removeValue(node, true);
		mainContainer.removeActor(node);
		if (ccFromNode == node) {
			ccFromNode = null;
		}

		node.notifyRemoved();
		unRegisterNode(node);

		updateSaveState();
	}

	public <T extends AbstractModule> void tryAndConnectLasCC (NodeWidget nodeWidget) {
		if (ccFromNode != null) {
			Class fromClass;
			Slot fromSlotObject;
			Array<String> toSlots;
			NodeWidget fromModule;
			NodeWidget toModule;
			String fromSlot = null;
			String toSlot = null;
			if (ccCurrentIsInput) {
				toSlots = nodeWidget.getOutputSlots();

				fromModule = nodeWidget;
				toModule = ccFromNode;
				toSlot = ccFromSlot;

			} else {
				toSlots = nodeWidget.getInputSlots();

				fromModule = ccFromNode;
				toModule = nodeWidget;
				fromSlot = ccFromSlot;
			}

			for (int i = 0; i < toSlots.size; i++) {
				String slot = toSlots.get(i);
				// we can connect
				if (ccCurrentIsInput) {
					fromSlot = slot;
				} else {
					toSlot = slot;
				}

				makeConnection(fromModule, toModule, fromSlot, toSlot);
				break;
			}

			ccFromNode = null;
		}
	}

	public NodeConnection findConnection (NodeWidget node, boolean isInput, String key) {
		NodeConnection nodeToFind = null;
		for (NodeConnection nodeConnection : nodeStage.data.nodeConnections) {
			if ((isInput && nodeConnection.toId.equals(key) && node == nodeConnection.toNode) || (!isInput && nodeConnection.toId.equals(key) && node == nodeConnection.fromNode)) {
				// found the node let's remove it
				nodeToFind = nodeConnection;
			}
		}

		return nodeToFind;
	}

	public void removeConnection (NodeConnection connection, boolean fireEvent) {
		//Notifications.fireEvent(Notifications.obtainEvent(NodeConnectionPreRemovedEvent.class).set(connection));
		nodeStage.data.nodeConnections.removeValue(connection, true);

		connection.fromNode.setSlotConnectionInactive(connection, false);
		connection.toNode.setSlotConnectionInactive(connection, true);

		if (fireEvent) {
			Notifications.fireEvent(Notifications.obtainEvent(NodeConnectionRemovedEvent.class).set(getNodeStage(), connection));
		}

		updateSaveState();

//        TalosMain.Instance().ProjectController().setDirty();
	}

	public void setActiveCurve (float x, float y, float toX, float toY, boolean isInput) {
		activeCurve = new Curve(x, y, toX, toY, isInput);
	}

	public void updateActiveCurve (float toX, float toY) {
		if (activeCurve != null) {
			activeCurve.setTo(toX, toY);
		}
	}

	public NodeConnection findConnection (NodeWidget from, NodeWidget to, String slotForm, String slotTo) {
		// a bit slow but...
		for (NodeConnection connection : nodeStage.data.nodeConnections) {
			if (connection.fromNode == from && connection.toNode == to && slotForm.equals(connection.fromId) && slotTo.equals(connection.toId)) {
				return connection;
			}
		}

		return null;
	}

	public NodeConnection addConnectionCurve (NodeWidget from, NodeWidget to, String slotForm, String slotTo) {
		NodeConnection connection = new NodeConnection();
		connection.fromNode = from;
		connection.toNode = to;
		connection.fromId = slotForm;
		connection.toId = slotTo;

		nodeStage.data.nodeConnections.add(connection);

		from.setSlotActive(slotForm, false);
		to.setSlotActive(slotTo, true);

		return connection;
	}

	public void updateConnectionCurve() {

	}

	public void connectNodeIfCan (NodeWidget currentNode, String currentSlot, boolean currentIsInput) {
		Object[] result = new Object[2];
		NodeWidget target = null;
		boolean targetIsInput = false;
		// iterate over all widgets that are not current and see if mouse is over any of their slots, need to only connect input to output or output to input
		for (NodeWidget node : getNodes()) {
			if (node != currentNode) {
				node.findHoveredSlot(result);

				if ((String)result[0] != null) {
					// found match
					target = node;
					if ((int)result[1] == 0) {
						targetIsInput = true;
					} else {
						targetIsInput = false;
					}
					break;
				}
			}
		}

		ccFromNode = null;

		if (target == null || currentIsInput == targetIsInput) {
			// removing
			// show popup (but maybe not in case of removing of existing curve)
			if (activeCurve.getFrom().dst(activeCurve.getTo()) > 20 && !ccCurrentlyRemoving) {
				ccFromNode = currentNode;
				ccFromSlot = currentSlot;
				ccCurrentIsInput = currentIsInput;

				nodeStage.showPopup();
			}
		} else {
			// yay we are connecting
			NodeWidget fromWrapper, toWrapper;
			String fromSlot, toSlot;

			if (targetIsInput) {
				fromWrapper = currentNode;
				toWrapper = target;
				fromSlot = currentSlot;
				toSlot = (String)result[0];
			} else {
				fromWrapper = target;
				toWrapper = currentNode;
				fromSlot = (String)result[0];
				toSlot = currentSlot;
			}

			//what if this already exists?
			if (findConnection(toWrapper, true, toSlot) == null) {
				makeConnection(fromWrapper, toWrapper, fromSlot, toSlot);
			}
		}
		removeActiveCurve();
	}

	public void removeActiveCurve () {
		activeCurve = null;
	}

	public Array<NodeWidget> getNodes () {
		return nodeStage.data.nodes;
	}

	public void makeConnection (NodeWidget from, NodeWidget to, String slotFrom, String slotTo) {
		NodeConnection connection = addConnectionCurve(from, to, slotFrom, slotTo);

		from.attachNodeToMyOutput(to, slotFrom, slotTo);
		to.attachNodeToMyInput(from, slotTo, slotFrom);

		updateSaveState();

//        TalosMain.Instance().ProjectController().setDirty();

		Notifications.fireEvent(Notifications.obtainEvent(NodeConnectionCreatedEvent.class).set(getNodeStage(), connection));
	}

	/**
	 * Selection
	 */
	public void selectNode (NodeWidget node) {
		clearSelection();
		nodeStage.onNodeSelectionChange();
		addNodeToSelection(node);
	}

	public void addNodeToSelection (NodeWidget node) {
		selectedNodes.add(node);
		nodeStage.onNodeSelectionChange();
		updateSelectionBackgrounds();
	}

	public void removeNodeFromSelection (NodeWidget node) {
		selectedNodes.remove(node);
		nodeStage.onNodeSelectionChange();
		updateSelectionBackgrounds();
	}

	public ObjectSet<NodeWidget> getSelectedNodes () {
		return selectedNodes;
	}

	public void setSelectedNodes (ObjectSet<NodeWidget> nodes) {
		selectedNodes.clear();
		selectedNodes.addAll(nodes);
		updateSelectionBackgrounds();
	}

	public void clearSelection () {
		selectedNodes.clear();
		updateSelectionBackgrounds();
	}

	public void updateSelectionBackgrounds () {

		// TODO: 23.02.23 dummy refactor
		if (nodeStage.data == null) {
			 return;
		}

		for (NodeWidget wrapper : nodeStage.data.nodes) {
			if (getSelectedNodes().contains(wrapper)) {
				wrapper.setSelected(true);
			} else {
				wrapper.setSelected(false);
			}
		}
	}

	public void selectAllNodes () {
		// TODO: 23.02.23 dummy refactor
		if (nodeStage.data == null) {
			return;
		}

		ObjectSet<NodeWidget> nodes = new ObjectSet<>();
		for (NodeWidget node : getNodes()) {
			nodes.add(node);
		}
		setSelectedNodes(nodes);
	}

	private Array<NodeConnection> getSelectedConnections () {
		Array<NodeConnection> arr = new Array<>();
		ObjectSet<NodeWidget> nodes = getSelectedNodes();
		Array<NodeConnection> connections = nodeStage.data.nodeConnections;
		for (NodeConnection connection : connections) {
			if (nodes.contains(connection.fromNode) && nodes.contains(connection.toNode)) {
				arr.add(connection);
			}
		}

		return arr;
	}

	public static class ClipboardPayload implements Json.Serializable {
		Array<NodeConnection> connections;
		ObjectSet<NodeWidget> nodes;
		Array<NodeGroup> groups;

		Array<JsonValue> nodeJsonArray = new Array<>();
		Array<JsonValue> connectionsJsonArray = new Array<>();
		Array<JsonValue> groupJsonArray = new Array<>();

		public Vector2 cameraPositionAtCopy = new Vector2();

		public ClipboardPayload () {

		}

		public void set (ObjectSet<NodeWidget> nodes, Array<NodeConnection> connections, Array<NodeGroup> groups) {
			this.nodes = nodes;
			this.connections = connections;
			this.groups = groups;
		}

		@Override
		public void write (Json json) {
			json.writeArrayStart("nodes");
			for (NodeWidget node : nodes) {
				json.writeValue(node);
			}
			json.writeArrayEnd();

			json.writeValue("cameraPositionAtCopy", cameraPositionAtCopy);

			json.writeArrayStart("connections");
			for (NodeBoard.NodeConnection connection : connections) {
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
			JsonValue nodes = jsonData.get("nodes");
			nodeJsonArray.clear();
			connectionsJsonArray.clear();
			for (JsonValue nodeData : nodes) {
				nodeJsonArray.add(nodeData);
			}
			for (JsonValue connectionData : jsonData.get("connections")) {
				connectionsJsonArray.add(connectionData);
			}
			for (JsonValue groupData : jsonData.get("groups")) {
				groupJsonArray.add(groupData);
			}

			cameraPositionAtCopy = json.readValue("cameraPositionAtCopy", Vector2.class, jsonData);
		}
	}

	public void copySelectedModules () {
		// TODO: 23.02.23 dummy refactor
		if (nodeStage.data == null) {
			return;
		}

		Array<NodeConnection> connections = getSelectedConnections();
		ObjectSet<NodeWidget> nodes = getSelectedNodes();
		Array<NodeGroup> groups = getSelectedGroups();

		ClipboardPayload payload = new ClipboardPayload();
		payload.set(nodes, connections, groups);
		Vector3 camPos = getStage().getCamera().position;
		payload.cameraPositionAtCopy.set(camPos.x, camPos.y);

		Json json = new Json();
		String clipboard = json.toJson(payload);
		Gdx.app.getClipboard().setContents(clipboard);
	}

	public void pasteFromClipboard () {
		// TODO: 23.02.23 dummy refactor
		if (nodeStage.data == null) {
			return;
		}

		String clipboard = Gdx.app.getClipboard().getContents();

		ObjectMap<Integer, NodeWidget> previousNodeIdMap = new ObjectMap<>();

		boolean hasShaderModule = false;

		for (NodeWidget node : getNodes()) {
			if (node instanceof ColorOutput)
				hasShaderModule = true;
		}

		Json json = new Json();
		try {
			ClipboardPayload payload = json.fromJson(ClipboardPayload.class, clipboard);

			Vector3 camPosAtPaste = getStage().getCamera().position;
			Vector2 offset = new Vector2(camPosAtPaste.x, camPosAtPaste.y);
			offset.sub(payload.cameraPositionAtCopy);

			Array<JsonValue> nodeDataArray = payload.nodeJsonArray;

			ObjectSet<NodeWidget> copiedNodes = new ObjectSet<>();

			for (JsonValue nodeData : nodeDataArray) {
				String moduleName = nodeData.getString("name");
				Class clazz = nodeStage.getNodeListPopup().getNodeClassByName(moduleName);

				if (clazz == null || (clazz.equals(ColorOutput.class) && hasShaderModule)) {
					continue;
				}

				NodeWidget node = createNode(clazz, nodeStage.getNodeListPopup().getModuleByName(moduleName), 0, 0);
				node.constructNode(nodeStage.getNodeListPopup().getModuleByName(moduleName));
				int uniqueId = node.getUniqueId();
				node.read(json, nodeData);
				node.setUniqueId(uniqueId);
				registerNodeId(node);

				node.moveBy(offset.x, offset.y);

				previousNodeIdMap.put(nodeData.getInt("id"), node); // get old Id
				copiedNodes.add(node);
			}

			// now let's connect the connections
			for (JsonValue connectionData : payload.connectionsJsonArray) {
				int fromNodeId = connectionData.getInt("fromNode");
				int toNodeId = connectionData.getInt("toNode");
				String fromSlot = connectionData.getString("fromSlot");
				String toSlot = connectionData.getString("toSlot");

				NodeWidget fromNode = previousNodeIdMap.get(fromNodeId);
				NodeWidget toNode = previousNodeIdMap.get(toNodeId);
				if (fromNode == null || toNode == null) {
					continue;
				}
				makeConnection(fromNode, toNode, fromSlot, toSlot);
			}

			// now let's add groups
			ObjectSet<NodeWidget> subNodeList = new ObjectSet<>();

			for (JsonValue groupData : payload.groupJsonArray) {
				String name = groupData.getString("name");
				Color color = json.readValue(Color.class, groupData.get("color"));
				JsonValue childNodeIds = groupData.get("nodes");
				subNodeList.clear();
				for (JsonValue idVal : childNodeIds) {
					int id = idVal.asInt();
					subNodeList.add(previousNodeIdMap.get(id));
				}

				NodeGroup nodeGroup = createGroupForNodes(subNodeList);
				nodeGroup.setText(name);
				nodeGroup.setColor(color);
			}

			setSelectedNodes(copiedNodes);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void nodeClicked (NodeWidget node) {
		isHoldingNode = true;

		wasNodeDragged = null;
		if (selectedNodes.contains(node)) {
			wasNodeSelectedOnDown = node;
		} else {
			wasNodeSelectedOnDown = null;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			addNodeToSelection(node);
		} else {
			if (!selectedNodes.contains(node)) {
				selectNode(node);
			}
		}
	}

	public void wrapperMovedBy (NodeWidget node, float x, float y) {
		wasNodeDragged = node;
		if (selectedNodes.size > 1) {
			for (NodeWidget other : selectedNodes) {
				if (other != node) {
					other.moveBy(x, y);
				}
			}
		}
	}

	public void nodeClickedUp (NodeWidget node, boolean hasMoved) {
		isHoldingNode = false;

		if (wasNodeDragged != null && hasMoved) {
			updateSaveState();

//            TalosMain.Instance().ProjectController().setDirty();
		} else {
			// on mouse up when no drag happens this wrapper should be selected unless shift was pressed
			if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
				selectNode(node);
			} else {
				if (wasNodeSelectedOnDown == node) {
					removeNodeFromSelection(node);
				} else {
					addNodeToSelection(node);
				}
			}
		}
	}

	private void updateSaveState () {
		nodeStage.markAssetChanged();
	}

	@EventHandler
	public void onNodeDataModifiedEvent (NodeDataModifiedEvent event) {
		NodeWidget node = event.getNode();
		Array<NodeWidget> affectedNodes = new Array<>();
		collectNodesNodeAffects(affectedNodes, node);
		affectedNodes.removeValue(node, true);

		for (NodeWidget affectedNode : affectedNodes) {
			affectedNode.graphUpdated(); //TODO: this is not currently used but should be for more optimal stuff
		}
	}

	@EventHandler
	public void onNodeConnectionCreated (NodeConnectionCreatedEvent event) {

		// need to find affected node list
		Array<NodeWidget> affectedNodes = new Array<>();
		collectNodesNodeAffects(affectedNodes, event.getConnection().toNode);

		for (NodeWidget node : affectedNodes) {
			node.graphUpdated();
		}
	}

	@EventHandler
	public void onNodeConnectionRemoved (NodeConnectionRemovedEvent event) {
		// need to find affected node list
		Array<NodeWidget> affectedNodes = new Array<>();
		collectNodesNodeAffects(affectedNodes, event.getConnection().toNode);

		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				for (NodeWidget node : affectedNodes) {
					node.graphUpdated();
				}
			}
		});
	}

	private void collectNodesNodeAffects (Array<NodeWidget> nodeList, NodeWidget node) {
		if (nodeList.contains(node, true)) {
			return;
		}

		nodeList.add(node);

		for (Array<NodeWidget.Connection> connections : node.outputs.values()) {
			for (NodeWidget.Connection connection : connections) {
				collectNodesNodeAffects(nodeList, connection.targetNode);
			}
		}
	}

	public NodeGroup createGroupForNodes (ObjectSet<NodeWidget> nodes) {
		if (nodes == null || nodes.size == 0)
			return null;

		for (NodeGroup other : nodeStage.data.groups) {
			other.removeWrappers(nodes);
		}

		NodeGroup group = new NodeGroup(this, skin);
		group.setNodes(nodes);
		nodeStage.data.groups.add(group);

		groupContainer.addActor(group);

		updateSaveState();

		clearSelection();

		return group;
	}

	public void createGroupFromSelectedNodes () {
		// TODO: 23.02.23 dummy refactor
		if (nodeStage.data == null) {
			return;
		}

		createGroupForNodes(getSelectedNodes());
	}

	public void ungroupSelectedNodes () {
		// TODO: 23.02.23 dummy refactor
		if (nodeStage.data == null) {
			return;
		}

		ungroupNodes(getSelectedNodes());
	}

	public void ungroupNodes (ObjectSet<NodeWidget> nodes) {
		if (nodes == null || nodes.size == 0)
			return;

		for (NodeGroup other : nodeStage.data.groups) {
			other.removeWrappers(nodes);
		}

	}

	public void removeGroup (NodeGroup nodeGroup) {
		nodeStage.data.groups.removeValue(nodeGroup, true);
		nodeGroup.remove();
	}

	private Array<NodeGroup> getSelectedGroups () {
		Array<NodeGroup> selectedGroups = new Array<>();
		ObjectSet<NodeWidget> nodes = getSelectedNodes();
		for (NodeGroup group : nodeStage.data.groups) {
			boolean isFullyContained = true;
			for (NodeWidget node : group.getNodes()) {
				if (!nodes.contains(node)) {
					isFullyContained = false;
					break;
				}
			}
			if (isFullyContained) {
				//add this group
				selectedGroups.add(group);
			}
		}

		return selectedGroups;
	}

	public void userSelectionApply (Rectangle rectangle) {
		clearSelection();
		Rectangle moduleRect = new Rectangle();
		for (int i = 0; i < nodeStage.data.nodes.size; i++) {
			NodeWidget node = nodeStage.data.nodes.get(i);
			tmp.set(node.getX(), node.getY());
			tmp.add(mainContainer.getX(), mainContainer.getY());
			localToStageCoordinates(tmp);
			moduleRect.set(tmp.x, tmp.y, node.getWidth(), node.getHeight());
			boolean hit = Intersector.intersectRectangles(rectangle, moduleRect, moduleRect);

			if (hit) {
				// hit
				addNodeToSelection(node);
			}
		}
	}

	public DynamicNodeStage getNodeStage () {
		return nodeStage;
	}

	public void clearMap() {
		nodeMap.clear();
	}
	public void registerNodeId(NodeWidget node) {
		nodeMap.put(node.getUniqueId(), node);

	}
	private void unRegisterNode(NodeWidget node) {
		nodeMap.remove(node.getUniqueId());
	}

	public NodeWidget getNodeById(int nodeId) {
		return nodeMap.get(nodeId);
	}

	public NodeWidget findNode(int uniqueId) {
		return nodeMap.get(uniqueId);
	}

}
