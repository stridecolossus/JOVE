package org.sarge.jove.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;

/**
 * Resource loader for a JOVE image.
 * @author Sarge
 */
public class ImageLoader implements DataResourceLoader<ImageData> {
	private final DataHelper helper = new DataHelper();

	@Override
	public ImageData load(DataInputStream in) throws IOException {
		// Validate file format
		helper.version(in);

		// Load image header
		final int count = in.readInt();
		final int mip = in.readInt();

		// Load dimensions
		final Dimensions size = new Dimensions(in.readInt(), in.readInt());

		// Load layout
		final Layout layout = helper.layout(in);

		// Load data buffer
		final Bufferable data = helper.buffer(in);

		// Create image
		return new ImageData(size, count, mip, layout, data);
	}

	@Override
	public void save(ImageData image, DataOutputStream out) throws IOException {
		// Write image image header
		helper.writeVersion(out);
		out.writeInt(image.count());
		out.writeInt(image.mip());

		// Write dimensions
		final Dimensions size = image.size();
		out.writeInt(size.width());
		out.writeInt(size.height());

		// Write layout
		helper.write(image.layout(), out);

		// Write image data
		helper.write(image.data(), out);
	}
}
