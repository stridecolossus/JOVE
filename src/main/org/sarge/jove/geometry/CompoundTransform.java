package org.sarge.jove.geometry;

import java.util.List;

import org.sarge.lib.util.StrictList;
import org.sarge.lib.util.ToString;

/**
 * Compound transformation.
 * @author Sarge
 */
public class CompoundTransform implements Transform {
	private final List<Transform> transforms = new StrictList<>();

	private boolean dirty;

	/**
	 * Default constructor for empty transform.
	 */
	public CompoundTransform() {
	}

	/**
	 * Constructor given a list of transforms.
	 * @param transforms Transforms
	 */
	public CompoundTransform( List<Transform> transforms ) {
		for( Transform t : transforms ) {
			add( t );
		}
	}

	/**
	 * Constructor given an array of transforms.
	 * @param transforms Transforms
	 */
	public CompoundTransform( Transform[] transforms ) {
		for( Transform t : transforms ) {
			add( t );
		}
	}

	/**
	 * Adds a transform.
	 * @param t
	 */
	public void add( Transform t ) {
		transforms.add( t );
		dirty = true;
	}

	/**
	 * Removes a transform.
	 * @param t
	 */
	public void remove( Transform t ) {
		transforms.remove( t );
		dirty = true;
	}

	@Override
	public boolean isDirty() {
		// Check for changes to transforms
		if( dirty ) return true;

		// Otherwise check child transforms
		for( Transform t : transforms ) {
			if( t.isDirty() ) return true;
		}

		// Transform has not changed
		return false;
	}

	@Override
	public Matrix toMatrix() {
		// Rebuild transform
		Matrix m = Matrix.IDENTITY;
		for( Transform t : transforms ) {
			m = m.multiply( t.toMatrix() );
		}

		// Mark as done
		dirty = false;

		return m;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
