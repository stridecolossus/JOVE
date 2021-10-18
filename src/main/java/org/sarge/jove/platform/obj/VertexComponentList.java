package org.sarge.jove.platform.obj;

import java.util.ArrayList;

/**
 * List of OBJ vertex components that can also be retrieved using negative indices.
 */
class VertexComponentList<T> extends ArrayList<T> {
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
