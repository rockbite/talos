package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.Array;
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

	private T resourcePayload;

	private boolean broken;
	private Exception brokenReason;

	@Setter@Getter
	private boolean nonFound;

	@Getter@Setter
	private boolean dummy;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GameAsset)) return false;
		GameAsset<?> other = (GameAsset<?>) o;
		return other.type == this.type
				&& Objects.equals(nameIdentifier, other.nameIdentifier)
				&& Objects.equals(getRootRawAsset().metaData.uuid, other.getRootRawAsset().metaData.uuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nameIdentifier, type, getRootRawAsset().metaData.uuid);
	}
}
