package com.fmse.absserver;

import java.util.HashMap;

public class HttpRequest 
{
	protected String requestMethod;
	protected String requestUri;
	protected String requestVersion;
	protected HashMap<String, String> headers;
	protected HashMap<String, String> requestInputs;
	
	public HttpRequest()
	{
		this.headers = new HashMap<String, String>();
		this.requestInputs = new HashMap<String, String>();
	}
	
	public String getRequestMethod() 
	{
		return requestMethod;
	}
	
	public void setRequestMethod(String requestMethod) 
	{
		this.requestMethod = requestMethod;
	}
	
	public String getRequestUri() 
	{
		return requestUri;
	}
	
	public void setRequestUri(String requestUri) 
	{
		this.requestUri = requestUri;
	}
	
	public String getRequestVersion() 
	{
		return requestVersion;
	}
	
	public void setRequestVersion(String requestVersion) 
	{
		this.requestVersion = requestVersion;
	}

	public HashMap<String, String> getHeaders() 
	{
		return headers;
	}

	public void setHeaders(HashMap<String, String> headers) 
	{
		this.headers = headers;
	}

	public HashMap<String, String> getRequestInputs()
	{
		return requestInputs;
	}

	public void setRequestInputs(HashMap<String, String> requestInputs)
	{
		this.requestInputs = requestInputs;
	}
}
