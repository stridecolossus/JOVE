package org.sarge.jove.platform.vulkan.generator;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * A <i>file printer</i> outputs a generated source file.
 * @author Sarge
 */
interface FilePrinter {
	/**
	 * Writes a generated source file.
	 * @param name		Filename
	 * @param source	Source code
	 */
	void print(String name, String source);

	/**
	 * Silent implementation.
	 */
	FilePrinter IGNORE = new FilePrinter() {
		@Override
		public void print(String name, String source) {
			// Ignored
		}
	};

	/**
	 * Creates a printer that outputs to the given writer.
	 * @param out Output
	 * @return Output writer
	 */
	static FilePrinter of(PrintWriter out) {
		return (_, source) -> {
			out.println(source);
			out.flush();
		};
	}

	/**
	 * Creates a printer that writes generated source files to the given directory.
	 * @param directory		Directory
	 * @param overwrite		Whether to overwrite existing files
	 * @return Directory printer
	 * @throws RuntimeException if {@link #overwrite} is {@code true} and the file already exists
	 */
	static FilePrinter of(Path directory, String extension, boolean overwrite) {
		// Validate
		if(!directory.toFile().exists()) {
			throw new IllegalArgumentException("Unknown directory " + directory);
		}

		// Init file options
		final Set<OpenOption> options = new HashSet<>();
		options.add(StandardOpenOption.WRITE);
		if(overwrite) {
			options.add(StandardOpenOption.CREATE);
			options.add(StandardOpenOption.TRUNCATE_EXISTING);
		}
		else {
			options.add(StandardOpenOption.CREATE_NEW);
		}

		return new FilePrinter() {
			@Override
			public void print(String name, String source) {
				final String filename = String.format("%s.%s", name, extension);
				final Path file = directory.resolve(filename);
				try {
					Files.writeString(file, source, options.toArray(OpenOption[]::new));
				}
				catch(IOException e) {
					throw new RuntimeException("Error writing file: " + filename, e);
				}
			}
		};
	}
}
