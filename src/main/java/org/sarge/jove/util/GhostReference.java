package org.sarge.jove.util;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

public class GhostReference<T> extends PhantomReference<T> {
	private static final Field field;

	static {
		try {
			field = Reference.class.getDeclaredField( "referent" );
			field.setAccessible( true );
		}
		catch( Exception e ) {
			throw new RuntimeException( "Cannot access referent field" );
		}
	}

	private static final Collection<GhostReference<?>> references = new HashSet<GhostReference<?>>();

	/**
	 * Constructor.
	 * @param referent		Referent object
	 * @param queue			Notification queue
	 */
	public GhostReference( T referent, ReferenceQueue<T> queue ) {
		super( referent, queue );
		references.add( this );
	}

	/**
	 * Retrieves the referent object.
	 * @return Referent
	 */
	public T getReferent() {
		try {
			return (T) field.get( this );
		}
		catch( Exception e ) {
			throw new RuntimeException( "Cannot access referant" );
		}
	}

	@Override
	public void clear() {
		references.remove( this );
		super.clear();
	}

	public static void main( String[] args ) {
		System.out.println( "start" );
		final ReferenceQueue<String> queue = new ReferenceQueue<String>();

		final Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					while( true ) {
						System.out.println( "waiting..." );
						final GhostReference<String> gr = (GhostReference<String>) queue.remove();
						System.out.println( "got "+gr.getReferent());
						gr.clear();
					}
				}
				catch( Exception e ) {
					e.printStackTrace();
				}
			}
		};
		final Thread t = new Thread( r );
		t.setDaemon( true );
		t.start();

		String str = "string";
		new GhostReference<String>( str, queue );

		str = null;
//		System.gc();

		/*
		for( int n = 0; n < 10; ++n ) {
			new GhostReference<String>( "string-" + n, queue );
		}
		*/

		System.out.println( "allocating..." );
		final byte[][] array = new byte[ 1024 ][];
		int idx = 0;
		while( true ) {
			array[ idx ] = new byte[ 1024 * 1024 ];
			++idx;
		}

	}
}
