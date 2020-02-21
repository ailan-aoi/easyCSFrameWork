package com.mec.action;

import java.lang.reflect.Method;

class ActionDefinition {
	private Class<?> klass;
	private Object object;
	private Method method;
	private Parameters parameters;
	
	public ActionDefinition() {
		parameters = new Parameters();
	}
	
	public Class<?> getKlass() {
		return klass;
	}
	
	public ActionDefinition setKlass(Class<?> klass) {
		try {
			this.klass = klass;
			setObject(klass.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return this;
	}
	
	public Object getObject() {
		return object;
	}
	
	public ActionDefinition setObject(Object object) {
		this.object = object;
		
		return this;
	}
	
	public Method getMethod() {
		return method;
	}
	
	public ActionDefinition setMethod(Method method) {
		this.method = method;
		setParameters(method);

		return this;
	}
	
	public Parameters getParameters() {
		return parameters;
	}
	
	private ActionDefinition setParameters(Method method) {
		parameters.setParameters(method.getParameters());
		
		return this;
	}
	
	public Object[] getParameterValues(String paraJson) {
		return parameters.getparasValue(paraJson);
	}
}
