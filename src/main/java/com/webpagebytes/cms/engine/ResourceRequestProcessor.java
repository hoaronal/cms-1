/*
 *   Copyright 2014 Webpagebytes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.webpagebytes.cms.engine;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.cms.exception.WPBIOException;
import com.webpagebytes.cms.exception.WPBResourceNotFoundException;

public class ResourceRequestProcessor {

	
	private static Map<String, String> resourcesContentType = new HashMap<String, String>();
	private StaticResourceMap resourcesMap = new StaticResourceMap();
	private static Set<String> noCacheContentType = new HashSet<String>();
	static
	{
		resourcesContentType.put("js", "text/javascript");
		resourcesContentType.put("css", "text/css");
		resourcesContentType.put("png", "image/png");
		resourcesContentType.put("jpg", "image/jpg");
		resourcesContentType.put("gif", "image/gif");
		resourcesContentType.put("html", "text/html");
		resourcesContentType.put("htm", "text/html");
		resourcesContentType.put("jpeg", "text/jpeg");
		resourcesContentType.put("swf", "application/x-shockwave-flash");
		resourcesContentType.put("xap", "application/x-silverlight-app");
		resourcesContentType.put("svg", "image/svg+xml");
		resourcesContentType.put("otf", "application/x-font-otf");
		resourcesContentType.put("eot", "application/vnd.ms-fontobject");
		resourcesContentType.put("ttf", "application/x-font-ttf");
		resourcesContentType.put("woff", "application/x-font-woff");	
		
		noCacheContentType.add("text/html");
		noCacheContentType.add("application/json");
		
		
	}
		
	public void initialize(String adminResourceFolder, String resourcesWhiteList) throws WPBException
	{
		resourcesMap.initialize(adminResourceFolder, resourcesWhiteList);			
	}
	
	
	public StaticResourceMap getResourcesMap() {
		return resourcesMap;
	}


	public void setResourcesMap(StaticResourceMap resourcesMap) {
		this.resourcesMap = resourcesMap;
	}


	public boolean isResourceRequest(String requestUri)
	{
		int lastIndex = requestUri.lastIndexOf('.');
		if (lastIndex <= 0)
		{
			return false;
		}
		String type = requestUri.substring(lastIndex+1);
		type = type.toLowerCase();
		
		if (resourcesContentType.containsKey(type))
		{
			return true;
		}
		return false;
	}
	protected static String addContentType(HttpServletResponse resp, String requestUri)
	{
		int lastIndex = requestUri.lastIndexOf('.');
		if (lastIndex <= 0)
		{
			return null;
		}
		String type = requestUri.substring(lastIndex+1);
		type = type.toLowerCase();
		
		if (resourcesContentType.containsKey(type))
		{
			resp.setContentType(resourcesContentType.get(type));
			return resourcesContentType.get(type);
		}
		return null;
	}
	
	public void process(HttpServletRequest req, 
							   HttpServletResponse resp, 
							   String resource) throws WPBIOException 
	{
		try
		{
			// the resource is something like /build-generated-id/js/ajsfile.js
			if (resource.startsWith("/") && resource.lastIndexOf("/")>0)
			{
				resource = resource.substring(resource.indexOf("/", 1));
			}
			byte[] res = resourcesMap.getResource(resource);
			String contentType;
			if ((contentType = addContentType(resp, resource)) != null)
			{
				if (! noCacheContentType.contains(contentType))
				{
					resp.addHeader("cache-control", "max-age=86400");
				} else
				{
					resp.addHeader("cache-control", "no-cache;no-store;");
				}
				resp.getOutputStream().write(res);				
			} else
			{
				throw new WPBResourceNotFoundException("Not supported content Type for " + resource);
			}
			
		} 
		catch (IOException e)
		{
			throw new WPBIOException("Error processing resource " + resource ,e);
		}
		catch (WPBResourceNotFoundException e)
		{
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
