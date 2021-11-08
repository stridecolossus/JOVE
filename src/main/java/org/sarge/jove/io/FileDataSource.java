package org.sarge.jove.io;

import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A <i>file data source</i> abstracts over the file-system by referring to resources by name.
 * @author Sarge
 */
public class FileDataSource implements DataSource {
	/**
	 * Helper - Creates a data-source with a root at the users home directory.
	 * @param path Optional application path
	 * @return New data-source
	 */
	public static FileDataSource home(Path path) {
		final Path home = Paths.get(System.getProperty("user.home"));
		if(path == null) {
			return new FileDataSource(home);
		}
		else {
			return new FileDataSource(home.resolve(path));
		}
	}

	private final Path root;

	/**
	 * Constructor.
	 * @param root Data-source root path
	 * @throws IllegalArgumentException if the root does not exist
	 */
	public FileDataSource(Path root) {
		if(!Files.exists(root)) throw new IllegalArgumentException("Data-source root does not exist: " + root);
		this.root = notNull(root);
	}

	/**
	 * Constructor.
	 * @param root Data-source root
	 * @throws IllegalArgumentException if the root does not exist
	 */
	public FileDataSource(String root) {
		this(Paths.get(root));
	}

	/**
	 * @return Root of this data-source
	 */
	public Path root() {
		return root;
	}

	@Override
	public InputStream input(String name) throws IOException {
		return Files.newInputStream(root.resolve(name));
	}

	@Override
	public OutputStream output(String name) throws IOException {
		return Files.newOutputStream(root.resolve(name));
	}

	@Override
	public String toString() {
		return root.toString();
	}
}
