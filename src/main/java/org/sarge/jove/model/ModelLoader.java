package org.sarge.jove.model;

import java.io.*;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.io.*;
import org.sarge.jove.model.Model.Header;

/**
 * The <i>model loader</i> is used to persist and load a JOVE model.
 * @author Sarge
 */
public class ModelLoader implements ResourceLoader<DataInputStream, BufferedModel> {
	private final DataHelper helper = new DataHelper();

	@Override
	public DataInputStream map(InputStream in) throws IOException {
		return new DataInputStream(in);
	}

	@Override
	public BufferedModel load(DataInputStream in) throws IOException {
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
		final Header header = new AbstractModelHeader(primitive) {
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
		return new BufferedModel() {
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
		// Write file header
		helper.writeVersion(out);

		// Write model header
		final Header header = model.header();
		out.writeUTF(header.primitive().name());
		out.writeInt(header.count());

		// Write vertex layout
		final List<Component> layout = header.layout().components();
		out.writeInt(layout.size());
		for(Component c : layout) {
			helper.write(c, out);
		}

		// Write vertices
		final BufferedModel mesh = model.buffer();
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
