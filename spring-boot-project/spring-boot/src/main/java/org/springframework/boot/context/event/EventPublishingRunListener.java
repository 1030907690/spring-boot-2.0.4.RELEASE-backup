/*
 * Copyright 2012-2018 the original author or authors.
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

package org.springframework.boot.context.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ErrorHandler;

/**
 * {@link SpringApplicationRunListener} to publish {@link SpringApplicationEvent}s.
 * <p>
 * Uses an internal {@link ApplicationEventMulticaster} for the events that are fired
 * before the context is actually refreshed.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 */
public class EventPublishingRunListener implements SpringApplicationRunListener, Ordered {

	private final SpringApplication application;

	private final String[] args;

	private final SimpleApplicationEventMulticaster initialMulticaster;

	/* 读取spring.factories文件 通过反射获取实例时会触发EventPublishingRunListener的构造函数*/
	public EventPublishingRunListener(SpringApplication application, String[] args) {
		this.application = application;
		this.args = args;
		this.initialMulticaster = new SimpleApplicationEventMulticaster();
		for (ApplicationListener<?> listener : application.getListeners()) {
			//重点来看一下addApplicationListener方法
			this.initialMulticaster.addApplicationListener(listener);

			/*
			public void addApplicationListener(ApplicationListener<?> listener) {
				Object var2 = this.retrievalMutex;
				synchronized(this.retrievalMutex) {
					Object singletonTarget = AopProxyUtils.getSingletonTarget(listener);
					if (singletonTarget instanceof ApplicationListener) {
						this.defaultRetriever.applicationListeners.remove(singletonTarget);
					}
					//内部类对象
					this.defaultRetriever.applicationListeners.add(listener);
					this.retrieverCache.clear();
        }
    }

    上述方法定义在SimpleApplicationEventMulticaster父类AbstractApplicationEventMulticaster中。
    关键代码为this.defaultRetriever.applicationListeners.add(listener);，这是一个内部类，
    用来保存所有的监听器。也就是在这一步，将spring.factories中的监听器传递到SimpleApplicationEventMulticaster中。

			* */
		}
	}

	@Override
	public int getOrder() {
		return 0;
	}

	/*
	执行到是 doInvokeListener 因为我们的事件类型为ApplicationEvent，所以会执行onApplicationStartedEvent((ApplicationStartedEvent) event);。
	springBoot会在运行过程中的不同阶段，发送各种事件，来执行对应监听器的对应方法。大同小异，别的监听器执行流程这里不再赘述，后面会有单独的详解。
	* */
	@Override
	public void starting() {
		//关键代码，这里是创建application启动事件`ApplicationStartingEvent`
		/*
		EventPublishingRunListener这个是springBoot框架中最早执行的监听器，在该监听器执行started()方法时，会继续发布事件，
		也就是事件传递。这种实现主要还是基于spring的事件机制。
		* */

		//里面判断是否配置有线程池 ,如果有线程池则用线程池执行,否则同步执行
		this.initialMulticaster.multicastEvent(
				new ApplicationStartingEvent(this.application, this.args));
	}

	@Override
	public void environmentPrepared(ConfigurableEnvironment environment) {
		this.initialMulticaster.multicastEvent(new ApplicationEnvironmentPreparedEvent(
				this.application, this.args, environment));
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {

	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
		for (ApplicationListener<?> listener : this.application.getListeners()) {
			if (listener instanceof ApplicationContextAware) {
				((ApplicationContextAware) listener).setApplicationContext(context);
			}
			context.addApplicationListener(listener);
		}
		this.initialMulticaster.multicastEvent(
				new ApplicationPreparedEvent(this.application, this.args, context));
	}

	@Override
	public void started(ConfigurableApplicationContext context) {
		context.publishEvent(
				new ApplicationStartedEvent(this.application, this.args, context));
	}

	@Override
	public void running(ConfigurableApplicationContext context) {
		context.publishEvent(
				new ApplicationReadyEvent(this.application, this.args, context));
	}

	@Override
	public void failed(ConfigurableApplicationContext context, Throwable exception) {
		ApplicationFailedEvent event = new ApplicationFailedEvent(this.application,
				this.args, context, exception);
		if (context != null && context.isActive()) {
			// Listeners have been registered to the application context so we should
			// use it at this point if we can
			context.publishEvent(event);
		}
		else {
			// An inactive context may not have a multicaster so we use our multicaster to
			// call all of the context's listeners instead
			if (context instanceof AbstractApplicationContext) {
				for (ApplicationListener<?> listener : ((AbstractApplicationContext) context)
						.getApplicationListeners()) {
					this.initialMulticaster.addApplicationListener(listener);
				}
			}
			this.initialMulticaster.setErrorHandler(new LoggingErrorHandler());
			this.initialMulticaster.multicastEvent(event);
		}
	}

	private static class LoggingErrorHandler implements ErrorHandler {

		private static Log logger = LogFactory.getLog(EventPublishingRunListener.class);

		@Override
		public void handleError(Throwable throwable) {
			logger.warn("Error calling ApplicationEventListener", throwable);
		}

	}

}
