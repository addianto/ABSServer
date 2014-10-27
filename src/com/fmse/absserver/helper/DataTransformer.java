package com.fmse.absserver.helper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.ListIterator;

import abs.backend.java.lib.runtime.ABSObject;
import abs.backend.java.lib.runtime.ABSRuntime;
import abs.backend.java.lib.types.ABSValue;

import java.util.List;

import com.fmse.absserver.ABSHttpServer;

public class DataTransformer 
{
	public static ArrayList<Object> 
		convertABSListToJavaList(ABS.StdLib.List<ABSValue> target) throws Exception
	{	
		ArrayList<Object> result = new ArrayList<Object>();
		
		do
		{
			ABSObject head = (ABSObject) ABS.StdLib.head_f.apply(target);
			result.add(head);
			
			String canonicalName = head.getClass().getCanonicalName();
			List<String> fieldNames = head.getFieldNames();
			for(String fieldName : fieldNames)
			{
				String methodName = "get" 
						+ fieldName.substring(0, 1).toUpperCase() 
						+ fieldName.substring(1);
			}
			
			target = ABS.StdLib.tail_f.apply(target);
		}
		while(!(target instanceof ABS.StdLib.List_Nil));

		return result;
	}
}