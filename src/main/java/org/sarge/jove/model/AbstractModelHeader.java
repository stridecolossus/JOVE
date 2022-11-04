package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.model.Model.Header;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractModelHeader implements Header {
	protected final Primitive primitive;

	/**
	 * Constructor.
	 * @param primitive Drawing primitive
	 */
	protected AbstractModelHeader(Primitive primitive) {
		this.primitive = notNull(primitive);
	}

	@Override
	public final Primitive primitive() {
		return primitive;
	}

	@Override
	public int hashCode() {
		return Objects.hash(primitive, count(), layout());
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Header that) &&
				(this.primitive == that.primitive()) &&
				(this.count() == that.count()) &&
				this.layout().equals(that.layout());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(primitive)
				.append("count", count())
				.append(layout())
				.append("indexed", isIndexed())
				.build();
	}
}
