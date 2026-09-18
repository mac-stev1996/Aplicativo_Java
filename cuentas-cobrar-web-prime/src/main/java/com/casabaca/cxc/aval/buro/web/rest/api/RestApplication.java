package com.casabaca.cxc.aval.buro.web.rest.api;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;

import com.casabaca.cxc.aval.buro.web.rest.api.client.impl.ServiciosRestCxcImpl;
import com.casabaca.exception.ServiceLocatorException;


@ApplicationPath("/restapi")
public class RestApplication extends Application {
	private Set<Object> singletons = new HashSet();
	private Set<Class<?>> empty = new HashSet();

	public RestApplication() throws ServiceLocatorException {
		CorsFilter corsFilter = new CorsFilter();
		corsFilter.getAllowedOrigins().add("*");
		corsFilter.setAllowedMethods("GET, POST, DELETE, PUT");

		// ADD YOUR RESTFUL RESOURCES HERE
		this.singletons.add(new ServiciosRestCxcImpl());
		
		this.singletons.add(corsFilter);

	}

	public Set<Class<?>> getClasses() {
		return this.empty;
	}
	
	@Override
	public Set<Object> getSingletons() {
		return this.singletons;
	}
}