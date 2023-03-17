package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtilities {

	/**
	 * @param startClass the class whose fields is being looked
	 * @param exclusiveParent the class till which the reflection should go. can be null.
	 * @return
	 */
	public static Iterable<Field> getFieldsUpTo(Class<?> startClass, Class<?> exclusiveParent) {

		List<Field> currentClassFields = new ArrayList<>(Arrays.asList(startClass.getDeclaredFields()));
		Class<?> parentClass = startClass.getSuperclass();

		if (parentClass != null && (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
			List<Field> parentClassFields = (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
			currentClassFields.addAll(parentClassFields);
		}

		return currentClassFields;
	}

	/**
	 * @param name field name
	 * @param startClass the class whose fields is being looked
	 * @param exclusiveParent the class till which the reflection should go. can be null.
	 * @return
	 * @throws Throwable
	 */
	public static Field getFieldWithName (String name, Class<?> startClass, Class<?> exclusiveParent) throws Throwable {
		Iterable<Field> fieldsUpTo = getFieldsUpTo(startClass, exclusiveParent);
		for (Field field: fieldsUpTo) {
			if (field.getName().equals(name)) {
				return field;
			}
		}

		throw new NoSuchFieldException("No field: " + name + " found in class hierarchy for class: " + startClass.getSimpleName());
	}

	public static Method getMethodRecursive (Class<?> startClazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		Class<?> parentClass = startClazz;

		while (parentClass != null) {
			final Method[] methods = parentClass.getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().equalsIgnoreCase(methodName)) {
					final Class<?>[] parameterTypes1 = method.getParameterTypes();
					if (parameterTypes1.length == parameterTypes.length) {
						boolean found = true;
						for (int i = 0; i < parameterTypes1.length; i++) {
							if (parameterTypes1[i] != parameterTypes[i]) {
								found = false;
								break;
							}
						}
						if (found) {
							return method;
						}
					}
				}
			}
			parentClass = parentClass.getSuperclass();
		}
		throw new NoSuchMethodException("No such method: " + methodName + "(" + Arrays.toString(parameterTypes) + ")");
	}

	public static void setFieldValue (Object instance, String fieldName, Object value) {
		try {
			Field field = getFieldWithName(fieldName, instance.getClass(), null);
				field.setAccessible(true);
				field.set(instance, value);
		} catch (Throwable throwable) {
			throw new GdxRuntimeException(throwable);
		}

	}

	public static <T> T getFieldValue (Object instance, String fieldName, Class<T> returnType) {
		try {
			Field field = getFieldWithName(fieldName, instance.getClass(), null);
			field.setAccessible(true);
			return (T) field.get(instance);
		} catch (Throwable throwable) {
			throw new GdxRuntimeException(throwable);
		}

	}

	public static <T> T getStaticFieldValue (Class<?> clazz, String fieldName, Class<T> returnTYpe) {
		try {
			Field field = getFieldWithName(fieldName, clazz, null);
			field.setAccessible(true);
			return (T) field.get(null);
		} catch (Throwable throwable) {
			throw new GdxRuntimeException(throwable);
		}
	}

	public static <T> T getFieldValue (Object instance, Field field, Class<T> returnType) {
		try {
			field.setAccessible(true);
			return (T) field.get(instance);
		} catch (Throwable throwable) {
			throw new GdxRuntimeException(throwable);
		}

	}

	public static Object callMethod (Object instance, String methodName, Object... params) {
		try {
			Array<Class<?>> paramTypes = new Array<>(Class.class);
			if (params != null) {
				for (Object param : params) {
					paramTypes.add(param.getClass());
				}
			}
			Method method = getMethodRecursive(instance.getClass(), methodName, paramTypes.toArray());
			method.setAccessible(true);
			return method.invoke(instance, params);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException re) {
			throw new GdxRuntimeException(re);
		}
	}

	public static Array<Method> getMethods (Class startClazz) {

		Array<Method> methods = new Array<>();

		Class<?> parentClass = startClazz;

		while (parentClass != null) {
			final Method[] methodz = parentClass.getDeclaredMethods();
			for (Method method : methodz) {
					methods.add(method);
			}
			parentClass = parentClass.getSuperclass();
		}

		return methods;
	}
}
