package org.sarge.jove.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A <i>classpath data source</i> is used to load resources from the classpath.
 * @author Sarge
 */
public class ClasspathDataSource implements DataSource {
	private static final String SLASH = "/";

	@Override
	public InputStream input(String name) throws IOException {
		final String path = prepend(name);
		final InputStream in = DataSource.class.getResourceAsStream(path);
		if(in == null) throw new FileNotFoundException("Cannot find classpath resource: " + name);
		return in;
	}

	@Override
	public OutputStream output(String name) throws IOException {
		throw new UnsupportedOperationException();
	}

	private static String prepend(String path) {
		if(path.startsWith(SLASH)) {
			return path;
		}
		else {
			return SLASH + path;
		}
	}
}
