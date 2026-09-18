package com.example;

import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;

import io.grpc.Metadata;
import io.grpc.Status;

@SpringBootApplication
public class GrpcServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrpcServerApplication.class, args);
	}

	@Bean
	TomcatConnectorCustomizer http2OverheadWindowCustomizer() {
		return (connector) -> {
			for (UpgradeProtocol protocol : connector.findUpgradeProtocols()) {
				if (protocol instanceof Http2Protocol http2Protocol) {
					http2Protocol.setOverheadWindowUpdateThreshold(0);
				}
			}
		};
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
