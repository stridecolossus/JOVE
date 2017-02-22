package org.sarge.jove.geometry;

import java.util.List;

import org.sarge.lib.util.StrictList;
import org.sarge.lib.util.ToString;

/**
 * Mutable compound transformation.
 * @author Sarge
 */
public class CompoundTransform implements Transform {
	private final List<Transform> transforms = new StrictList<>();

	private boolean dirty;

	/**
	 * Adds a transform.
	 * @param t
	 */
	public void add(Transform t) {
		transforms.add(t);
		dirty = true;
	}

	/**
	 * Removes a transform.
	 * @param t
	 */
	public void remove(Transform t) {
		transforms.remove(t);
		dirty = true;
	}

	@Override
	public boolean isDirty() {
		// Check for changes to transforms
		if(dirty) return true;

		// Otherwise check child transforms
		if(transforms.stream().anyMatch(Transform::isDirty)) {
			return true;
		}

		// Transform has not changed
		return false;
	}

	@Override
	public Matrix toMatrix() {
		// Rebuild transform
		Matrix m = Matrix.IDENTITY;
		for(Transform t : transforms) {
			m = m.multiply(t.toMatrix());
		}

		// Mark as done
		dirty = false;

		return m;
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
