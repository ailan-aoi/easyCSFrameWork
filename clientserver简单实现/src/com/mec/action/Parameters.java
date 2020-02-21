package com.mec.action;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mec.cs_annocation.ActionPara;

class Parameters {
	private static final Gson gson = new GsonBuilder().create();
	private Type mapType = new TypeToken<Map<String, String>>() {}.getType();
	private Parameter[] paras;
	
	Parameters() {
	}
	
	void setParameters(Parameter[] parameters) {
		this.paras = parameters;
	}
	
	Object[] getparasValue(String paraJson) {
		List<Object> values = new ArrayList<>();
		System.out.println("接到的json类型的参数是" + paraJson);
		Map<String, String> paraMap = gson.fromJson(paraJson, mapType);
		for (Parameter para : paras) {
			ActionPara actionPara = para.getAnnotation(ActionPara.class);
			Object obj = gson.fromJson(paraMap.get(actionPara.para()), para.getType());
			System.out.println("参数actionPara的值:" + obj);
			values.add(obj);
		}
		
		return values.toArray();
	}
	
}
