package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

import com.example.proto.HelloReply;
import com.example.proto.HelloRequest;
import com.example.proto.SimpleGrpc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrpcClientApplicationTests {

	@Test
	void runnerCallsSayHello() throws Exception {
		SimpleGrpc.SimpleBlockingStub stub = mock(SimpleGrpc.SimpleBlockingStub.class);
		HelloRequest request = HelloRequest.newBuilder().setName("Alien").build();
		when(stub.sayHello(request)).thenReturn(HelloReply.newBuilder().setMessage("Hello ==> Alien").build());

		CommandLineRunner runner = new GrpcClientApplication().runner(stub);
		runner.run();

		verify(stub).sayHello(request);
	}

}
