package org.sarge.jove.common;

import org.sarge.lib.util.ToString;

/**
 * OpenGL resource template.
 * <p>
 * A <i>resource</i> is anything that has been allocated on the hardware such as textures, VBOs, etc.
 * <p>
 * This class is used a template base-class for such resources and provides methods that can be used to track allocated
 * resources to identify resource leaks.
 * <p>
 * In addition it also automatically releases resources that have been garbage collected but not explicitly released
 * and logs these orphaned resources.
 * <p>
 * @author Sarge
 */
public abstract class AbstractGraphicResource implements GraphicResource {
//	private static final Logger log = Logger.getLogger( AbstractGraphicResource.class.getName() );

	private static final int DELETED = -1;

//	private class ResourceRef extends WeakReference<GraphicResource> {
//		private final int id;
//
//		public ResourceRef( GraphicResource referent, int id ) {
//			super( referent );
//			this.id = id;
//		}
//	}

//	private static final Map<Class<? extends GraphicResource>, ResourceRef> resources = new StrictMap<>();

//	private ResourceRef ref;

	private int id = DELETED;

	@Override
	public final int getResourceID() {
		return id;
	}

	/**
	 * Allocates this resource.
	 * @param id OpenGL resource identifier
	 * @throws IllegalArgumentException if the ID is invalid or this resource is already allocated
	 */
	protected void setResourceID( int id ) {
		if( id < 1 ) throw new IllegalArgumentException( "Invalid resource ID: " + id );
		if( this.id != DELETED ) throw new IllegalArgumentException( "Resource already allocated: " + this );
		this.id = id;
	}

	/**
	 * Deletes this resource from the GPU.
	 * @param id Resource ID
	 */
	@SuppressWarnings("hiding")
	protected abstract void delete( int id );

	@Override
	public final void release() {
		if( id == DELETED ) throw new IllegalArgumentException( "Resource already released: " + this );
		delete( id );
		id = DELETED;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
