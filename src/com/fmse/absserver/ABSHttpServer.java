package com.fmse.absserver;

import ABS.StdLib.List_Cons;
import ABS.StdLib.List_Nil;
import ABS.StdLib.Map;
import ABS.StdLib.Pair;
import ABS.StdLib.abs___f;
import Model.PaymentMessage.PaymentMessageImpl_c;
import abs.backend.java.lib.runtime.ABSObject;
import abs.backend.java.lib.runtime.COG;
import abs.backend.java.lib.runtime.StartUp;
import abs.backend.java.lib.types.ABSString;
import abs.backend.java.lib.types.ABSUnit;
import abs.backend.java.lib.types.ABSValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.TemplateResolver;

import com.fmse.absserver.helper.DataTransformer;

/**
 *
 * @author Salman
 */
public class ABSHttpServer extends ABSObject 
{

    @Override
    public String getClassName() 
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getFieldNames() 
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public ABSHttpServer(COG cog) 
    {
        super(cog);
    }
    
    @Override
    public ABSUnit run() 
    {
    	ServerSocket serverSocket = null;
    	boolean isRunning = true;
    	
        try
        {
            serverSocket = new ServerSocket(8080);

            while(isRunning) {
                Socket remote = serverSocket.accept();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(remote.getInputStream()));
                
                String httpRequestLine = in.readLine();
                String requestMethod = httpRequestLine.split(" ")[0];
                String requestUri = httpRequestLine.split(" ")[1];
                String requestVersion = httpRequestLine.split(" ")[2];
                
                System.out.println(httpRequestLine);
                
                String line;
                HashMap<String, String> headers = new HashMap<String, String>();
                while((line = in.readLine()) != null)
                {
                	if(line.equals("")) 
                	{ 
                		break; 
            		}
                	
                	if(!line.equals(" "))
                	{
                		String[] protocol = line.split(": ");
                		headers.put(protocol[0], protocol[1]);
                		System.out.println(line);
                	}
                }
                
                if(requestMethod.equals("POST"))
                {
                	System.out.println("Getting POST Request");
                	char[] buffer = new char[100];
                	in.read(buffer);
                	
                	System.out.println(buffer);
                }
                
                ABSHttpRequest request = new ABSHttpRequest();
                request.setRequestMethod(requestMethod);
                request.setRequestUri(requestUri);
                request.setRequestVersion(requestVersion);
                request.setHeaders(headers);
                
                ABSRequestHandler handler = new ABSRequestHandler(this);
                handler.handle(request, remote.getOutputStream());
                
                remote.close();
            }
            
            serverSocket.close();
        }
        catch(Exception e) 
        {
            e.printStackTrace();
        }
        
        return ABSUnit.UNIT;
    }
    
    public static void main(String[] args) throws Exception 
    {
        StartUp.startup(new String[0], ABSHttpServer.class);
    }
    
}
