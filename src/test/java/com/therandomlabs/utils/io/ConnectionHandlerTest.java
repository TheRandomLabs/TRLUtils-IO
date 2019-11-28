package com.therandomlabs.utils.io;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionHandlerTest {
	private ConnectionHandler connectionHandler;

	@BeforeEach
	public void init() {
		connectionHandler = new ConnectionHandler();
	}

	@Test
	public void shouldBeAbleToReadGoogle() throws IOException {
		assertThat(connectionHandler.read(URLUtils.of("https://google.com/"))).isNotEmpty();
	}
}
