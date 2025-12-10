package org.sarge.jove.model;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;

import org.sarge.jove.common.Layout;
import org.sarge.jove.model.Mesh.*;

/**
 * The <i>mesh loader</i> is used to persist and load a {@link BufferedMesh}.
 * @author Sarge
 */
public class MeshLoader {
	private static final int VERSION = 1;

	public Mesh load(Path path) throws IOException {
		try(var in = new DataInputStream(Files.newInputStream(path))) {
			return load(in);
		}
	}

	/**
	 * Loads a persisted mesh from the given input stream.
	 * @param in Input stream
	 * @return Mesh
	 * @throws IOException if the mesh cannot be loaded
	 */
	public Mesh load(DataInputStream in) throws IOException {
		// Verify file version
		if(in.readInt() > VERSION) {
			throw new IOException("Unsupported mesh version");
		}

		// Load model header
		final Primitive primitive = Primitive.valueOf(in.readUTF());
		final int count = in.readInt();

		// Load vertex layout
		final List<Layout> layouts = new ArrayList<>();
		final int num = in.readInt();
		for(int n = 0; n < num; ++n) {
			final int size = in.readInt();
			final var type = Layout.Type.valueOf(in.readUTF());
			final boolean signed = in.readBoolean();
			final int bytes = in.readInt();
			final var layout = new Layout(size, type, signed, bytes);
			layouts.add(layout);
		}

		// Load mesh data
		final MeshData vertices = data(in);
		final MeshData index = data(in);

		// Create mesh
		return new Mesh() {
			@Override
			public Primitive primitive() {
				return primitive;
			}

			@Override
			public List<Layout> layout() {
				return List.copyOf(layouts);
			}

			@Override
			public int count() {
				return count;
			}

			@Override
			public MeshData vertices() {
				return vertices;
			}

			@Override
			public Optional<Index> index() {
				if(index.length() == 0) {
					return Optional.empty();
				}
				else {
					return Optional.of(new IndexWrapper(index));
				}
			}
		};
	}

	/**
	 * TODO - could be integrated more with DefaultIndex? could then allow smaller element sizes?
	 * and/or persist smaller values? and write data size into file?
	 */
	private record IndexWrapper(MeshData index) implements Mesh.Index {
		@Override
		public int length() {
			return index.length();
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			index.buffer(buffer);
		}

		@Override
		public int minimumElementBytes() {
			return Integer.BYTES;
		}

		@Override
		public Index index(int bytes) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Loads a data buffer for a mesh.
	 */
	private static MeshData data(DataInputStream in) throws IOException {
		// Init data buffer
		final var data = new MeshData() {
			private final int length = in.readInt();
			private final byte[] bytes = new byte[length];

			@Override
			public int length() {
				return length;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				if(buffer.isDirect()) {
					for(int n = 0; n < length; ++n) {
						buffer.put(bytes[n]);
					}
				}
				else {
					buffer.put(bytes);
				}
			}
		};

		// Load data
		if(data.length > 0) {
			in.readFully(data.bytes);
		}

		return data;
	}

	public void write(Mesh mesh, Path path) throws IOException {
		try(var out = new DataOutputStream(Files.newOutputStream(path))) {
			write(mesh, out);
		}
	}

	/**
	 * Writes a mesh to the given output stream.
	 * @param mesh		Mesh
	 * @param out		Output stream
	 * @throws IOException if the mesh cannot be written
	 */
	public void write(Mesh mesh, DataOutputStream out) throws IOException {
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
		final var index = mesh.index();
		if(index.isPresent()) {
			write(index.get(), out);
		}
		else {
			out.writeInt(0);
		}
	}

	/**
	 * Writes mesh data to the given output stream.
	 * @param data		Data buffer
	 * @param out		Output
	 * @throws IOException if the data cannot be written
	 */
	private static void write(MeshData data, DataOutputStream out) throws IOException {
		// Write data header
		final int length = data.length();
		out.writeInt(length);

		// Stop if empty
		if(length == 0) {
			return;
		}

		// Buffer data
		final var buffer = ByteBuffer.allocate(length);
		data.buffer(buffer);

		// Copy buffer to output
		out.write(buffer.array());
	}
}
