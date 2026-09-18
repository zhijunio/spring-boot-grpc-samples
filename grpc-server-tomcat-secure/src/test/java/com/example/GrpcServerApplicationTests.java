package com.example;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.grpc.client.interceptor.security.BasicAuthenticationInterceptor;
import org.springframework.test.annotation.DirtiesContext;

import com.example.proto.HelloReply;
import com.example.proto.HelloRequest;
import com.example.proto.SimpleGrpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "spring.grpc.client.channel.default.target=static://127.0.0.1:${local.server.port}")
class GrpcServerApplicationTests {

	@Autowired
	@Qualifier("simpleBlockingStub")
	SimpleGrpc.SimpleBlockingStub stub;

	@Autowired
	@Qualifier("basic")
	SimpleGrpc.SimpleBlockingStub basic;

	@Test
	@DirtiesContext
	void unauthenticated() {
		assertThatExceptionOfType(StatusRuntimeException.class)
			.isThrownBy(() -> this.stub.sayHello(HelloRequest.newBuilder().setName("Alien").build()))
			.extracting(StatusRuntimeException::getStatus)
			.extracting(Status::getCode)
			.isEqualTo(Status.Code.UNAUTHENTICATED);
	}

	@Test
	@DirtiesContext
	void authenticated() {
		HelloReply response = this.basic.sayHello(HelloRequest.newBuilder().setName("Alien").build());
		assertThat(response.getMessage()).isEqualTo("Hello ==> Alien");
	}

	@TestConfiguration
	@ImportGrpcClients(types = SimpleGrpc.SimpleBlockingStub.class)
	static class ExtraConfiguration {

		@Bean
		@Lazy
		SimpleGrpc.SimpleBlockingStub basic(GrpcChannelFactory channels, @LocalServerPort int port) {
			return SimpleGrpc.newBlockingStub(channels.createChannel("default", ChannelBuilderOptions.defaults()
				.withInterceptors(List.of(new BasicAuthenticationInterceptor("user", "user")))));
		}

	}

}
