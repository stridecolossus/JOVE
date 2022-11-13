package org.sarge.jove.common;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>layout</i> specifies the structure of compound data types such as tuples, vertices, arrays, etc.
 * @author Sarge
 */
public final class Layout {
	private final List<Component> components;
	private final int stride;

	/**
	 * Constructor.
	 * @param components Components of this layout
	 */
	public Layout(List<Component> components) {
		this.components = List.copyOf(components);
		this.stride = components.stream().mapToInt(Component::stride).sum();
	}

	/**
	 * Constructor.
	 * @param components Components of this layout
	 */
	public Layout(Component... components) {
		this(Arrays.asList(components));
	}

	/**
	 * @return Components of this layout
	 */
	public List<Component> components() {
		return components;
	}

	/**
	 * @return Stride (bytes)
	 */
	public int stride() {
		return stride;
	}

	@Override
	public int hashCode() {
		return components.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Layout that) &&
				this.components.equals(that.components);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(components).build();
	}
}
