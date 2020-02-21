package com.mec.CS_Frame_Work.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class CommandDistributor {
	
	public CommandDistributor() {
	}
	
	private static String getMethodNameByCommand(String command) {
		if (command == null) {
			return null;
		}
		StringBuffer result = new StringBuffer().append("deal");
		String[] parts = command.split("_");
		for (String part : parts) {
			String first = part.substring(0, 1);
			String other = part.substring(1).toLowerCase();
			
			part = first + other;
			result.append(part);
		}
		
		return result.toString();
	}

	static void distributorCommand(Object object, NetMessage message, byte[] binMessage) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> klass = object.getClass();

		String methodName = getMethodNameByCommand(message.getCommand().name());
		if (methodName == null) {
			return;
		}
		Method method = klass.getDeclaredMethod(methodName, new Class<?>[] {NetMessage.class, byte[].class});
		method.invoke(object, new Object[] {message, binMessage});
	}

	static void distributorCommand(Object object, NetMessage message) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> klass = object.getClass();
		
		String methodName = getMethodNameByCommand(message.getCommand().name());
		if (methodName == null) {
			return;
		}
		Method method = klass.getDeclaredMethod(methodName, NetMessage.class);
		method.invoke(object, message);
	}
}
