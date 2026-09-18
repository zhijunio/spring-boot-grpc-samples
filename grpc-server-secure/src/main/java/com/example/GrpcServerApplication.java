package com.example;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.grpc.server.security.GrpcSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import io.grpc.Metadata;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

@SpringBootApplication
public class GrpcServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrpcServerApplication.class, args);
	}

	@Bean
	InMemoryUserDetailsManager inMemoryUserDetailsManager() {
		return new InMemoryUserDetailsManager(
				User.withUsername("user").password("{noop}user").authorities("ROLE_USER").build(),
				User.withUsername("admin").password("{noop}admin").authorities("ROLE_ADMIN").build());
	}

	@Bean
	@GlobalServerInterceptor
	ServerInterceptor securityInterceptor(GrpcSecurity security) throws Exception {
		return security
			.authorizeRequests((requests) -> requests.methods("Simple/StreamHello")
				.hasAuthority("ROLE_ADMIN")
				.methods("Simple/SayHello")
				.hasAuthority("ROLE_USER")
				.methods("grpc.*/*")
				.permitAll()
				.allRequests()
				.denyAll())
			.httpBasic(withDefaults())
			.preauth(withDefaults())
			.build();
	}

	@Bean
	GrpcExceptionHandler grpcExceptionHandler() {
		return (exception) -> {
			if (exception instanceof IllegalArgumentException) {
				Metadata metadata = new Metadata();
				metadata.put(Metadata.Key.of("error-code", Metadata.ASCII_STRING_MARSHALLER), "INVALID_ARGUMENT");
				return Status.INVALID_ARGUMENT.withDescription(exception.getMessage()).asException(metadata);
			}
			return null;
		};
	}

}
