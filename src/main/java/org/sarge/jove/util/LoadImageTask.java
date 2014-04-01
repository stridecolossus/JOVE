package org.sarge.jove.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.nio.ByteBuffer;

import org.sarge.jove.common.Dimensions;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Task to load an image.
 * @author Sarge
 */
public class LoadImageTask implements Runnable {
	private final ImageLoader loader;
	private final String path;

	private JoveImage image;

	/**
	 * Constructor.
	 * @param loader	Loader
	 * @param path		Image path
	 */
	public LoadImageTask( ImageLoader loader, String path ) {
		Check.notNull( loader );
		Check.notEmpty( path );

		this.loader = loader;
		this.path = path;
	}

	/**
	 * @return Loaded image
	 */
	public JoveImage getImage() {
		return image;
	}

//	public void setImage( JoveImage image ) {
//		this.image = image;
//	}

	@Override
	public void run() {
		try {
			loader.load( path );
		}
		catch( Exception e ) {
			throw new RuntimeException( "Error loading image: " + path, e );
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}

	public static void main( String[] args ) throws IntrospectionException {
		for( PropertyDescriptor pd : Introspector.getBeanInfo( LoadImageTask.class, Object.class ).getPropertyDescriptors() ) {
			System.out.println("name="+pd.getDisplayName()+" name="+pd.getName()+" desc="+pd.getShortDescription());
			System.out.println("before "+pd.getValue( "image" ));
			pd.setValue("image",new JoveImage() {

				@Override
				public boolean hasAlpha() {
					return false;
				}

				@Override
				public Dimensions getDimensions() {
					return null;
				}

				@Override
				public ByteBuffer getBuffer() {
					return null;
				}
			});
			System.out.println("after "+pd.getValue( "image" ));
		}
	}
}
