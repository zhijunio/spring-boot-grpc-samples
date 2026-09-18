package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.annotation.DirtiesContext;

import com.example.proto.HelloReply;
import com.example.proto.HelloRequest;
import com.example.proto.SimpleGrpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(properties = { "spring.grpc.server.port=0",
		"spring.grpc.client.channel.default.target=static://127.0.0.1:${local.grpc.server.port}",
		"spring.grpc.client.channel.stub.target=static://127.0.0.1:${local.grpc.server.port}",
		"spring.grpc.client.channel.secure.target=static://127.0.0.1:${local.grpc.server.port}",
		"spring.main.allow-bean-definition-overriding=true" })
@DirtiesContext
class OpaqueTokenServerApplicationTests {

	@Autowired
	@Qualifier("simpleBlockingStub")
	SimpleGrpc.SimpleBlockingStub stub;

	@Autowired
	@Qualifier("secureSimpleBlockingStub")
	SimpleGrpc.SimpleBlockingStub secure;

	@Test
	void unauthenticated() {
		assertThatExceptionOfType(StatusRuntimeException.class)
			.isThrownBy(() -> this.stub.sayHello(HelloRequest.newBuilder().setName("Alien").build()))
			.extracting(StatusRuntimeException::getStatus)
			.extracting(Status::getCode)
			.isEqualTo(Status.Code.UNAUTHENTICATED);
	}

	@Test
	void authenticatedByIntrospection() {
		HelloReply response = this.secure.sayHello(HelloRequest.newBuilder().setName("Alien").build());
		assertThat(response.getMessage()).isEqualTo("Hello ==> Alien");
	}

	@Test
	void unauthorizedWhenScopeMissing() {
		assertThatExceptionOfType(StatusRuntimeException.class)
			.isThrownBy(() -> this.secure.streamHello(HelloRequest.newBuilder().setName("Alien").build()).next())
			.extracting(StatusRuntimeException::getStatus)
			.extracting(Status::getCode)
			.isEqualTo(Status.Code.PERMISSION_DENIED);
	}

	@TestConfiguration(proxyBeanMethods = false)
	@EnableDynamicProperty
	@ImportGrpcClients(target = "stub", types = { SimpleGrpc.SimpleBlockingStub.class })
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
		@GlobalServerInterceptor
		AuthenticationProcessInterceptor jwtSecurityFilterChain(GrpcSecurity grpc,
				@Value("${spring.security.oauth2.client.provider.spring.issuer-uri}") String issuerUri)
				throws Exception {
			return grpc
				.authorizeRequests((requests) -> requests.methods("Simple/StreamHello")
					.hasAuthority("SCOPE_profile")
					.methods("Simple/SayHello")
					.authenticated()
					.methods("grpc.*/*")
					.permitAll()
					.allRequests()
					.denyAll())
				.oauth2ResourceServer((resourceServer) -> resourceServer
					.opaqueToken((opaqueToken) -> opaqueToken.introspectionUri(issuerUri + "/oauth2/introspect")
						.introspectionClientCredentials("spring", "secret")))
				.build();
		}

		@Bean
		GrpcChannelBuilderCustomizer<?> stubs(ObjectProvider<ClientRegistrationRepository> context) {
			return GrpcChannelBuilderCustomizer.matching("secure",
					(builder) -> builder.intercept(new BearerTokenAuthenticationInterceptor(
							new ClientCredentialsTokenSupplier(context.getObject(), () -> "spring"))));
		}

	}

}
