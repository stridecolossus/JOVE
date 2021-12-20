package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Layout;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractModel implements Model {
	protected final Primitive primitive;
	protected final List<Layout> layout;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 */
	protected AbstractModel(Primitive primitive, List<Layout> layout) {
		this.primitive = notNull(primitive);
		this.layout = new ArrayList<>(layout);
	}

	@Override
	public final Primitive primitive() {
		return primitive;
	}

	@Override
	public final List<Layout> layout() {
		return List.copyOf(layout);
	}

	// TODO - when?
	/**
	 * Validates this model.
	 * @param normals Whether this model explicitly contains vertex normals
	 * @throws IllegalArgumentException if the draw count is not valid for the drawing primitive
	 * @throws IllegalArgumentException if the layout specifies normals which is not supported by the drawing primitive
	 * @see Primitive#isValidVertexCount(int)
	 * @see Primitive#isNormalSupported()
	 */
	protected void validate() throws IllegalArgumentException {
		if(!primitive.isValidVertexCount(count())) {
			throw new IllegalArgumentException(String.format("Invalid number of model vertices %d for primitive %s", count(), primitive));
		}

		if(layout.contains(Vertex.NORMALS) && !primitive.isNormalSupported()) {
			throw new IllegalArgumentException("Normals not supported for primitive: " + primitive);
		}
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
				this.vertexBuffer().equals(that.vertexBuffer()) &&
				Objects.equals(this.indexBuffer(), that.indexBuffer());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(primitive)
				.append(layout)
				.append("count", count())
				.append("indexed", isIndexed())
				.build();
	}
}
