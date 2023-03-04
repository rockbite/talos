package com.talosvfx.talos.runtime.utils;

public interface Supplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get();
}
