package com.mec.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActionExecutor {
	
	public static Object executorRequest(String action, String paraJson) throws Exception, IllegalArgumentException, InvocationTargetException {
		ActionDefinition at = ActionFactory.getActionDefinition(action);
		Method method = at.getMethod();
		Object object = at.getObject();
		
		Object[] objs = at.getParameterValues(paraJson);
		return method.invoke(object, objs);
	}
	
	public static Object executorResponse(String action, String paraJson) throws Exception, IllegalArgumentException, InvocationTargetException {
		ActionDefinition at = ActionFactory.getActionDefinition(action);
		Method method = at.getMethod();
		Object object = at.getObject();
		
		if (method.getParameters().length == 0) {
			return method.invoke(object);
		}
		return method.invoke(object, at.getParameterValues(paraJson)[0]);
	}
	
}
