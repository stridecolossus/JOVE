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
import org.sarge.jove.model.Vertex.Component;

/**
 * The <i>grid builder</i> constructs a grid of vertices in the X-Z plane.
 * TODO
 * - diagram illustrating size of grid (number of vertices vs number of quads)
 * - explain difference between primitive and index factory
 * - static model if no index
 * @author Sarge
 */
public class GridBuilder {
	/**
	 * A <i>height function</i> is used to set the height of grid vertices (Y axis).
	 */
	public interface HeightFunction {
		/**
		 * Calculates the height at the given coordinates.
		 * @param row Row
		 * @param col Column
		 * @return Height
		 */
		float height(int x, int y);

		/**
		 * Creates a height function that returns a literal height for all tiles.
		 * @param height Height
		 * @return New literal height function
		 */
		static HeightFunction literal(float height) {
			return (x, y) -> height;
		}

		/**
		 * Creates a height function that maps to the given image, i.e. equivalent to a texture sampler.
		 * @param image Image
		 * @return Image height function
		 * @see ImageData#pixel(int, int)
		 */
		static HeightFunction of(Dimensions size, ImageData image) {
			final Dimensions dim = image.extents().size();
			final float w = dim.width() / size.width();
			final float h = dim.height() / size.height();
			return (row, col) -> {
				final int pixel = image.pixel((int) (col * w), (int) (row * h));
				return pixel / 65535.0f; // (float) Short.MAX_VALUE; // TODO
			};
		}
	}

	private Dimensions size = new Dimensions(4, 4);
	private float tile = 1;
	private HeightFunction height = HeightFunction.literal(0);
	private Primitive primitive = Primitive.TRIANGLES;
	private IndexFactory index = Triangle.INDEX_TRIANGLES;

	/**
	 * Sets the size of the grid (number of vertices in each direction).
	 * @param size Grid size
	 */
	public GridBuilder size(Dimensions size) {
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
	// TODO - dimensions for tile w/h and float scalar

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
	 * Sets the index factory used to generate indices for the grid (default is {@link Triangle#INDEX_TRIANGLES}).
	 * @param index Index factory or {@code null} for no index
	 */
	public GridBuilder index(IndexFactory index) {
		this.index = index;
		return this;
	}

	// triangles, quads or isolines
	// SpacingEqual, SpacingFractionalEven, and SpacingFractionalOdd
	// https://satellitnorden.wordpress.com/2018/02/12/vulkan-adventures-part-3-return-of-the-triangles-tessellation-tutorial/

	/**
	 * Constructs this grid.
	 * @param layout Grid vertex layout
	 * @return New grid
	 */
	public DefaultModel build() {
		// Init model
		final ModelBuilder model = new ModelBuilder();
		model.primitive(primitive);
		model.layout(List.of(Component.POSITION, Component.COORDINATE));

		// Calculate half distance in both directions
		final int w = size.width();
		final int h = size.height();
		final float dx = tile * (w - 1) / 2;
		final float dz = tile * (h - 1) / 2;

		// Build grid vertices (column major)
		final List<Vertex> vertices = new ArrayList<>();
		for(int row = 0; row < h; ++row) {
			for(int col = 0; col < w; ++col) {
				// Determine grid position and height
				final float x = col * tile - dx;
				final float z = row * tile - dz;
				final float y = height.height(col, row);
				final Point pos = new Point(x, y, z);

				// TODO - normals from height function

				// Calculate texture coordinate
				final Coordinate coord = new Coordinate2D((float) col / w, (float) row / h);

				// Add grid vertex
				final Vertex vertex = new Vertex(pos, null, coord, null);
				vertices.add(vertex);
			}
		}

		if(index == null) {
			// Build grid without index according to the drawing primitive
			final Optional<IndexFactory> factory = primitive.index();
			if(factory.isPresent()) {
				build(factory.get()).mapToObj(vertices::get).forEach(model::add);
			}
			else {
				vertices.forEach(model::add);
			}
		}
		else {
			// Build indexed grid
			vertices.forEach(model::add);
			build(index).forEach(model::add);
		}

		// Build grid model
		return model.build();
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
				.flatMap(start -> factory.strip(w).map(n -> n + start));
	}
}
