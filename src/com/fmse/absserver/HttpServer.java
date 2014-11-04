package com.fmse.absserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

import abs.backend.java.lib.runtime.ABSObject;
import abs.backend.java.lib.runtime.COG;
import abs.backend.java.lib.runtime.StartUp;
import abs.backend.java.lib.types.ABSUnit;

/**
 *
 * @author Salman El Farisi
 */
public class HttpServer extends ABSObject {
	
	private static final int DEFAULT_PORT = 8080;

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
    
    public HttpServer(COG cog) 
    {
        super(cog);
    }
    
    @Override
    public ABSUnit run() {
    	boolean isRunning = true;
    	
        try(ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {

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
                
                HttpRequest request = new HttpRequest();
                request.setRequestMethod(requestMethod);
                request.setRequestUri(requestUri);
                request.setRequestVersion(requestVersion);
                request.setHeaders(headers);
                
                if(requestMethod.equals("POST"))
                {
                	System.out.println("Getting POST Request");
                	HashMap<String, String> requestHeaders = request.getHeaders();
                	Integer contentLength = Integer.parseInt(requestHeaders.get("Content-Length"));
                	char[] buffer = new char[contentLength];
                	in.read(buffer);
                	
                	String inputString = URLDecoder.decode(new String(buffer), "UTF-8");
                	String[] inputs = inputString.split("&");
                	
                	HashMap<String, String> requestInputs = new HashMap<String, String>();
                	for (String input : inputs) 
                	{
						String key = input.split("=")[0];
						String value = input.split("=")[1];
						System.out.println(value);
						requestInputs.put(key, value);
					}
                	
                	request.setRequestInputs(requestInputs);
                }
                
                RequestHandler handler = new RequestHandler(this);
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
        StartUp.startup(new String[0], HttpServer.class);
    }
    
}
