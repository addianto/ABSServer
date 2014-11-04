package com.fmse.absserver;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.TemplateResolver;

import ABS.Framework.Http.ABSHttpRequestImpl_c;
import ABS.Framework.Http.ABSHttpRequest_i;
import ABS.StdLib.List_Cons;
import ABS.StdLib.List_Nil;
import ABS.StdLib.Map;
import ABS.StdLib.Map_InsertAssoc;
import ABS.StdLib.Pair;
import ABS.StdLib.Pair_Pair;
import abs.backend.java.lib.runtime.ABSObject;
import abs.backend.java.lib.types.ABSClass;
import abs.backend.java.lib.types.ABSString;
import abs.backend.java.lib.types.ABSValue;

import com.fmse.absserver.helper.DataTransformer;

public class RequestHandler
{
	private static final String ABS_REQUEST_PATTERN = "([^\\s]+(\\.(?i)(abs))$)";
	protected HttpServer absContext;
	
	public RequestHandler(HttpServer absContext)
	{
		this.absContext = absContext;
	}
	
	public void handle(HttpRequest request, OutputStream os) throws Exception
	{
		PrintWriter out = new PrintWriter(os);
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println("Server: ABSServer");
        out.println("");
        
        TemplateResolver templateResolver = new TemplateResolver();
        templateResolver.setTemplateMode("XHTML");
        templateResolver.setSuffix(".html");
        templateResolver.setResourceResolver(new ResourceResolver());
        
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        
        if(isRequestForABS(request.getRequestUri()))
        {
        	Class resolver = Class.forName("ABS.Framework.Route.RouteConfigImpl_c");
            Object resolverObj = resolver.getMethod("__ABS_createNewObject", ABSObject.class).invoke(resolver, absContext);
            ABSString absResult = (ABSString) resolver.getMethod("route", ABSString.class).invoke(resolverObj, ABSString.fromString(request.getRequestUri()));
            String result = DataTransformer.convertABSStringToJavaString(absResult);              
    		
            String controllerName = result.split("@")[0] + "_c";
            String methodName = result.split("@")[1];
            
            ABSHttpRequest_i absHttpRequest = this.createABSHttpRequest(request);
            Class controllerClazz = Class.forName(controllerName);
            Object obj = controllerClazz.getMethod("__ABS_createNewObject", ABSObject.class).invoke(controllerClazz, absContext);
            Pair<ABSString, ABS.StdLib.List<ABSValue>> pair = (Pair<ABSString, ABS.StdLib.List<ABSValue>>) obj.getClass()
            		.getMethod(methodName, ABSHttpRequest_i.class).invoke(obj, absHttpRequest);
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
	
	private ABSHttpRequest_i createABSHttpRequest(HttpRequest request) throws Exception
	{
		ABS.StdLib.Map<ABSString, ABSString> absRequestInputMap = new ABS.StdLib.Map_EmptyMap<ABSString, ABSString>();
		HashMap<String, String> requestInputMap = request.getRequestInputs();
		Iterator<Entry<String, String>> it = requestInputMap.entrySet().iterator();
		while(it.hasNext())
		{
			java.util.Map.Entry<String, String> pairs = it.next();
			ABSString key = ABSString.fromString(pairs.getKey());
			ABSString value = ABSString.fromString(pairs.getValue());
			Pair<ABSString, ABSString> absPair = new Pair_Pair<ABSString, ABSString>(key, value);
			absRequestInputMap = new ABS.StdLib.Map_InsertAssoc<ABSString, ABSString>(absPair, absRequestInputMap);
		}
		
		ABSHttpRequest_i absHttpRequest = new ABSHttpRequestImpl_c(absRequestInputMap);
		return absHttpRequest;
	}
	
	private boolean isRequestForABS(String requestUri)
	{
		Pattern pattern = Pattern.compile(ABS_REQUEST_PATTERN);
		Matcher matcher = pattern.matcher(requestUri);
		
		return matcher.matches();
	}
}
