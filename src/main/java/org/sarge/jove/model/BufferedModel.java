package org.sarge.jove.model;

import java.util.Optional;

import org.sarge.jove.common.Bufferable;

/**
 * A <i>buffered model</i> is a mesh comprised of buffered vertex data and indices for a model.
 * @author Sarge
 */
public interface BufferedModel extends Mesh {
	/**
	 * @return Model vertices
	 */
	Bufferable vertices();

	/**
	 * @return Index
	 */
	Optional<Bufferable> index();
}
