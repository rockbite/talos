package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * GameAsset is a potentially complex resource. It links 1+ {@link RawAsset} together to reference
 * all assets required for this GameAsset to load
 */
public class GameAsset<T> {


	public String nameIdentifier;
	public GameAssetType type;

	public Array<RawAsset> dependentRawAssets = new Array<>();
	public Array<GameAsset<?>> dependentGameAssets = new Array<>();


	//Export only

	@Getter
	private ObjectIntMap<GameAsset<?>> gameResourcesThatRequireMe = new ObjectIntMap<>();

	private T resourcePayload;

	private boolean broken;
	private Exception brokenReason;

	@Setter@Getter
	private boolean nonFound;

	@Getter@Setter
	private boolean dummy;

	public void addDependency (GameAsset<?> containerAsset) {
		addDependency(containerAsset, 1);
	}
	public void addDependency (GameAsset<?> containerAsset, int counter) {
		gameResourcesThatRequireMe.getAndIncrement(containerAsset, 0, counter);
		for (int i = 0; i < dependentGameAssets.size; i++) {
			dependentGameAssets.get(i).addDependency(containerAsset, counter);
		}
	}

	public interface GameAssetUpdateListener {
		void onUpdate ();
	}

	public Array<GameAssetUpdateListener> listeners = new Array<>();

	public GameAsset (String nameIdentifier, GameAssetType type) {
		this.nameIdentifier = nameIdentifier;
		this.type = type;
	}

	public void setResourcePayload (T resourcePayload) {
		this.resourcePayload = resourcePayload;
	}

	public T getResource () {
		return this.resourcePayload;
	}

	public boolean isBroken () {
		return broken;
	}

	public void setBroken (Exception e) {
		this.broken = true;
		this.brokenReason = e;
	}

	public void setUpdated () {
		for (GameAssetUpdateListener listener : listeners) {
			listener.onUpdate();
		}
	}

	public RawAsset getRootRawAsset () {
		return dependentRawAssets.first();
	}

	public GameAsset<T> copy () {
		GameAsset<T> copy = new GameAsset<>(nameIdentifier, type);

		for (RawAsset dependentRawAsset : dependentRawAssets) {
			copy.dependentRawAssets.add(dependentRawAsset.copy());
		}

		copy.dependentGameAssets.addAll(dependentGameAssets);
		copy.resourcePayload = resourcePayload;
		copy.broken = broken;
		copy.dummy = dummy;
		copy.nonFound = nonFound;
		return copy;
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GameAsset)) return false;
		GameAsset<?> other = (GameAsset<?>) o;
		return Objects.equals(getRootRawAsset().metaData.uuid, other.getRootRawAsset().metaData.uuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getRootRawAsset().metaData.uuid);
	}
}
