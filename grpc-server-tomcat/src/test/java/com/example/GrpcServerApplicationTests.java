package com.example;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.test.annotation.DirtiesContext;

import com.example.proto.HelloReply;
import com.example.proto.HelloRequest;
import com.example.proto.SimpleGrpc;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "spring.grpc.client.channel.default.target=static://127.0.0.1:${local.server.port}")
@DirtiesContext
class GrpcServerApplicationTests {

	@Autowired
	SimpleGrpc.SimpleBlockingStub stub;

	@Test
	void serverResponds() {
		HelloReply response = this.stub.sayHello(HelloRequest.newBuilder().setName("Alien").build());
		assertThat(response.getMessage()).isEqualTo("Hello ==> Alien");
	}

	@Test
	void streamResponds() {
		Iterator<HelloReply> response = this.stub.streamHello(HelloRequest.newBuilder().setName("Alien").build());
		assertThat(response.next().getMessage()).isEqualTo("Hello(0) ==> Alien");
		while (response.hasNext()) {
			response.next();
		}
	}

	@TestConfiguration
	@ImportGrpcClients(types = SimpleGrpc.SimpleBlockingStub.class)
	static class ExtraConfiguration {

	}

}
