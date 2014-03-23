package org.sarge.jove.material;

import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.scene.RenderContext;

/**
 * Built-in properties.
 *
 * TODO
 * - viewport
 * - resolution
 * - aspect ratio
 * - frustum near/far
 * - camera left/up
 *
 * @author Sarge
 */
public enum MaterialProperty {
	/**
	 * Frame render time.
	 */
	TIME( PropertyScope.FRAME ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			return ctx.getTime();
		}
	},

	/**
	 * Time elapsed since previous frame.
	 */
	ELAPSED_TIME( PropertyScope.FRAME ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			return ctx.getElapsed();
		}
	},

	/**
	 * Projection matrix (P).
	 */
	PROJECTION_MATRIX( PropertyScope.FRAME ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			return ctx.getScene().getProjectionMatrix();
		}
	},

	/**
	 * View (or camera) matrix (V).
	 */
	VIEW_MATRIX( PropertyScope.FRAME ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			return ctx.getScene().getCamera().getViewMatrix();
		}
	},

	/**
	 * Model (or world) matrix for the current node (M).
	 */
	MODEL_MATRIX( PropertyScope.NODE ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			return ctx.getModelMatrix();
		}
	},

	/**
	 * Normal matrix.
	 */
	NORMAL_MATRIX( PropertyScope.NODE ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			final Matrix v = ctx.getScene().getCamera().getViewMatrix();
			final Matrix m = ctx.getModelMatrix();
			final Matrix mv = v.multiply( m );
			return mv.getSubMatrix( 3 );
		}
	},

	/**
	 * Model-view matrix (VM).
	 */
	MODELVIEW_MATRIX( PropertyScope.NODE ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			final Matrix v = ctx.getScene().getCamera().getViewMatrix();
			final Matrix m = ctx.getModelMatrix();
			return v.multiply( m );
		}
	},

	/**
	 * Projection-model-view matrix (PVM).
	 */
	PROJECTION_MODELVIEW_MATRIX( PropertyScope.NODE ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			final Matrix p = ctx.getScene().getProjectionMatrix();
			final Matrix v = ctx.getScene().getCamera().getViewMatrix();
			final Matrix m = ctx.getModelMatrix();
			return p.multiply( v ).multiply( m );
		}
	},

	/**
	 * Camera (or eye) position.
	 */
	CAMERA_POSITION( PropertyScope.FRAME ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			return ctx.getScene().getCamera().getPosition();
		}
	},

	/**
	 * Camera (or view) direction.
	 */
	CAMERA_DIRECTION( PropertyScope.FRAME ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			return ctx.getScene().getCamera().getDirection();
		}
	},

	/**
	 * Active lights.
	 */
	LIGHTS( PropertyScope.NODE ) {
		@Override
		public Object getValue( RenderContext ctx ) {
			return ctx.getLights();
		}
	};

	private final PropertyScope scope;

	private MaterialProperty( PropertyScope scope ) {
		this.scope = scope;
	}

	/**
	 * @return Scope of this property
	 */
	public PropertyScope getScope() {
		return scope;
	}

	/**
	 * Retrieves up this property value.
	 * @param ctx Render context
	 * @return Property value
	 */
	public abstract Object getValue( RenderContext ctx );
}
