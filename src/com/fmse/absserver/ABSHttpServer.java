package com.fmse.absserver;

import ABS.StdLib.List_Cons;
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
import java.util.HashMap;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.TemplateResolver;

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
    	ServerSocket serverSocket;
        try
        {
            serverSocket = new ServerSocket(8080);

            while(true) {
                Socket remote = serverSocket.accept();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(remote.getInputStream()));

                StringBuilder requestString = new StringBuilder();
                HashMap<String, String> protocolMap = new HashMap<String, String>();
                
                String httpRequestLine = in.readLine();
                String requestMethod = httpRequestLine.split(" ")[0];
                String requestUri = httpRequestLine.split(" ")[1];
                String requestVersion = httpRequestLine.split(" ")[2];
                
                System.out.println(httpRequestLine);
                
                String line;
                while((line = in.readLine()) != null)
                {
                	if(line.equals("")) 
                	{ 
                		break; 
            		}
                	
                	if(!line.equals(" "))
                	{
                		String[] protocol = line.split(": ");
                		protocolMap.put(protocol[0], protocol[1]);
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
                
                System.out.println(requestString.toString());
                PrintWriter out = new PrintWriter(remote.getOutputStream());
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("Server: ABSServer");
                
                out.println("");
                
                String[] protocols = requestString.toString().split("\n");
                String url[] = protocols[0].split(" ");
                
                String html = "<h1>Welcome to ABS HTTP Server</h1>";
                TemplateResolver templateResolver = new TemplateResolver();
                templateResolver.setTemplateMode("XHTML");
                templateResolver.setSuffix(".html");
                templateResolver.setResourceResolver(new ABSResourceResolver());
                
                TemplateEngine templateEngine = new TemplateEngine();
                templateEngine.setTemplateResolver(templateResolver);
                
                if(requestUri.equals("/payment/index.abs")) 
                {
                    Class controller = Class.forName("Controller.PostPaidPayment.PostPaidPaymentControllerImpl_c");
                    Object obj = controller.getMethod("__ABS_createNewObject", ABSObject.class).invoke(controller, this);
                    Pair<ABSString, ABS.StdLib.List<ABSValue>> pair = (Pair<ABSString, ABS.StdLib.List<ABSValue>>) obj.getClass().getMethod("index").invoke(obj);
                    String view = pair.getArg(0).toString().replaceAll("\"", "");
                    System.out.println(view);
                }
                else if(requestUri.equals("/payment/inquiry.abs")) 
                {
                	Class controller = Class.forName("Controller.PostPaidPayment.PostPaidPaymentControllerImpl_c");
                    Object obj = controller.getMethod("__ABS_createNewObject", ABSObject.class).invoke(controller, this);
                    Pair<ABSString, ABS.StdLib.List<ABSValue>> pair = (Pair<ABSString, ABS.StdLib.List<ABSValue>>) obj.getClass().getMethod("inquiry").invoke(obj);
                    String view = pair.getArg(0).toString().replaceAll("\"", "");
                    List_Cons<ABSValue> data = (List_Cons<ABSValue>) pair.getArg(1);
                    
                    System.out.println(((PaymentMessageImpl_c) data.getArg(0)).getCustomerName());
                    System.out.println(((PaymentMessageImpl_c) data.getArg1().getArg(0)).getCustomerName());
                }
               
                out.flush();
                remote.close();
            }
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
