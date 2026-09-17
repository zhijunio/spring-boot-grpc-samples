package com.example;

import com.example.proto.HelloRequest;
import com.example.proto.HelloResponse;
import com.example.proto.HelloServiceGrpc;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.ImportGrpcClients;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestGrpcTransport
@ImportGrpcClients(types = HelloServiceGrpc.HelloServiceBlockingStub.class)
class HelloServiceTest {

	@Autowired
	HelloServiceGrpc.HelloServiceBlockingStub stub;

	@Test
	void sayHello() {
		HelloResponse response = this.stub.sayHello(HelloRequest.newBuilder().setGreeting("John Doe").build());
		assertThat(response.getReply()).isEqualTo("Hello John Doe!");
	}

	@Test
	void lotsOfReplies() {
		List<String> replies = new ArrayList<>();
		this.stub.lotsOfReplies(HelloRequest.newBuilder().setGreeting("John Doe").build())
			.forEachRemaining(r -> replies.add(r.getReply()));
		assertThat(replies).containsExactly("[00000] Hello John Doe!", "[00001] Hello John Doe!",
				"[00002] Hello John Doe!", "[00003] Hello John Doe!", "[00004] Hello John Doe!",
				"[00005] Hello John Doe!", "[00006] Hello John Doe!", "[00007] Hello John Doe!",
				"[00008] Hello John Doe!", "[00009] Hello John Doe!");
	}

}
