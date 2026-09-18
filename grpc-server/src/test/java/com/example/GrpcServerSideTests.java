package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.example.GrpcServerSideTests.TestConfig;
import com.example.proto.HelloReply;
import com.example.proto.HelloRequest;
import com.example.proto.SimpleGrpc.SimpleBlockingStub;

@TestPropertySource(properties = { "spring.grpc.client.channel.default.target=localhost:9090" })
@SpringJUnitConfig(TestConfig.class)
@AutoConfigureTestGrpcTransport
@ImportGrpcClients
class GrpcServerSideTests {

	@Autowired
	private SimpleBlockingStub stub;

	@Test
	void contextLoads() {
		HelloRequest request = HelloRequest.newBuilder().setName("Test").build();
		HelloReply reply = this.stub.sayHello(request);
		assertThat(reply.getMessage()).contains(("Test"));
	}

	@TestConfiguration
	@Import({ GrpcServerService.class })
	@EnableAutoConfiguration
	static class TestConfig {

	}

}
