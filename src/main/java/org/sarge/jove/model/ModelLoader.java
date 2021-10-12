package org.sarge.jove.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.sarge.jove.common.Layout;
import org.sarge.jove.model.Model.Header;
import org.sarge.jove.platform.vulkan.util.VulkanHelper;
import org.sarge.jove.util.ResourceLoader;

/**
 * The <i>model loader</i> persists a vertex model.
 * @author Sarge
 */
public class ModelLoader extends ResourceLoader.Adapter<DataInputStream, BufferedModel> {
	private static final int VERSION = 1;

	/**
	 * Writes the given model to an output stream.
	 * @param model		Model
	 * @param out		Output stream
	 * @throws IOException if the model cannot be written
	 */
	@SuppressWarnings("static-method")
	public void write(Model model, OutputStream out) throws IOException {
		write(model, new DataOutputStream(out));
	}

	/**
	 * Writes the given model.
	 */
	private static void write(Model model, DataOutputStream out) throws IOException {
		// Write file format version
		out.writeInt(VERSION);

		// Write model header
		final Header header = model.header();
		out.writeUTF(header.primitive().name());
		out.writeInt(header.count());
		out.writeBoolean(header.clockwise());

		// Write vertex layout
		final var layout = header.layout();
		out.writeInt(layout.size());
		for(Layout c : header.layout()) {
			out.writeInt(c.size());
			out.writeInt(c.bytes());
			out.writeUTF(c.type().getName());
		}

		// Write VBO
		write(model.vertexBuffer(), out);

		// Write index
		final var index = model.indexBuffer();
		if(index.isPresent()) {
			write(model.indexBuffer().get(), out);
		}
		else {
			out.writeInt(0);
		}

		// Done
		out.flush();
	}

	private static void write(ByteBuffer src, DataOutputStream out) throws IOException {
		// Convert to array
		final byte[] array;
		if(src.isDirect()) {
			final int len = src.rewind().remaining();
			array = new byte[len];
			src.get(array);
		}
		else {
			array = src.array();
		}

		// Write length and data
		out.writeInt(array.length);
		out.write(array);
	}

	@Override
	protected DataInputStream map(InputStream in) throws IOException {
		return new DataInputStream(in);
	}

	/**
	 * Loads a buffered model.
	 * @param in Input stream
	 * @return New model
	 * @throws IOException if the model cannot be loaded
	 * @throws UnsupportedOperationException if the file version is unsupported by this loader
	 */
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
		final boolean clockwise = in.readBoolean();

		// Load vertex layout
		final int num = in.readInt();
		final List<Layout> layout = new ArrayList<>();
		for(int n = 0; n < num; ++n) {
			final int size = in.readInt();
			final int bytes = in.readInt();
			final String name = in.readUTF();
			final Class<?> type;
			try {
				type = Class.forName(name);
			}
			catch(ClassNotFoundException e) {
				throw new IOException("Unknown layout component type: " + name, e);
			}
			layout.add(new Layout(size, type, bytes, true));
		}

		// Load data
		final ByteBuffer vertices = loadBuffer(in);
		final ByteBuffer index = loadBuffer(in);

		// Create model
		return new BufferedModel(new Header(layout, primitive, count, clockwise), vertices, Optional.ofNullable(index));
	}

	/**
	 * Loads a buffer.
	 * @param in Input stream
	 * @return New buffer or {@code null} if empty
	 * @throws IOException if the buffer cannot be loaded
	 */
	private static ByteBuffer loadBuffer(DataInputStream in) throws IOException {
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
		return VulkanHelper.buffer(bytes).rewind();
	}
}
