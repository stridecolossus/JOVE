package org.sarge.jove.common;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>layout</i> specifies the structure of compound data types such as tuples, vertices, arrays, etc.
 * @author Sarge
 */
public final class CompoundLayout {
	private final List<Layout> components;
	private final int stride;

	/**
	 * Constructor.
	 * @param components Components of this layout
	 */
	public CompoundLayout(List<Layout> components) {
		this.components = List.copyOf(components);
		this.stride = components.stream().mapToInt(Layout::stride).sum();
	}

	/**
	 * Constructor.
	 * @param components Components of this layout
	 */
	public CompoundLayout(Layout... components) {
		this(Arrays.asList(components));
	}

	/**
	 * @return Components of this layout
	 */
	public List<Layout> components() {
		return components;
	}

	/**
	 * @return Stride (bytes)
	 */
	public int stride() {
		return stride;
	}

	/**
	 * @return Whether this model contains the given component (by identity)
	 */
	public boolean contains(Layout component) {
		return components.stream().anyMatch(c -> c == component);
	}

	@Override
	public int hashCode() {
		return components.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof CompoundLayout that) &&
				this.components.equals(that.components);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(components).build();
	}
}
