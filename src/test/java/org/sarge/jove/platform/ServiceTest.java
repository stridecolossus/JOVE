package org.sarge.jove.platform;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.Service.ErrorHandler;
import org.sarge.jove.platform.Service.Group;
import org.sarge.jove.platform.Service.ServiceException;

public class ServiceTest {
	private Service service;

	@BeforeEach
	public void before() {
		service = mock(Service.class);
	}

	@Test
	public void group() {
		final Group group = new Group(Set.of(service));
		assertArrayEquals(new Service[]{service}, group.services().toArray());
		group.close();
		verify(service).close();
	}

	@Test
	public void exceptionErrorHandler() {
		final var exception = assertThrows(ServiceException.class, () -> ErrorHandler.THROW.handle("error"));
		assertEquals("error", exception.getMessage());
	}

	@Test
	public void loggingErrorHandler() {
		ErrorHandler.LOGGER.handle("error");
	}
}
