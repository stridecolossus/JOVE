package org.sarge.jove.model;

import java.io.*;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.io.*;

/**
 * The <i>model loader</i> persists a JOVE model as a renderable {@link Mesh}.
 * @author Sarge
 */
public class ModelLoader implements ResourceLoader<DataInputStream, Mesh> {
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
		final List<Component> layout = new ArrayList<>();
		for(int n = 0; n < num; ++n) {
			final Component c = helper.layout(in);
			layout.add(c);
		}

		// Load data
		final Bufferable vertices = helper.buffer(in);
		final Bufferable index = helper.buffer(in);

		// Create model header
		final Header header = new Header() {
			@Override
			public Primitive primitive() {
				return primitive;
			}

			@Override
			public Layout layout() {
				return new Layout(layout);
			}

			@Override
			public int count() {
				return count;
			}

			@Override
			public boolean isIndexed() {
				return index != null;
			}
		};

		// Create mesh
		return new Mesh() {
			@Override
			public Header header() {
				return header;
			}

			@Override
			public Bufferable vertices() {
				return vertices;
			}

			@Override
			public Optional<Bufferable> index() {
				return Optional.ofNullable(index);
			}
		};
	}

	/**
	 * Writes a model to an output stream.
	 * @param model		Model
	 * @param out		Output stream
	 * @throws IOException if the model cannot be written
	 * @throws IllegalStateException if the model is undefined
	 * @see Model#mesh()
	 */
	public void save(Model model, DataOutputStream out) throws IOException {
		// Write model header
		helper.writeVersion(out);
		out.writeUTF(model.primitive().name());
		out.writeInt(model.count());

		// Write vertex layout
		final List<Component> layout = model.layout().components();
		out.writeInt(layout.size());
		for(Component c : layout) {
			helper.write(c, out);
		}

		// Write vertices
		final Mesh mesh = model.mesh();
		helper.write(mesh.vertices(), out);

		// Write index
		final Optional<Bufferable> index = mesh.index();
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
