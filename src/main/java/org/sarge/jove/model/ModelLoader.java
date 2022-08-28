package org.sarge.jove.model;

import java.io.*;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.io.*;

/**
 * The <i>model loader</i> persists a JOVE model.
 * @author Sarge
 */
public class ModelLoader implements ResourceLoader<DataInputStream, Model> {
	private final DataHelper helper = new DataHelper();

	@Override
	public DataInputStream map(InputStream in) throws IOException {
		return new DataInputStream(in);
	}

	@Override
	public Model load(DataInputStream in) throws IOException {
		// Load and verify file format version
		helper.version(in);

		// Load model header
		final Primitive primitive = Primitive.valueOf(in.readUTF());
		final int count = in.readInt();

		// Load vertex layout
		final int num = in.readInt();
		final List<Layout> layouts = new ArrayList<>();
		for(int n = 0; n < num; ++n) {
			final Layout layout = helper.layout(in);
			layouts.add(layout);
		}

		// Load data
		final Bufferable vertices = helper.buffer(in);
		final Bufferable index = helper.buffer(in);

		// Create model
		return new DefaultModel(primitive, count, new CompoundLayout(layouts), vertices, index);
	}

	/**
	 * Writes a model to an output stream.
	 * @param model		Model
	 * @param out		Output stream
	 * @throws IOException if the model cannot be written
	 */
	public void save(Model model, DataOutputStream out) throws IOException {
		// Write model header
		helper.writeVersion(out);
		out.writeUTF(model.primitive().name());
		out.writeInt(model.count());

		// Write vertex layout
		final List<Layout> layouts = model.layout().layouts();
		out.writeInt(layouts.size());
		for(Layout c : layouts) {
			helper.write(c, out);
		}

		// Write vertices
		helper.write(model.vertices(), out);

		// Write index
		final Optional<Bufferable> index = model.index();
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
