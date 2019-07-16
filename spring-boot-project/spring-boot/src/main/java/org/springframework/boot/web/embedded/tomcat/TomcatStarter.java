/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.web.embedded.tomcat;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.web.servlet.ServletContextInitializer;

/**
 * {@link ServletContainerInitializer} used to trigger {@link ServletContextInitializer
 * ServletContextInitializers} and track startup errors.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
class TomcatStarter implements ServletContainerInitializer {

	private static final Log logger = LogFactory.getLog(TomcatStarter.class);

	private final ServletContextInitializer[] initializers;

	private volatile Exception startUpException;

	TomcatStarter(ServletContextInitializer[] initializers) {
		this.initializers = initializers;
	}

	/*
	Embedded servlet containers do not directly execute the Servlet 3.0+ javax.servlet.ServletContainerInitializer interface or Spring’s org.springframework.web.WebApplicationInitializer interface. This is an intentional design decision intended to reduce the risk that third party libraries designed to run inside a war may break Spring Boot applications.

	If you need to perform servlet context initialization in a Spring Boot application, you should register a bean that implements the org.springframework.boot.web.servlet.ServletContextInitializer interface. The single onStartup method provides access to the ServletContext and, if necessary, can easily be used as an adapter to an existing WebApplicationInitializer.

	Scanning for Servlets, Filters, and listeners

	When using an embedded container, automatic registration of classes annotated with @WebServlet, @WebFilter, and @WebListener can be enabled by using @ServletComponentScan.
	* */
	//StandardContext#startInternal()调用这里
	@Override
	public void onStartup(Set<Class<?>> classes, ServletContext servletContext)
			throws ServletException {
		try {
			//  遍历ServletContextInitializer  ServletWebServerApplicationContext注册servlet的操作
			for (ServletContextInitializer initializer : this.initializers) {
				initializer.onStartup(servletContext);
			}
		}
		catch (Exception ex) {
			this.startUpException = ex;
			// Prevent Tomcat from logging and re-throwing when we know we can
			// deal with it in the main thread, but log for information here.
			if (logger.isErrorEnabled()) {
				logger.error("Error starting Tomcat context. Exception: "
						+ ex.getClass().getName() + ". Message: " + ex.getMessage());
			}
		}
	}

	public Exception getStartUpException() {
		return this.startUpException;
	}

}
