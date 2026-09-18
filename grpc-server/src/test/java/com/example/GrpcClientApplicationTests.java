package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.grpc.client.BlockingStubFactory;
import org.springframework.grpc.client.BlockingV2StubFactory;
import org.springframework.grpc.client.FutureStubFactory;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.grpc.client.SimpleStubFactory;

import com.example.proto.SimpleGrpc;

import io.grpc.stub.AbstractStub;

class GrpcClientApplicationTests {

	@Nested
	@SpringBootTest
	@AutoConfigureTestGrpcTransport
	class NoAutowiredClients {

		@Autowired
		private ApplicationContext context;

		@Test
		void noStubIsCreated() {
			assertThat(this.context.containsBeanDefinition("simpleBlockingStub")).isFalse();
			assertThat(this.context.containsBeanDefinition("simpleStub")).isFalse();
			assertThat(this.context.containsBeanDefinition("simpleFutureStub")).isFalse();
			assertThat(this.context.getBeanNamesForType(AbstractStub.class)).isEmpty();
		}

	}

	@Nested
	@SpringBootTest(properties = "spring.grpc.client.channel.default.target=0.0.0.0:9090")
	@AutoConfigureTestGrpcTransport
	class SpecificAutowiredClients {

		@Autowired
		private ApplicationContext context;

		@Test
		void stubOfCorrectTypeIsCreated() {
			assertThat(this.context.containsBeanDefinition("simpleFutureStub")).isTrue();
			assertThat(this.context.getBean(SimpleGrpc.SimpleFutureStub.class)).isNotNull();
			assertThat(this.context.containsBeanDefinition("simpleStub")).isFalse();
			assertThat(this.context.containsBeanDefinition("simpleBlockingStub")).isFalse();
			assertThat(this.context.getBeanNamesForType(AbstractStub.class)).hasSize(1);
		}

		@TestConfiguration
		@ImportGrpcClients(basePackageClasses = SimpleGrpc.class, factory = FutureStubFactory.class)
		static class TestConfig {

		}

	}

	@Nested
	@SpringBootTest(properties = "spring.grpc.client.channel.default.target=0.0.0.0:9090")
	@AutoConfigureTestGrpcTransport
	class BlockingV2AutowiredClients {

		@Autowired
		private ApplicationContext context;

		@Test
		void stubOfCorrectTypeIsCreated() {
			assertThat(this.context.containsBeanDefinition("simpleBlockingV2Stub")).isTrue();
			assertThat(this.context.getBean(SimpleGrpc.SimpleBlockingV2Stub.class)).isNotNull();
			assertThat(this.context.containsBeanDefinition("simpleStub")).isFalse();
			assertThat(this.context.containsBeanDefinition("simpleBlockingStub")).isFalse();
			assertThat(this.context.getBeanNamesForType(AbstractStub.class)).hasSize(1);
		}

		@TestConfiguration
		@ImportGrpcClients(basePackageClasses = SimpleGrpc.class, factory = BlockingV2StubFactory.class)
		static class TestConfig {

		}

	}

	@Nested
	@SpringBootTest
	@AutoConfigureTestGrpcTransport
	class ExplicitImportClientsWithNoFactory {

		@Autowired
		private ApplicationContext context;

		@Test
		void stubOfCorrectTypeIsCreated() {
			assertThat(this.context.containsBeanDefinition("simpleBlockingStub")).isTrue();
			assertThat(this.context.getBean(SimpleGrpc.SimpleBlockingStub.class)).isNotNull();
			assertThat(this.context.containsBeanDefinition("simpleStub")).isFalse();
			assertThat(this.context.containsBeanDefinition("simpleFutureStub")).isFalse();
			assertThat(this.context.getBeanNamesForType(AbstractStub.class)).hasSize(1);
		}

		@TestConfiguration
		@ImportGrpcClients
		static class TestConfig {

		}

	}

	@Nested
	@SpringBootTest(properties = "spring.grpc.client.channel.default.target=0.0.0.0:9090")
	@AutoConfigureTestGrpcTransport
	class AllStubAutowiredClients {

		@Autowired
		private ApplicationContext context;

		@Autowired
		private SimpleGrpc.SimpleBlockingStub simpleBlockingStub;

		@Autowired
		private SimpleGrpc.SimpleBlockingV2Stub simpleBlockingV2Stub;

		@Autowired
		private SimpleGrpc.SimpleFutureStub simpleFutureStub;

		@Autowired
		private SimpleGrpc.SimpleStub simpleStub;

		@Test
		void stubsCreatedWithRightName() {
			assertNotNull(this.context.getBeansOfType(SimpleGrpc.SimpleBlockingStub.class).get("simpleBlockingStub"));
			assertNotNull(this.context.getBeansOfType(SimpleGrpc.SimpleBlockingV2Stub.class).get("simpleBlockingV2Stub"));
			assertNotNull(this.context.getBeansOfType(SimpleGrpc.SimpleFutureStub.class).get("simpleFutureStub"));
			assertNotNull(this.context.getBeansOfType(SimpleGrpc.SimpleStub.class).get("simpleStub"));
			assertThat(this.context.getBeanNamesForType(AbstractStub.class)).hasSize(4);

			assertNotNull(this.simpleBlockingStub);
			assertNotNull(this.simpleBlockingV2Stub);
			assertNotNull(this.simpleFutureStub);
			assertNotNull(this.simpleStub);
		}

		@TestConfiguration
		@ImportGrpcClients.Container(value = {
				@ImportGrpcClients(basePackageClasses = SimpleGrpc.class, factory = BlockingStubFactory.class),
				@ImportGrpcClients(basePackageClasses = SimpleGrpc.class, factory = BlockingV2StubFactory.class),
				@ImportGrpcClients(basePackageClasses = SimpleGrpc.class, factory = FutureStubFactory.class),
				@ImportGrpcClients(basePackageClasses = SimpleGrpc.class, factory = SimpleStubFactory.class), })
		static class TestConfig {

		}

	}

}
