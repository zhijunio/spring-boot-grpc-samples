package com.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.experimental.boot.server.exec.CommonsExecWebServerFactoryBean;
import org.springframework.experimental.boot.server.exec.MavenClasspathEntry;
import org.springframework.experimental.boot.test.context.EnableDynamicProperty;
import org.springframework.experimental.boot.test.context.OAuth2ClientProviderIssuerUri;
import org.springframework.grpc.client.GrpcChannelBuilderCustomizer;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.grpc.client.interceptor.security.BearerTokenAuthenticationInterceptor;
import org.springframework.grpc.client.interceptor.security.ClientCredentialsTokenSupplier;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.annotation.DirtiesContext;

import com.example.proto.HelloReply;
import com.example.proto.HelloRequest;
import com.example.proto.SimpleGrpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.reflection.v1.ServerReflectionGrpc;
import io.grpc.reflection.v1.ServerReflectionRequest;
import io.grpc.reflection.v1.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(properties = { "spring.grpc.server.port=0",
		"spring.grpc.client.channel.default.target=static://127.0.0.1:${local.grpc.server.port}",
		"spring.grpc.client.channel.stub.target=static://127.0.0.1:${local.grpc.server.port}",
		"spring.grpc.client.channel.secure.target=static://127.0.0.1:${local.grpc.server.port}" })
@DirtiesContext
class GrpcServerApplicationTests {

	@Autowired
	@Qualifier("simpleBlockingStub")
	SimpleGrpc.SimpleBlockingStub stub;

	@Autowired
	ServerReflectionGrpc.ServerReflectionStub reflect;

	@Autowired
	@Qualifier("secureSimpleBlockingStub")
	SimpleGrpc.SimpleBlockingStub basic;

	@Test
	void unauthenticated() {
		assertThatExceptionOfType(StatusRuntimeException.class)
			.isThrownBy(() -> this.stub.sayHello(HelloRequest.newBuilder().setName("Alien").build()))
			.extracting(StatusRuntimeException::getStatus)
			.extracting(Status::getCode)
			.isEqualTo(Status.Code.UNAUTHENTICATED);
	}

	@Test
	void anonymous() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<ServerReflectionResponse> response = new AtomicReference<>();
		AtomicReference<Throwable> error = new AtomicReference<>();
		StreamObserver<ServerReflectionResponse> responses = new StreamObserver<>() {
			@Override
			public void onNext(ServerReflectionResponse value) {
				response.set(value);
			}

			@Override
			public void onError(Throwable t) {
				error.set(t);
				latch.countDown();
			}

			@Override
			public void onCompleted() {
				latch.countDown();
			}
		};
		StreamObserver<ServerReflectionRequest> request = this.reflect.serverReflectionInfo(responses);
		request.onNext(ServerReflectionRequest.newBuilder().setListServices("").build());
		request.onCompleted();
		assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(error.get()).isNull();
		assertThat(response.get()).isNotNull();
	}

	@Test
	void unauthauthorized() {
		assertThatExceptionOfType(StatusRuntimeException.class)
			.isThrownBy(() -> this.basic.streamHello(HelloRequest.newBuilder().setName("Alien").build()).next())
			.extracting(StatusRuntimeException::getStatus)
			.extracting(Status::getCode)
			.isEqualTo(Status.Code.PERMISSION_DENIED);
	}

	@Test
	void authenticated() {
		HelloReply response = this.basic.sayHello(HelloRequest.newBuilder().setName("Alien").build());
		assertThat(response.getMessage()).isEqualTo("Hello ==> Alien");
	}

	@TestConfiguration(proxyBeanMethods = false)
	@EnableDynamicProperty
	@ImportGrpcClients(target = "stub",
			types = { SimpleGrpc.SimpleBlockingStub.class, ServerReflectionGrpc.ServerReflectionStub.class })
	@ImportGrpcClients(target = "secure", prefix = "secure", types = { SimpleGrpc.SimpleBlockingStub.class })
	static class ExtraConfiguration {

		@Bean
		@OAuth2ClientProviderIssuerUri
		static CommonsExecWebServerFactoryBean authServer() {
			return CommonsExecWebServerFactoryBean.builder()
				.useGenericSpringBootMain()
				.classpath((classpath) -> classpath
					.entries(MavenClasspathEntry.springBootStarter("oauth2-authorization-server")));
		}

		@Bean
		GrpcChannelBuilderCustomizer<?> stubs(ObjectProvider<ClientRegistrationRepository> context) {
			return GrpcChannelBuilderCustomizer.matching("secure",
					(builder) -> builder.intercept(new BearerTokenAuthenticationInterceptor(
							new ClientCredentialsTokenSupplier(context.getObject(), () -> "spring"))));
		}

	}

}
