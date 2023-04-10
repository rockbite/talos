package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.utils.Array;

import java.util.function.Function;

public class CollectionFunctionalUtils {
    public static <T, K> Array<K> map(Array<T> array, Function<? super T, ? extends K> mapper) {
        Array<K> newArr = new Array<>(array.size);
        for (T obj: array) {
            newArr.add(mapper.apply(obj));
        }
        return newArr;
    }
}
