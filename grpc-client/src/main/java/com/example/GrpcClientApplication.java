package com.example;

import com.example.proto.HelloServiceGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;

@SpringBootApplication(proxyBeanMethods = false)
@ImportGrpcClients(target = "hello", types = HelloServiceGrpc.HelloServiceBlockingStub.class)
public class GrpcClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcClientApplication.class, args);
    }

}
