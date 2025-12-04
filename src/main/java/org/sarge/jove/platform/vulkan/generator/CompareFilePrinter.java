package org.sarge.jove.platform.vulkan.generator;

import java.io.IOException;
import java.nio.file.*;

class CompareFilePrinter implements FilePrinter {
	private final Path directory = Paths.get("./src/generated/java/org/sarge/jove/platform/vulkan");

	@Override
	public void print(String name, String source) {
		final String filename = String.format("%s.%s", name, "java");
		final Path file = directory.resolve(filename);

		final String current;
		try {
			current = Files.readString(file);
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}

		if(!source.equals(current)) {
			System.out.println("generated:");
			System.out.println(source);
			System.out.println("---------------------------");
			System.out.println("current:");
			System.out.println(current);
			System.out.println("---------------------------");
		}
	}
}
