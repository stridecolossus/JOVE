package org.sarge.jove.common;

import java.util.ArrayList;
import java.util.List;

/**
 * A <i>compound layout</i> comprises a list of layouts that are compared by <i>identity</i>.
 * @author Sarge
 */
public class CompoundLayout {
	private final List<Layout> layouts;

	/**
	 * Constructor.
	 * @param layouts Layouts
	 */
	public CompoundLayout(List<Layout> layouts) {
		this.layouts = List.copyOf(layouts);
	}

	/**
	 * Default constructor.
	 */
	protected CompoundLayout() {
		this.layouts = new ArrayList<>();
	}

	/**
	 * @return Layouts
	 */
	public List<Layout> layouts() {
		return layouts;
	}

	/**
	 * Calculates the total <i>stride</i> of this layout.
	 * @return Stride
	 */
	public int stride() {
		return layouts.stream().mapToInt(Layout::length).sum();
	}

	/**
	 * Tests whether this compound layout contains the given layout by <i>identity</i>.
	 * @param layout Layout
	 * @return Whether this compound layout contains the given layout
	 */
	public boolean contains(Layout layout) {
		return layouts.stream().anyMatch(e -> e == layout);
	}

	/**
	 * Sub-class helper - Adds a layout.
	 * @param layout Layout to add
	 */
	protected void add(Layout layout) {
		layouts.add(layout);
	}

	@Override
	public final int hashCode() {
		return layouts.hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof CompoundLayout that) &&
				equals(that);
	}

	private boolean equals(CompoundLayout that) {
		final Object[] a = this.layouts.toArray();
		final Object[] b = that.layouts.toArray();
		if(a.length != b.length) {
			return false;
		}
		for(int n = 0; n < a.length; ++n) {
			if(a[n] != b[n]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public final String toString() {
		return layouts.toString();
	}
}