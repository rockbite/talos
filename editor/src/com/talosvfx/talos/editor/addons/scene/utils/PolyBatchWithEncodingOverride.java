package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.graphics.g2d.PolygonBatch;

public interface PolyBatchWithEncodingOverride extends PolygonBatch {

	void setCustomEncodingColour (float r, float g, float b, float a);
	void setUsingCustomColourEncoding (boolean usingCustomEncoding);

	void setCustomInfo (float customInfo);

	void sampleEmissive (boolean sampleEmissive);

    boolean isSamplingEmissive();

    void setIgnoreBlendModeChanges (boolean shouldIgnore);
}
