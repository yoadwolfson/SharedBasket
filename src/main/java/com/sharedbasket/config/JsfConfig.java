package com.sharedbasket.config;

import com.sun.faces.config.ConfigureListener;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.faces.webapp.FacesServlet;

/**
 * JSF bootstrap is defined in src/main/webapp/WEB-INF/web.xml.
 */
@Configuration
public class JsfConfig {

	@Bean
	public ServletContextInitializer jsfInitializer() {
		return servletContext -> {
			servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
			servletContext.setInitParameter("javax.faces.FACELETS_LIBRARIES", "/WEB-INF/custom-tags.xml");
		};
	}

	@Bean
	public ServletRegistrationBean<FacesServlet> facesServlet() {
		ServletRegistrationBean<FacesServlet> registration =
			new ServletRegistrationBean<>(new FacesServlet(), "*.xhtml", "/faces/*");
		registration.setName("Faces Servlet");
		registration.setLoadOnStartup(1);
		return registration;
	}

	@Bean
	public ServletListenerRegistrationBean<ConfigureListener> configureListener() {
		return new ServletListenerRegistrationBean<>(new ConfigureListener());
	}
}
