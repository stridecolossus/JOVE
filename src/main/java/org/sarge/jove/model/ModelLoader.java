package org.sarge.jove.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.BufferHelper;
import org.sarge.jove.io.DataResourceLoader;
import org.sarge.jove.model.Model.Header;

/**
 * The <i>model loader</i> persists a JOVE model.
 * @author Sarge
 */
public class ModelLoader implements DataResourceLoader<Model> {
	private static final int VERSION = 1;

	@Override
	public void save(Model model, DataOutputStream out) throws IOException {
		// Write file format version
		out.writeInt(VERSION);

		// Write model header
		final Header header = model.header();
		out.writeUTF(header.primitive().name());
		out.writeInt(header.count());

		// Write vertex layout
		final List<Layout> layout = header.layout().layouts();
		out.writeInt(layout.size());
		for(Layout c : layout) {
			out.writeUTF(c.components());
			out.writeInt(c.bytes());
			out.writeUTF(c.type().getName());
			out.writeBoolean(c.signed());
		}

		// Write VBO
		writeBuffer(model.vertices(), out);

		// Write index
		final var index = model.index();
		if(index.isPresent()) {
			writeBuffer(index.get(), out);
		}
		else {
			out.writeInt(0);
		}

		// Done
		out.flush();
	}

	/**
	 * Writes a buffer.
	 */
	private static void writeBuffer(Bufferable src, DataOutputStream out) throws IOException {
		final int len = src.length();
		final ByteBuffer bb = ByteBuffer.allocate(len).order(BufferHelper.ORDER);
		src.buffer(bb);
		out.writeInt(len);
		out.write(bb.array());
	}

	@Override
	public BufferedModel load(DataInputStream in) throws IOException {
		// Load and verify file format version
		final int version = in.readInt();
		if(version > VERSION) {
			throw new UnsupportedOperationException(String.format("Unsupported version: version=%d supported=%d", version, VERSION));
		}

		// Load model header
		final Primitive primitive = Primitive.valueOf(in.readUTF());
		final int count = in.readInt();

		// Load vertex layout
		final int num = in.readInt();
		final List<Layout> layout = new ArrayList<>();
		for(int n = 0; n < num; ++n) {
			// Load layout
			final String components = in.readUTF();
			final int bytes = in.readInt();
			final String name = in.readUTF();
			final boolean signed = in.readBoolean();

			// Lookup layout component type
			final Class<?> type;
			try {
				type = Class.forName(name);
			}
			catch(ClassNotFoundException e) {
				throw new IOException("Unknown layout component type: " + name, e);
			}

			// Add layout
			layout.add(new Layout(components, type, bytes, signed));
		}

		// Load data
		final Bufferable vertices = loadBuffer(in);
		final Bufferable index = loadBuffer(in);

		// Create model
		final Header header = new Header(new CompoundLayout(layout), primitive, count);
		return new BufferedModel(header, vertices, Optional.ofNullable(index));
	}

	/**
	 * Loads a buffer.
	 * @param in Input stream
	 * @return New buffer or {@code null} if empty
	 * @throws IOException if the buffer cannot be loaded
	 */
	private static Bufferable loadBuffer(DataInputStream in) throws IOException {
		// Read buffer size
		final int len = in.readInt();
		if(len == 0) {
			return null;
		}

		// Load bytes
		final byte[] bytes = new byte[len];
		final int actual = in.read(bytes);
		if(actual != len) throw new IOException(String.format("Error loading buffer: expected=%d actual=%d", len, actual));

		// Convert to buffer
		return Bufferable.of(bytes);
	}
}
