package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class LazySupplierTest {
	private Supplier<Object> lazy;
	private Supplier<Object> supplier;
	private Object result;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		supplier = mock(Supplier.class);
		result = new Object();
		when(supplier.get()).thenReturn(result);
		lazy = new LazySupplier<>(supplier);
	}

	@Test
	void get() {
		assertEquals(result, lazy.get());
	}

	@Test
	void getMultiple() {
		lazy.get();
		assertEquals(result, lazy.get());
		verify(supplier, times(1)).get();
	}

	@Test
	@Timeout(1000)
	void getConcurrent() throws InterruptedException {
		final AtomicInteger count = new AtomicInteger();
		final Callable<Void> task = () -> {
			assertEquals(result, lazy.get());
			count.incrementAndGet();
			return null;
		};
		Executors.newFixedThreadPool(3).invokeAll(Collections.nCopies(3, task));
		verify(supplier, times(1)).get();
		assertEquals(3, count.get());
	}
}
