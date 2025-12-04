package org.sarge.jove.platform.vulkan.generator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class FilePrinterTest {
	@Test
	void writer() {
		final var string = new StringWriter();
		final var writer = new PrintWriter(string);
		final var printer = FilePrinter.of(writer);
		printer.print("name", "source");
		assertEquals("source", string.toString().trim());
	}

	@Nested
	class FileTest {
		@TempDir
		private Path dir;

		private Path file;

		@BeforeEach
		void before() {
			file = dir.resolve("name.java");
		}

		@Test
		void write() throws Exception {
			final var printer = FilePrinter.of(dir, "java", true);
			printer.print("name", "source");
			assertEquals(true, file.toFile().exists());
			assertEquals("source", Files.readString(file));
		}

		@Test
		void overwrite() throws Exception {
			final var printer = FilePrinter.of(dir, "java", false);
			file.toFile().createNewFile();
			assertThrows(RuntimeException.class, () -> printer.print("name", "source"));
		}
	}
}
