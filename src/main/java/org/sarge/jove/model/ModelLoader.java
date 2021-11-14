package org.sarge.jove.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.DataHelper;
import org.sarge.jove.io.DataResourceLoader;
import org.sarge.jove.model.Model.Header;

/**
 * The <i>model loader</i> persists a JOVE model.
 * @author Sarge
 */
public class ModelLoader implements DataResourceLoader<Model> {
	private final DataHelper helper = new DataHelper();

	@Override
	public void save(Model model, DataOutputStream out) throws IOException {
		// Write model header
		final Header header = model.header();
		helper.writeVersion(out);
		out.writeUTF(header.primitive().name());
		out.writeInt(header.count());

		// Write vertex layout
		final List<Layout> layouts = header.layout();
		out.writeInt(layouts.size());
		for(Layout layout : layouts) {
			helper.write(layout, out);
		}

		// Write VBO
		helper.write(model.vertices(), out);

		// Write index
		final var index = model.index();
		if(index.isPresent()) {
			helper.write(index.get(), out);
		}
		else {
			out.writeInt(0);
		}
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
		final Header header = new Header(layouts, primitive, count);
		return new BufferedModel(header, vertices, Optional.ofNullable(index));
	}
}
