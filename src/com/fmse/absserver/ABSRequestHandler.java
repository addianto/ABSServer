package com.fmse.absserver;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.TemplateResolver;

import ABS.StdLib.List_Cons;
import ABS.StdLib.List_Nil;
import ABS.StdLib.Pair;
import abs.backend.java.lib.runtime.ABSObject;
import abs.backend.java.lib.types.ABSString;
import abs.backend.java.lib.types.ABSValue;

import com.fmse.absserver.helper.DataTransformer;

public class ABSRequestHandler
{
	private static final String ABS_REQUEST_PATTERN = "([^\\s]+(\\.(?i)(abs))$)";
	protected ABSHttpServer absContext;
	
	public ABSRequestHandler(ABSHttpServer absContext)
	{
		this.absContext = absContext;
	}
	
	public void handle(ABSHttpRequest request, OutputStream os) throws Exception
	{
		PrintWriter out = new PrintWriter(os);
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println("Server: ABSServer");
        out.println("");
        
        TemplateResolver templateResolver = new TemplateResolver();
        templateResolver.setTemplateMode("XHTML");
        templateResolver.setSuffix(".html");
        templateResolver.setResourceResolver(new ABSResourceResolver());
        
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        
        if(isRequestForABS(request.getRequestUri()))
        {
        	Class resolver = Class.forName("Framework.Route.RouteConfigImpl_c");
            Object resolverObj = resolver.getMethod("__ABS_createNewObject", ABSObject.class).invoke(resolver, absContext);
            ABSString absResult = (ABSString) resolver.getMethod("route", ABSString.class).invoke(resolverObj, ABSString.fromString(request.getRequestUri()));
            String result = DataTransformer.convertABSStringToJavaString(absResult);              
    		
            String controllerName = result.split("@")[0] + "_c";
            String methodName = result.split("@")[1];
            
            Class controllerClazz = Class.forName(controllerName);
            Object obj = controllerClazz.getMethod("__ABS_createNewObject", ABSObject.class).invoke(controllerClazz, absContext);
            Pair<ABSString, ABS.StdLib.List<ABSValue>> pair = (Pair<ABSString, ABS.StdLib.List<ABSValue>>) obj.getClass().getMethod(methodName).invoke(obj);
            String view = DataTransformer.convertABSStringToJavaString((ABSString) pair.getArg(0));
            List_Cons<ABSValue> data;
            
            Context ctx = new Context();
            if(!(pair.getArg(1) instanceof List_Nil))
            {
            	data = (List_Cons<ABSValue>) pair.getArg(1);
            	List<Object> dataModels = DataTransformer.convertABSListToJavaList(data);
            	ctx.setVariable("dataList", dataModels);
            }
            
            StringWriter writer = new StringWriter();
            templateEngine.process(view, ctx, writer);
            out.println(writer);

            out.flush();
        }
	}
	
	private boolean isRequestForABS(String requestUri)
	{
		Pattern pattern = Pattern.compile(ABS_REQUEST_PATTERN);
		Matcher matcher = pattern.matcher(requestUri);
		
		return matcher.matches();
	}
}
