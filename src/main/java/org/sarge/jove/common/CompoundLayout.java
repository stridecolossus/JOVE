package org.sarge.jove.common;

import java.util.*;

/**
 * A <i>compound layout</i> specifies the structure of compound data types such as vertices.
 * @author Sarge
 */
public final class CompoundLayout {
	private final List<Layout> layout;
	private final int stride;

	// TODO - record
	// TODO - is this a layout as well, i.e. recursive?

	/**
	 * Constructor.
	 * @param layout Layout
	 */
	public CompoundLayout(List<Layout> layout) {
		this.layout = List.copyOf(layout);
		this.stride = layout.stream().mapToInt(Layout::stride).sum();
	}

	/**
	 * Constructor.
	 * @param layout Layout
	 */
	public CompoundLayout(Layout... layout) {
		this(Arrays.asList(layout));
	}

	/**
	 * @return Layout
	 */
	public List<Layout> layout() {
		return layout;
	}

	/**
	 * @return Stride (bytes)
	 */
	public int stride() {
		return stride;
	}

	/**
	 * @return Whether this layout contains the given element (by identity)
	 */
	public boolean contains(Layout component) {
		return layout.stream().anyMatch(c -> c == component);
	}

	@Override
	public int hashCode() {
		return layout.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof CompoundLayout that) &&
				this.layout.equals(that.layout);
	}

	@Override
	public String toString() {
		return layout.toString();
	}
}
