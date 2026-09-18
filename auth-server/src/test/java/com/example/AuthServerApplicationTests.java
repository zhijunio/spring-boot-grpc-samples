package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class AuthServerApplicationTests {

	@LocalServerPort
	int port;

	@Test
	void clientCredentialsToken() {
		String body = RestClient.create()
			.post()
			.uri("http://127.0.0.1:" + this.port + "/oauth2/token")
			.headers((headers) -> headers.setBasicAuth("spring", "secret"))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body("grant_type=client_credentials")
			.retrieve()
			.body(String.class);
		assertThat(body).contains("access_token");
	}

}
