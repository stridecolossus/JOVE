package org.sarge.jove.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import org.sarge.jove.platform.Service.ServiceException;

/**
 * A <i>data source</i> is used to access a named resource.
 * @author Sarge
 */
public interface DataSource extends Function<String, InputStream> {
	/**
	 * Creates a data-source for the given file-system directory.
	 * @param dir Directory
	 * @return File-system data-source
	 */
	static DataSource file(File dir) {
		if(!dir.exists()) throw new IllegalArgumentException("Data-source directory does not exist: " + dir);
		final Path path = dir.toPath();
		return filename -> {
			try {
				return Files.newInputStream(path.resolve(filename));
			}
			catch(IOException e) {
				throw new ServiceException("Error opening file: " + filename, e);
			}
		};
	}
}
