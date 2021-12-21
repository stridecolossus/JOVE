package org.sarge.jove.io;

import static org.sarge.lib.util.Check.oneOrMore;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.util.BufferHelper;

/**
 * Data resource loader utilities.
 * <p>
 * The supported file format version can be initialised in the constructor.
 * <p>
 * @author Sarge
 */
@SuppressWarnings("static-method")
public class DataHelper {
	private static final int VERSION = 1;

	private static final Bufferable EMPTY = new Bufferable() {
		@Override
		public int length() {
			return 0;
		}

		@Override
		public void buffer(ByteBuffer bb) {
			throw new UnsupportedOperationException();
		}
	};

	private final int ver;

	/**
	 * Default constructor.
	 */
	public DataHelper() {
		this(VERSION);
	}

	/**
	 * Constructor.
	 * @param ver Supported version number
	 */
	public DataHelper(int ver) {
		this.ver = oneOrMore(ver);
	}

	/**
	 * Writes the version number.
	 * @param out Output stream
	 */
	public void writeVersion(DataOutput out) throws IOException {
		out.writeInt(ver);
	}

	/**
	 * Loads and validates the version number.
	 * @param in Input stream
	 * @return Version number
	 * @throws UnsupportedOperationException if the file version is higher than the supported version number
	 */
	public int version(DataInput in) throws IOException {
		final int actual = in.readInt();
		if(actual > ver) {
			throw new UnsupportedOperationException(String.format("Unsupported version: version=%d supported=%d", actual, ver));
		}
		return actual;
	}

	/**
	 * Loads a bufferable object.
	 * @param in Input stream
	 * @return New buffer or {@code null} if empty
	 * @throws IOException if the buffer cannot be loaded
	 */
	public Bufferable buffer(DataInput in) throws IOException {
		// Read buffer size
		final int len = in.readInt();

		// Ignore if empty buffer
		if(len == 0) {
			return EMPTY;
		}

		// Load bytes
		final byte[] bytes = new byte[len];
		in.readFully(bytes);

		// Convert to buffer
		return Bufferable.of(bytes);
	}

	/**
	 * Writes a bufferable object.
	 * @param obj	Bufferable object to write
	 * @param out	Output stream
	 */
	public void write(Bufferable obj, DataOutput out) throws IOException {
		// Output length
		final int len = obj.length();
		out.writeInt(len);

		// Stop if empty buffer
		if(len == 0) {
			return;
		}

		// Write buffer
		final ByteBuffer bb = ByteBuffer.allocate(len).order(BufferHelper.NATIVE_ORDER);
		obj.buffer(bb);
		out.write(bb.array());
	}

	/**
	 * Loads a layout.
	 * @param in Input stream
	 * @return Layout
	 * @throws IOException if the layout cannot be loaded
	 */
	public Layout layout(DataInput in) throws IOException {
		// Load layout
		final int size = in.readInt();
		final int bytes = in.readInt();
		final String name = in.readUTF();
		final boolean signed = in.readBoolean();

		// Lookup layout component type
		final Class<?> type;
		try {
			type = Class.forName(name);
		}
		catch(ClassNotFoundException e) {
			throw new IOException("Unknown layout component type: " + name, e);
		}

		// Create layout
		return new Layout(size, type, bytes, signed);
	}

	/**
	 * Writes a layout.
	 * @param layout	Layout
	 * @param out		Output stream
	 * @throws IOException if the layout cannot be written
	 */
	public void write(Layout layout, DataOutput out) throws IOException {
		out.writeInt(layout.size());
		out.writeInt(layout.bytes());
		out.writeUTF(layout.type().getName());
		out.writeBoolean(layout.signed());
	}
}