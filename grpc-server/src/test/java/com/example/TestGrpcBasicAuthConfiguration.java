package com.example;

import io.grpc.ClientInterceptor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.grpc.client.interceptor.security.BasicAuthenticationInterceptor;

@TestConfiguration(proxyBeanMethods = false)
class TestGrpcBasicAuthConfiguration {

	@Bean
	@GlobalClientInterceptor
	ClientInterceptor testBasicAuth() {
		return new BasicAuthenticationInterceptor("user", "password");
	}

}
