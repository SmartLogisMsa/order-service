package com.smartlogis.orderservice;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.smartlogis.common.infrastructure.MessageResolver;

public class TestMessageResolver {
	public static void initializeMessageResource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding("UTF-8");
		ReflectionTestUtils.setField(MessageResolver.class, "messageSource", messageSource);
	}
}
