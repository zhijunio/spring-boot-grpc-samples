package com.example;

import com.example.proto.HelloRequest;
import com.example.proto.HelloResponse;
import com.example.proto.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class HelloService extends HelloServiceGrpc.HelloServiceImplBase {

	@Override
	public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		HelloResponse response = HelloResponse.newBuilder()
			.setReply("Hello %s!".formatted(request.getGreeting()))
			.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void lotsOfReplies(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
		for (int i = 0; i < 10; i++) {
			responseObserver.onNext(HelloResponse.newBuilder()
				.setReply("[%05d] Hello %s!".formatted(i, request.getGreeting()))
				.build());
		}
		responseObserver.onCompleted();
	}

}