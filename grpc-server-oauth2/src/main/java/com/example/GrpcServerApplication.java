package com.example;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import io.grpc.Metadata;
import io.grpc.Status;

@SpringBootApplication
@Import(AuthenticationConfiguration.class)
public class GrpcServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrpcServerApplication.class, args);
	}

	@Bean
	@GlobalServerInterceptor
	AuthenticationProcessInterceptor jwtSecurityFilterChain(GrpcSecurity grpc) throws Exception {
		return grpc
			.authorizeRequests((requests) -> requests.methods("Simple/StreamHello")
				.hasAuthority("SCOPE_profile")
				.methods("Simple/SayHello")
				.authenticated()
				.methods("grpc.*/*")
				.permitAll()
				.allRequests()
				.denyAll())
			.oauth2ResourceServer((resourceServer) -> resourceServer.jwt(withDefaults()))
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
