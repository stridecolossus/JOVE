package org.sarge.jove.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes generated source code.
 * @author Sarge
 */
public class SourceWriter {
	private final Path dir;

	private boolean replace = true;
	private int count;

	/**
	 * Constructor.
	 * @param dir Output directory
	 * @throws IllegalArgumentException if the given path is not a directory
	 */
	public SourceWriter(Path dir) {
		if(!dir.toFile().isDirectory()) throw new IllegalArgumentException("Not a directory: " + dir);
		this.dir = dir;
	}

	/**
	 * Sets whether to replace existing files.
	 * @param replace Whether to replace files or ignore if already present
	 */
	public void replace(boolean replace) {
		this.replace = replace;
	}

	/**
	 * @return Number of files written
	 */
	public int count() {
		return count;
	}

	// TODO - some sort of Path::resolve() type mechanism?

	/**
	 * Writes source code.
	 * @param name			Class name
	 * @param source		Source code
	 * @throws IOException if the file cannot be written
	 */
	public void write(String name, String source) throws IOException {
		final Path path = dir.resolve(name + ".java");
		if(path.toFile().canWrite()) {
			Files.writeString(path, source);
			++count;
		}
		// TODO
		else {
			System.out.println("**** READONLY " + path);
		}
	}
}
