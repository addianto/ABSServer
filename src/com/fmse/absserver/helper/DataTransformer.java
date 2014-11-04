package com.fmse.absserver.helper;

import java.util.ArrayList;

import abs.backend.java.lib.runtime.ABSObject;
import abs.backend.java.lib.types.ABSString;
import abs.backend.java.lib.types.ABSValue;

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
			target = ABS.StdLib.tail_f.apply(target);
		}
		while(!(target instanceof ABS.StdLib.List_Nil));

		return result;
	}
	
	public static String
		convertABSStringToJavaString(ABSString target) throws Exception
	{
		return target.toString().replaceAll("\"", "");
	}
}
