package com.example;

import com.example.proto.HelloRequest;
import com.example.proto.HelloResponse;
import com.example.proto.HelloServiceGrpc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	HelloServiceGrpc.HelloServiceBlockingStub helloServiceStub;

	@Test
	void sayHello() throws Exception {
		when(this.helloServiceStub.sayHello(any(HelloRequest.class)))
			.thenReturn(HelloResponse.newBuilder().setReply("Hello John Doe!").build());

		this.mockMvc.perform(get("/").param("greeting", "John Doe"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.reply").value("Hello John Doe!"));
	}

}
