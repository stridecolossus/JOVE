package org.sarge.jove.model;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.model.Model.Header;
import org.sarge.jove.model.Vertex.Layout;
import org.sarge.jove.util.ResourceLoader;

/**
 * TODO
 * @author Sarge
 */
public class ModelLoader extends ResourceLoader.Adapter<DataInputStream, BufferedModel> {
	private static final int VERSION = 1;
	private static final String DELIMITER = "-";

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

		// Write model properties
		final Header header = model.header();
		out.writeUTF(header.primitive().name());
		out.writeBoolean(header.clockwise());

		// Write vertex layout
		final String layout = header.layout().components().stream().map(Enum::name).collect(joining(DELIMITER));
		out.writeUTF(layout);

		// Write VBO
		out.writeInt(model.count());
		write(model.vertexBuffer(), out);

		// Write index
		final var index = model.indexBuffer();
		if(index.isPresent()) {
			write(index.get(), out);
		}
		else {
			out.writeInt(0);
		}

		// Done
		out.flush();
	}

//	/**
//	 * Allocates a direct NIO buffer of the given length.
//	 * @param len Length
//	 * @return New direct buffer
//	 */
//	static ByteBuffer allocate(int len) {
//		return ByteBuffer.allocateDirect(len).order(NATIVE_ORDER);
//	}
//
//	/**
//	 * Allocates a direct NIO buffer that wraps the given array.
//	 * @param bytes Array
//	 * @return New NIO buffer
//	 */
//	static ByteBuffer allocate(byte[] bytes) {
//		final ByteBuffer bb = allocate(bytes.length);
//		bb.put(bytes);
//		bb.flip();
//		return bb;
//	}
//	// 	/**
//	 * Native byte order for NIO buffers.
//	 */
//	public static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

	/**
	 * Writes the given buffer.
	 */
	private static void write(Bufferable data, DataOutputStream out) throws IOException {
		final byte[] bytes = data.toByteArray();
		out.writeInt(bytes.length);
		out.write(bytes);
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
	 */
	@Override
	public BufferedModel load(DataInputStream in) throws IOException {
		// Load and verify file format version
		final int version = in.readInt();
		if(version > VERSION) {
			throw new UnsupportedOperationException(String.format("Unsupported version: version=%d supported=%d", version, VERSION));
		}

		// Load model properties
		final Primitive primitive = Primitive.valueOf(in.readUTF());
		final boolean clockwise = in.readBoolean();

		// Load vertex layout
		final var layout = Arrays.stream(in.readUTF().split(DELIMITER)).map(Vertex.Component::valueOf).collect(toList());

		// Load data
		final int count = in.readInt();
		final Bufferable vertices = loadBuffer(in);
		final Bufferable index = loadBuffer(in);

		// Create model
		return new BufferedModel(new Header(primitive, new Layout(layout), clockwise), count, vertices, index);
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

		// Convert to bufferable object
		return Bufferable.of(bytes);
	}
}
