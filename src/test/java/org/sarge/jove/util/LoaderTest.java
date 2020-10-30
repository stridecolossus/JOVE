package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.sarge.jove.util.Loader.LoaderAdapter;

public class LoaderTest {
	@Test
	void adapter() {
		// Create a loader that reads a byte-array and outputs a string
		final var loader = new LoaderAdapter<byte[], String>() {
			@Override
			protected byte[] open(InputStream in) throws IOException {
				return in.readAllBytes();
			}

			@Override
			protected String create(byte[] chars) throws IOException {
				return new String(chars);
			}
		};

		// Load resource
		final String str = "whatever";
		final String result = loader.load(new ByteArrayInputStream(str.getBytes()));
		assertEquals(str, result);
	}
}
