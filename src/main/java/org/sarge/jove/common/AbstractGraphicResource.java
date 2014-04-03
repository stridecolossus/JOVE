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

//	private static final Map<Class<? extends GraphicResource>, Releaser> registry = new StrictMap<>();
//	private static final Map<Class<? extends GraphicResource>, ReferenceQueue<? extends GraphicResource>> queues = new StrictMap<>();

//	/**
//	 * Releaser for graphic resources.
//	 */
//	protected static interface Releaser {
//		/**
//		 * Releases the given resource.
//		 * @param id Resource id
//		 */
//		void release( int id );
//	}
//
//	/**
//	 * Registers the releaser for the given class.
//	 * @param clazz			Graphic resource class
//	 * @param releaser		Releaser
//	 */
//	protected static <T extends GraphicResource> void register( Class<T> clazz, Releaser releaser ) {
//		registry.put( clazz, releaser );
//		queues.put( clazz, new ReferenceQueue<T>() );
//	}
//
//	private static class ResourceReference <T extends GraphicResource> extends WeakReference<T> {
//		private final int id;
//
//		public ResourceReference( T res, int id, ReferenceQueue<T> q ) {
//			super( res, q );
//			this.id = id;
//		}
//	}
//
//	public static void clean() {
//		for( ReferenceQueue<?> q : queues.values() ) {
//			final ResourceReference<?> ref = q.poll();
//
//		}
//	}

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

//		final ReferenceQueue<?> q = queues.get( this.getClass() );
//		if( q == null ) throw new RuntimeException( "Resource class not registered: " + this.getClass().getName() );
//
//		new ResourceReference<>( this, id, q );
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
