package com.talosvfx.talos.editor.widgets.propertyWidgets;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ValueProperty {
    float min() default -9999;
    float max() default 9999;
    float step() default 0.01f;
    String[] prefix() default "";
    boolean progress() default false;
}
