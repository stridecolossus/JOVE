package org.sarge.jove.platform.obj;

import java.util.ArrayList;

/**
 * Custom list implementation for OBJ vertex components.
 *
 * @author Sarge
 */
class VertexComponentList<T> extends ArrayList<T> {
	/**
	 * @param index Element index, starts at <b>one</b> and can be negative, e.g. -1 is the <b>last</b> element
	 * @throws IndexOutOfBoundsException for an index of zero
	 */
	@Override
	public T get(int index) {
		if(index > 0) {
			return super.get(index - 1);
		}
		else
		if(index < 0) {
			return super.get(size() + index);
		}
		else {
			throw new IndexOutOfBoundsException("Invalid zero index");
		}
	}
}
