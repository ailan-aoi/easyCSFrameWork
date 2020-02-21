package com.mec.action;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.mec.cs_annocation.ActionMethod;
import com.mec.cs_annocation.HasActionMethod;
import com.my.util.core.PackageScanner;

public class ActionFactory {
	private static Map<String, ActionDefinition> actionMap;
	
	static {
		actionMap = new HashMap<>();
	}
	
	public void addAction(Object Object) {
		increaseActionMapElement(Object);
	}
	
	private static void increaseActionMapElement(Object Object) {
		Method[] methods = Object.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(ActionMethod.class)) {
				ActionMethod actionMethod = method.getAnnotation(ActionMethod.class);
				String action = actionMethod.action();
				ActionDefinition ad = new ActionDefinition().setObject(Object).setMethod(method);
				actionMap.put(action, ad);
			}
		}
	}
	
	public static void scanAction(String packagePath) {
		new PackageScanner() {
			@Override
			public void dealKlass(Class<?> klass) {
				if (klass.isAnnotationPresent(HasActionMethod.class)) {
					try {
						Object object = klass.newInstance();
						increaseActionMapElement(object);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				
			}
		}.packageScanner(packagePath);
	}
	
	public static ActionDefinition getActionDefinition(String action) {
		return actionMap.get(action);
	}
	
}
