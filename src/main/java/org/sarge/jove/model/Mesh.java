package org.sarge.jove.model;

import org.sarge.jove.model.Model.Header;
import org.sarge.jove.scene.Renderable;

/**
 * A <i>mesh</i> is a renderable model.
 * @author Sarge
 */
public interface Mesh extends Renderable {
	/**
	 * @return Model header
	 */
	Header header();
}
