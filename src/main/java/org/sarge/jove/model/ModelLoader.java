package org.sarge.jove.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.DataHelper;
import org.sarge.jove.io.ResourceLoader;

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
	public BufferedModel load(DataInputStream in) throws IOException {
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
		return new BufferedModel(primitive, layouts, count, vertices, index);
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
		final List<Layout> layouts = model.layout();
		out.writeInt(layouts.size());
		for(Layout c : layouts) {
			helper.write(c, out);
		}

		// Write vertices
		helper.write(model.vertexBuffer(), out);

		// Write index
		final Optional<Bufferable> index = model.indexBuffer();
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
