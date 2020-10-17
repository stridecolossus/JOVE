package org.sarge.jove.model;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.model.Model.AbstractModel;
import org.sarge.jove.util.Loader;

/**
 * Loader for a buffered model.
 * @author Sarge
 */
public class ModelLoader implements Loader<InputStream, Model> {
	private static final int VERSION = 1;
	private static final char SPACE = ' ';

	/**
	 * Writes the given model to an output stream.
	 * @param model		Model
	 * @param out		Output stream
	 * @throws IOException if the model cannot be written
	 */
	public void write(Model model, OutputStream out) throws IOException {
		write(model, new PrintStream(out));
	}

	/**
	 * Writes the given model.
	 */
	private static void write(Model model, PrintStream out) throws IOException {
		// Write file format version
		out.println(VERSION);

		// Write model primitive
		out.println(model.primitive());

		// Write vertex layout
		for(Vertex.Component c : model.layout().components()) {
			out.print(c);
			out.print(SPACE);
		}
		out.println();

		// Output vertex buffer
		final FloatBuffer vertices = model.vertices().asFloatBuffer();
		out.println(vertices.limit());
		for(int n = 0; n < vertices.limit(); ++n) {
			out.print(vertices.get());
			out.print(SPACE);
		}
		out.println();

		// Output index buffer
		if(model.index().isPresent()) {
			final IntBuffer index = model.index().get().asIntBuffer();
			out.println(index.limit());
			for(int n = 0; n < index.limit(); ++n) {
				out.print(index.get());
				out.print(SPACE);
			}
			out.println();
		}
		else {
			out.println(0);
		}

		// Done
		out.flush();
	}

	/**
	 * Loads a buffered model from the given input stream.
	 * @param in Input stream
	 * @return New model
	 * @throws UnsupportedOperationException if the file format version of the model is not supported by this loader
	 * @throws RuntimeException if the model cannot be parsed
	 */
	@Override
	public Model load(InputStream in) {
		try(final Scanner scanner = new Scanner(in)) {
			return load(scanner);
		}
	}

	/**
	 * Reads model.
	 */
	private static Model load(Scanner in) {
		// Load and verify file format version
		final int version = in.nextInt();
		if(version > VERSION) {
			throw new UnsupportedOperationException(String.format("Unsupported version: version=%d supported=%d", version, VERSION));
		}

		// Load primitive
		final Primitive primitive = Primitive.valueOf(in.next());

		// Load layout
		in.nextLine();
		final var components = Arrays.stream(in.nextLine().split(" ")).map(Vertex.Component::valueOf).collect(toList());
		final var layout = new Vertex.Layout(components);

		// Load vertices
		final int vertexCount = in.nextInt();
		final ByteBuffer vertices = Bufferable.allocate(vertexCount * Float.BYTES);
		for(int n = 0; n < vertexCount; ++n) {
			vertices.putFloat(in.nextFloat());
		}

		// Load index
		final int indexCount = in.nextInt();
		final ByteBuffer index;
		if(indexCount == 0) {
			index = null;
		}
		else {
			index = Bufferable.allocate(indexCount * Integer.BYTES);
			for(int n = 0; n < indexCount; ++n) {
				index.putInt(in.nextInt());
			}
		}

		// Determine vertex count
		final int count = indexCount == 0 ? vertexCount : indexCount;

		// Create buffered model
		return new AbstractModel(primitive, layout, count) {
			@Override
			public ByteBuffer vertices() {
				return vertices.rewind().asReadOnlyBuffer();
			}

			@Override
			public Optional<ByteBuffer> index() {
				return Optional.ofNullable(index).map(ByteBuffer::rewind).map(ByteBuffer::asReadOnlyBuffer);
			}
		};
	}
}
