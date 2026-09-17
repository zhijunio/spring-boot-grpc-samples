package com.example;

import com.example.proto.HelloRequest;
import com.example.proto.HelloServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.ImportGrpcClients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@AutoConfigureTestGrpcTransport
@ImportGrpcClients(types = HelloServiceGrpc.HelloServiceBlockingStub.class)
class HelloServiceUnauthenticatedTest {

	@Autowired
	HelloServiceGrpc.HelloServiceBlockingStub stub;

	@Test
	void sayHelloRejectedWithoutCredentials() {
		assertThatExceptionOfType(StatusRuntimeException.class)
			.isThrownBy(() -> this.stub.sayHello(HelloRequest.newBuilder().setGreeting("John Doe").build()))
			.satisfies((ex) -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.UNAUTHENTICATED));
	}

}
