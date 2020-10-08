package org.sarge.jove.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DataSourceTest {
	@Nested
	class DirectoryTests {
		@Test
		void directory() throws IOException {
			final var src = DataSource.of(new File("./src/test/resources"));
			assertNotNull(src);
			try(final InputStream in = src.apply("statue.jpg")) {
				assertNotNull(in);
			}
		}

		@Test
		void unknownDirectory() {
			assertThrows(IllegalArgumentException.class, () -> DataSource.of(new File("cobblers")));
		}
	}
}
