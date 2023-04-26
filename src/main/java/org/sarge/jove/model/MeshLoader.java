package org.sarge.jove.model;

import java.io.*;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.io.*;

/**
 * The <i>mesh loader</i> is used to persist and load a {@link BufferedMesh}.
 * @author Sarge
 */
public class MeshLoader implements ResourceLoader<DataInputStream, Mesh> {
	private final DataHelper helper = new DataHelper();

	@Override
	public DataInputStream map(InputStream in) throws IOException {
		return new DataInputStream(in);
	}

	@Override
	public Mesh load(DataInputStream in) throws IOException {
		// Load and verify file format version
		helper.version(in);

		// Load model header
		final Primitive primitive = Primitive.valueOf(in.readUTF());
		final int count = in.readInt();

		// Load vertex layout
		final int num = in.readInt();
		final List<Layout> layout = new ArrayList<>();
		for(int n = 0; n < num; ++n) {
			final Layout c = helper.layout(in);
			layout.add(c);
		}

		// Load data
		final ByteSizedBufferable vertices = helper.buffer(in);
		final ByteSizedBufferable index = helper.buffer(in);

		// Create mesh
		return new Mesh(primitive, new CompoundLayout(layout), () -> count, vertices, index);
	}

	/**
	 * Writes a mesh to an output stream.
	 * @param mesh		Mesh
	 * @param out		Output stream
	 * @throws IOException if the mesh cannot be written
	 */
	public void save(Mesh mesh, DataOutputStream out) throws IOException {
		// Write file header
		helper.writeVersion(out);

		// Write model header
		out.writeUTF(mesh.primitive().name());
		out.writeInt(mesh.count());

		// Write vertex layout
		final List<Layout> layout = mesh.layout().layout();
		out.writeInt(layout.size());
		for(Layout c : layout) {
			helper.write(c, out); // TODO - cast
		}

		// Write vertices
		helper.write(mesh.vertices(), out);

		// Write index
		final Optional<ByteSizedBufferable> index = mesh.index();
		if(index.isPresent()) {
			helper.write(index.get(), out);
		}
		else {
			out.writeInt(0);
		}

		// TODO - do we need this?
		out.flush();
	}
}
