package com.talosvfx.talos.runtime.values;

import com.talosvfx.talos.runtime.modules.AbstractModule;

public class ModuleValue<T extends AbstractModule> extends Value {

	private T module;

	@Override
	public void set (Value value) {
		this.module = ((ModuleValue<T>)value).module;
	}

	public T getModule () {
		return module;
	}

	public <U extends T> void setModule (U module) {
		this.module = module;
	}
}
