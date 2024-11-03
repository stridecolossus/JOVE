package org.sarge.jove.io;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A <i>zip data source</i> provides read access to a ZIP archive.
 * @author Sarge
 */
public class ZipDataSource implements DataSource {
	private final ZipFile file;

	/**
	 * Constructor.
	 * @param file ZIP archive
	 */
	public ZipDataSource(ZipFile file) {
		this.file = requireNonNull(file);
	}

	@Override
	public InputStream input(String name) throws IOException {
		final ZipEntry entry = file.getEntry(name);
		return file.getInputStream(entry);
	}

	@Override
	public OutputStream output(String name) throws IOException {
		throw new UnsupportedOperationException();
	}
}

// https://www.baeldung.com/java-compress-and-uncompress
// https://docs.oracle.com/javase/8/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
// FileSystems.newFileSystem(URI of ZIP file);
