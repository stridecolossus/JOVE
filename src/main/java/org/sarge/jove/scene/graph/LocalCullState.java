package org.sarge.jove.scene.graph;

import static org.sarge.lib.util.Check.notNull;

public class LocalCullState {

	public enum CullState {
		HIDE,
		SHOW,
		INHERIT
	}

	private CullState state = CullState.INHERIT;
	private boolean culled;

	public CullState state() {
		return state;
	}

	public void set(CullState state) {
		this.state = notNull(state);
	}

	void update(LocalCullState parent) {
		culled = switch(state) {
			case HIDE -> true;
			case SHOW -> false;
			case INHERIT -> parent.culled;
		};
	}

	/**
	 *
	 * TODO
	 * - combine with local volume?
	 * - frustum culling overrides this setting?
	 * - otherwise would be very complex, e.g. state = SHOW but frustum culled => would still need to walk culled nodes
	 * - i.e. SHOW is only supported if parent has not been culled?
	 *
	 *
	 */

}
