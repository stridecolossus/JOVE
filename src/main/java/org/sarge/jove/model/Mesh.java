package org.sarge.jove.model;

import java.util.Optional;

import org.sarge.jove.common.Bufferable;

/**
 * A <i>mesh</i> is a renderable model.
 * @author Sarge
 */
public interface Mesh {
	/**
	 * @return Model header
	 */
	Header header();

	/**
	 * @return Vertices
	 */
	Bufferable vertices();

	/**
	 * @return Index
	 */
	Optional<Bufferable> index();
}
