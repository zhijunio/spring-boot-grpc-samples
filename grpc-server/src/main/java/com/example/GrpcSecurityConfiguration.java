package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration(proxyBeanMethods = false)
@Profile("!test")
public class GrpcSecurityConfiguration {

	@Bean
	UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager(
				User.withUsername("user").password("{noop}password").roles("USER").build());
	}

	@Bean
	@GlobalServerInterceptor
	AuthenticationProcessInterceptor authenticationProcessInterceptor(GrpcSecurity grpc) throws Exception {
		return grpc
			.authorizeRequests(requests -> requests
				.methods("com.example.HelloService/SayHello").hasAuthority("ROLE_USER")
				.methods("com.example.HelloService/LotsOfReplies").hasAuthority("ROLE_USER")
				.methods("grpc.*/*").permitAll()
				.allRequests().authenticated())
			.httpBasic(Customizer.withDefaults())
			.preauth(Customizer.withDefaults())
			.build();
	}

}
