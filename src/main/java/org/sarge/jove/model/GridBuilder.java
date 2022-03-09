package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.util.Mask;
import org.sarge.lib.util.Check;

/**
 * The <i>grid builder</i> constructs a grid of vertices in the X-Z plane with an optional index buffer.
 * <p>
 * The height of each vertex in the grid (i.e. the Y component) is generated by a {@link HeightFunction}.
 * <p>
 * The {@link #indexed(boolean)} method is used to configure the behaviour for primitives that provide a {@link Primitive#index()} for the following use-cases:
 * <p>
 * <table border=1>
 * <tr><td>indexed</td><td>indexed primitive</td><td>behaviour</td></tr>
 * <tr><td>yes</td><td>yes</td><td>indexed model</td></tr>
 * <tr><td>no</td><td>yes</td><td>unindexed model with duplicate vertices</td></tr>
 * <tr><td>n/a</td><td>no</td><td>point or patch grid</td></tr>
 * </table>
 * <p>
 * @author Sarge
 */
public class GridBuilder {
	/**
	 * A <i>height function</i> is used to set the height of grid vertices (Y axis).
	 */
	@FunctionalInterface
	public interface HeightFunction {
		/**
		 * Calculates the height at the given coordinates.
		 * @param row Row
		 * @param col Column
		 * @return Height
		 */
		float height(int row, int col);

		/**
		 * Creates a height function that returns a literal height for all tiles.
		 * @param height Height
		 * @return New literal height function
		 */
		static HeightFunction literal(float height) {
			return (x, y) -> height;
		}

		/**
		 * Creates a function that looks up values from the given height-map image.
		 * @param size			Grid dimensions
		 * @param image 		Image
		 * @param component		Component channel index for height values
		 * @param scale			Height scalar
		 * @return Image height function
		 * @throws IllegalArgumentException if the component index is invalid for the given image
		 * @see ImageData#pixel(int, int, int)
		 */
		static HeightFunction heightmap(Dimensions size, ImageData image, int component, float scale) {
			// Validate
			Check.zeroOrMore(component);
			if(component >= image.components().length()) throw new IllegalArgumentException(String.format("Invalid component index: component=%d image=%s", component, image));

			// Map grid coordinates to image dimensions
			final Dimensions dim = image.extents().size();
			final float w = dim.width() / size.width();
			final float h = dim.height() / size.height();

			// Calculate height normalisation scalar
			final float normalise = scale / Mask.unsignedMaximum(Byte.SIZE * image.layout().bytes());

			// Create function
			return (row, col) -> {
				final int x = (int) (col * w);
				final int y = (int) (row * h);
				return image.pixel(x, y, component) * normalise;
			};
		}
	}

	private Dimensions size = new Dimensions(4, 4);
	private float tile = 1;
	private HeightFunction height = HeightFunction.literal(0);
	private Primitive primitive = Primitive.TRIANGLES;
	private IndexFactory index;

	/**
	 * Sets the size of the grid.
	 * <p>
	 * Note that {@link #size} specifies the number of <i>vertices</i> in each direction, i.e. one more than the number of <i>quads</i> comprising the grid.
	 * <p>
	 * @param size Grid size
	 * @throws IllegalArgumentException if the size is not greater-than one
	 */
	public GridBuilder size(Dimensions size) {
		if((size.width() < 2) || (size.height() < 2)) throw new IllegalArgumentException("Size must be greater than one");
		this.size = notNull(size);
		return this;
	}

	/**
	 * Sets the grid tile size.
	 * @param tile Tile size
	 */
	public GridBuilder tile(float tile) {
		this.tile = tile;
		return this;
	}

	/**
	 * Sets the function used to set the height of grid vertices (default is zero).
	 * @param height Height function
	 */
	public GridBuilder height(HeightFunction height) {
		this.height = notNull(height);
		return this;
	}

	/**
	 * Sets the drawing primitive (default is {@link Primitive#TRIANGLES}).
	 * @param primitive Drawing primitive
	 */
	public GridBuilder primitive(Primitive primitive) {
		this.primitive = notNull(primitive);
		return this;
	}

	/**
	 * Sets the index factory used to generate an indexed model.
	 * @param index Index factory or {@code null} to delegate to {@link Primitive#index()}
	 * @see #primitive(Primitive)
	 */
	public GridBuilder index(IndexFactory index) {
		this.index = index;
		return this;
	}

	// TODO - triangles, quads or isolines
	// SpacingEqual, SpacingFractionalEven, and SpacingFractionalOdd
	// https://satellitnorden.wordpress.com/2018/02/12/vulkan-adventures-part-3-return-of-the-triangles-tessellation-tutorial/

	/**
	 * Constructs this grid.
	 * @return New grid model
	 */
	public MutableModel build() {
		// Calculate half distance in both directions
		final int w = size.width();
		final int h = size.height();
		final float halfWidth = tile * (w - 1) / 2;
		final float halfHeight = tile * (h - 1) / 2;

		// Build grid vertices (column major)
		final List<Vertex> vertices = new ArrayList<>();
		for(int row = 0; row < h; ++row) {
			for(int col = 0; col < w; ++col) {
				// Determine grid position and height
				final float x = col * tile - halfWidth;
				final float z = row * tile - halfHeight;
				final float y = height.height(row, col);
				final Point pos = new Point(x, y, z);

				// TODO - normals from height function

				// Calculate texture coordinate
				final Coordinate coord = new Coordinate2D((float) col / w, (float) row / h);

				// Add grid vertex
				final Vertex vertex = new Vertex().position(pos).coordinate(coord);
				vertices.add(vertex);
			}
		}

		// Build model
		final MutableModel model = new MutableModel()
				.primitive(primitive)
				.layout(Point.LAYOUT)
				.layout(Coordinate2D.LAYOUT);

		if(index == null) {
			final Optional<IndexFactory> factory = primitive.index();
			if(factory.isPresent()) {
				// Build unindexed model with duplicate vertices
				build(factory.get()).mapToObj(vertices::get).forEach(model::add);
			}
			else {
				// Otherwise assume point or patch grid
				vertices.forEach(model::add);
			}
		}
		else {
			// Build indexed model with the configured factory
			vertices.forEach(model::add);
			build(index).forEach(model::add);
		}

		return model;
	}

	/**
	 * Generates the grid index.
	 * @param factory Index factory
	 * @return Grid index
	 */
	private IntStream build(IndexFactory factory) {
		final int w = size.width() - 1;
		return IntStream
				.range(0, size.height() - 1)
				.map(row -> row * size.height())
				.flatMap(start -> factory.strip(start, w));
	}
}
