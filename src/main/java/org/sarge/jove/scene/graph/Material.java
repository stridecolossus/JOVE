package org.sarge.jove.scene.graph;

import java.util.List;

/**
 * A <i>material</i> specifies the rendering properties of a {@link MeshNode}.
 * @author Sarge
 */
public interface Material {
	/**
	 * @return Render queue for this material
	 */
	RenderQueue queue();

	/**
	 * @return Rendering state switch keys for this material
	 */
	List<Renderable> states();
}
