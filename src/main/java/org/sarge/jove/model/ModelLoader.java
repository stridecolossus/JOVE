package org.sarge.jove.model;

import java.io.*;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.io.*;

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
		final List<Component> components = new ArrayList<>();
		for(int n = 0; n < num; ++n) {
			final Component c = helper.layout(in);
			components.add(c);
		}

		// Load data
		final ByteSizedBufferable vertices = helper.buffer(in);
		final ByteSizedBufferable index = helper.buffer(in);

		// Create mesh
		return new BufferedModel(primitive, count, new Layout(components), vertices, index);
	}

	/**
	 * Writes a model to an output stream.
	 * @param model		Model
	 * @param out		Output stream
	 * @throws IOException if the model cannot be written
	 * @throws IllegalStateException if the model is undefined
	 * @see DefaultModel#mesh()
	 */
	public void save(DefaultModel model, DataOutputStream out) throws IOException {
		// Write file header
		helper.writeVersion(out);

		// Write model header
		out.writeUTF(model.primitive().name());
		out.writeInt(model.count());

		// Write vertex layout
		final List<Component> layout = model.layout().components();
		out.writeInt(layout.size());
		for(Component c : layout) {
			helper.write(c, out); // TODO - cast
		}

		// Write vertices
		final BufferedModel mesh = model.buffer();
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
