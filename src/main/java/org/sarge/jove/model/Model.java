package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;

/**
 * A <i>model</i> is comprised of a vertex buffer with a specified layout and an optional index.
 * @author Sarge
 */
public interface Model {
	/**
	 * @return Vertex layout
	 */
	List<Layout> layout();

	/**
	 * @return Drawing primitive
	 */
	Primitive primitive();

	/**
	 * @return Draw count
	 */
	int count();

	/**
	 * @return Vertex buffer
	 */
	Bufferable vertices();

	/**
	 * @return Whether this is an indexed model
	 * @see #index()
	 */
	boolean isIndexed();

	/**
	 * @return Index buffer
	 * @see #isIndexed()
	 */
	Bufferable index();

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractModel implements Model {
		private final Primitive primitive;

		/**
		 * Constructor.
		 * @param primitive Drawing primitive
		 */
		protected AbstractModel(Primitive primitive) {
			this.primitive = notNull(primitive);
		}

		@Override
		public Primitive primitive() {
			return primitive;
		}

		/**
		 * Validates this model.
		 * @throws IllegalArgumentException if the draw count is not valid for the drawing primitive
		 * @throws IllegalArgumentException if the layout specifies normals which is not supported by the drawing primitive
		 * @see Primitive#isValidVertexCount(int)
		 * @see Primitive#isNormalSupported()
		 */
		public void validate() throws IllegalArgumentException {
			if(!primitive.isValidVertexCount(count())) {
				throw new IllegalArgumentException(String.format("Invalid number of model vertices %d for primitive %s", count(), primitive));
			}

			// TODO
//			if(!primitive.isNormalSupported() && layout.stream().anyMatch(e -> e == Vector.LAYOUT)) {
//				throw new IllegalArgumentException("Normals not supported for primitive: " + primitive);
//			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(layout(), primitive, count());
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Model that) &&
					this.layout().equals(that.layout()) &&
					primitive.equals(that.primitive()) &&
					(this.count() == that.count()) &&
					this.vertices().equals(that.vertices()) &&
					Objects.equals(this.index(), that.index());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append(layout())
					.append(primitive)
					.append("count", count())
					.append("indexed", isIndexed())
					.build();
		}
	}
}
