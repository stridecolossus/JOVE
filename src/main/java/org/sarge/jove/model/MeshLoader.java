package org.sarge.jove.model;

import java.io.*;
import java.nio.*;
import java.util.*;

import org.sarge.jove.common.Layout;

/**
 * The <i>mesh loader</i> is used to persist and load a {@link BufferedMesh}.
 * @author Sarge
 */
public class MeshLoader {
	private static final int VERSION = 1;

	// TODO
	public Mesh load(DataInputStream in) throws IOException {
		// Verify file version
		if(in.readInt() > VERSION) {
			throw new IOException("Unsupported mesh version");
		}

		// Load model header
		final Primitive primitive = Primitive.valueOf(in.readUTF());
		final int count = in.readInt();
		final int num = in.readInt();

		// Load vertex layout
		final List<Layout> layouts = new ArrayList<>();
		for(int n = 0; n < num; ++n) {
			final int size = in.readInt();
			final var type = Layout.Type.valueOf(in.readUTF());
			final boolean signed = in.readBoolean();
			final int bytes = in.readInt();
			final var layout = new Layout(size, type, signed, bytes);
			layouts.add(layout);
		}

		// Load data
		final ByteBuffer vertices = buffer(in);
		final ByteBuffer index = buffer(in);

		// Create mesh
		return new AbstractMesh(primitive, layouts) {
			@Override
			public int count() {
				return count;
			}

			@Override
			public ByteBuffer vertices() {
				return vertices;
			}

			@Override
			public Optional<ByteBuffer> index() {
				return Optional.ofNullable(index);
			}
		};
	}

	/**
	 * Loads a buffer.
	 */
	private static ByteBuffer buffer(DataInputStream in) throws IOException {
		// Read buffer size
		final int len = in.readInt();

		// Ignore if empty buffer
		if(len == 0) {
			return null;
		}

		// Load bytes
		final byte[] bytes = new byte[len];
		in.readFully(bytes);

		// Convert to buffer
		final var buffer = ByteBuffer.allocateDirect(len).order(ByteOrder.nativeOrder());
		for(byte b : bytes) {
			buffer.put(b);
		}
		return buffer.rewind().asReadOnlyBuffer();
	}

	/**
	 * Writes a mesh to an output stream.
	 * @param mesh		Mesh
	 * @param out		Output stream
	 * @throws IOException if the mesh cannot be written
	 */
	public void save(Mesh mesh, DataOutputStream out) throws IOException {
		// Write file header
		out.writeInt(VERSION);

		// Write model header
		out.writeUTF(mesh.primitive().name());
		out.writeInt(mesh.count());

		// Write vertex layout
		final List<Layout> layouts = mesh.layout();
		out.writeInt(layouts.size());
		for(Layout layout : layouts) {
			out.writeInt(layout.count());
			out.writeUTF(layout.type().name());
			out.writeBoolean(layout.signed());
			out.writeInt(layout.bytes());
		}

		// Write vertices
		write(mesh.vertices(), out);

		// Write index
		final Optional<ByteBuffer> index = mesh.index();
		if(index.isEmpty()) {
			out.writeInt(0);
		}
		else {
			write(mesh.index().get(), out);
		}

		// TODO - do we need this?
		out.flush();
	}

	/**
	 * Writes a buffer.
	 * @param obj	Buffer to write
	 * @param out	Output stream
	 */
	private static void write(ByteBuffer buffer, DataOutput out) throws IOException {
		// Output length
		final int len = buffer.limit();
		out.writeInt(len);

		// Stop if empty buffer
		if(len == 0) {
			return;
		}

		// Write buffer
		if(buffer.hasArray()) {
			out.write(buffer.array());
		}
		else {
			buffer.rewind();
			for(int n = 0; n < len; ++n) {
				out.writeByte(buffer.get());
			}
		}
	}
}
