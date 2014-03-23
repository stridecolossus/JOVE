package org.sarge.jove.particle;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sarge.jove.animation.Player;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.material.PointSpriteProperty;
import org.sarge.jove.material.RenderProperty;
import org.sarge.jove.particle.ParticleSystem.CollisionAction;
import org.sarge.jove.scene.Node;
import org.sarge.jove.scene.RenderContext;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.io.Element;
import org.sarge.lib.io.Element.ElementException;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictMap;
import org.sarge.lib.util.StringConverter;
import org.sarge.lib.util.Util;

/**
 * Loads a particle system from an XML descriptor.
 * @author Sarge
 */
public class ParticleSystemLoader {
	private static final Map<Class<?>, StringConverter<?>> converters = new StrictMap<>();

	static {
		// Register primitives
		register( float.class, StringConverter.FLOAT );
		register( int.class, StringConverter.INTEGER );
		register( long.class, StringConverter.LONG );
		register( boolean.class, StringConverter.BOOLEAN );
		register( String.class, StringConverter.STRING );

		// Register engine primitives
		register( Point.class, Point.CONVERTER );
		register( Vector.class, Vector.CONVERTER );
		register( Colour.class, Colour.CONVERTER );
	}

	private static final String CLASSNAME = "class";
	private static final String CREATION_RATE = "creation-rate";
	private static final String AGE_LIMIT = "age-limit";

	/**
	 * Registers a string-to-object converter.
	 * @param clazz Data-type
	 * @param c Converter
	 */
	public static <T> void register( Class<T> clazz, StringConverter<T> c ) {
		converters.put( clazz, c );
	}

	private final DataSource src;

	/**
	 * Constructor.
	 * @param src Data-source
	 */
	public ParticleSystemLoader( DataSource src ) {
		Check.notNull( src );
		this.src = src;
	}

	/**
	 * Loads a particle system scene-graph from the given XML descriptor.
	 * @param path XML file-path
	 * @return Particle system scene-graph
	 * @throws Exception if the descriptor is invalid
	 * @throws IOException if the file cannot be loaded
	 */
	public Node load( String path, RenderingSystem sys ) throws ElementException, IOException {
		// Load XML
		final Element root = Element.load( src.open( path ) );

		// Load particle system
		final ParticleSystem ps = load( root, sys );

		// Create scene
		final Node node = new Node( path );
		node.add( ps );
		//node.add( new PointSizeEffect( root.getFloat( "point-size", 10f ) ) );

		final RenderProperty effect = new RenderProperty() {
			@Override
			public String getType() {
				return "point-sprite";
			}

			@Override
			public void apply( RenderContext ctx ) {
				ctx.getRenderingSystem().setPointSprites( new PointSpriteProperty() );
			}

			@Override
			public void reset( RenderContext ctx ) {
				ctx.getRenderingSystem().setPointSprites( null );
			}
		};
		node.add( effect );

		// Start particle system
		if( root.getBoolean( "auto", true ) ) {
			ps.setState( Player.State.PLAYING );
		}

		return node;
	}

	/**
	 * Loads particle system from the given XML descriptor.
	 */
	private ParticleSystem load( Element root, RenderingSystem sys ) throws ElementException {
		// Load particle system properties
		final Emitter emitter = load( getChild( root, "emitter" ), Emitter.class );
		final DirectionFactory dirFactory = load( getChild( root, "direction-factory" ), DirectionFactory.class );
		final ColourFactory colFactory = load( getChild( root, "colour-factory" ), ColourFactory.class );

		// Create particle system
		final ParticleSystem ps = new ParticleSystem( emitter, dirFactory, colFactory );

		// Load optional parameters
		ps.setMaximumParticleCount( root.getInteger( "max-particles", Integer.MAX_VALUE ) );
		ps.setRepeating( root.getBoolean( "repeating", false ) );
		if( root.hasAttribute( CREATION_RATE ) ) {
			ps.setCreationRate( root.getFloat( CREATION_RATE, null ) );
		}
		if( root.hasAttribute( AGE_LIMIT ) ) {
			ps.setAgeLimit( root.getLong( AGE_LIMIT, null ) );
		}

		// Load particle influences
		for( Element e : root.getChildren( "influence" ) ) {
			final Influence inf = load( e, Influence.class );
			ps.add( inf );
		}

		// Load collision surfaces
		for( Element e : root.getChildren( "surface" ) ) {
			final CollisionSurface surface = load( e, CollisionSurface.class );
			final CollisionAction action = e.getEnum( "action", CollisionAction.KILL, CollisionAction.class );
			ps.add( surface, action );
		}

		// Create initial particles
		final int num = root.getInteger( "initial-count", 0 );
		if( num > 0 ) {
			ps.create( num, System.currentTimeMillis() );
		}

		return ps;
	}

	/**
	 * Finds a mandatory child element.
	 */
	private static Element getChild( Element root, String name ) throws ElementException {
		final Element child = root.getChild( name );
		if( child == null ) throw new ElementException( "Expected element: " + name, root );
		return child;
	}

	/**
	 * Loads an object with the given name and class.
	 * @param root		XML
	 * @param name		Element name
	 * @param base		Expected base-class
	 * @return Object
	 * @throws ElementException if the element cannot be found or the object cannot be instantiated
	 */
	private static <T> T load( Element root, Class<T> base ) throws ElementException {
		final Class<? extends T> clazz = findClass( root, base );
		final Object obj = loadObject( root, clazz );
		return base.cast( obj );
	}

	/**
	 * Loads the class specified in the given XML.
	 */
	@SuppressWarnings("unchecked")
	private static <T> Class<? extends T> findClass( Element root, Class<T> base ) throws ElementException {
		// Load class-name
		final String classname = root.getString( CLASSNAME, null );

		// Find class
		final Class<?> actual;
		try {
			actual = Class.forName( classname );
		}
		catch( Exception e ) {
			throw new ElementException( "Unknown class: " + e.getMessage(), root );
		}

		// Check correct type
		if( !base.isAssignableFrom( actual ) ) {
			throw new ElementException( "Incorrect class: expected=" + base.getName() + ", actual=" + actual.getName(), root );
		}

		return (Class<? extends T>) actual;
	}

	/**
	 * Recursively loads an object from an XML element.
	 * @param root		Root element
	 * @param base		Base-class
	 * @return Object
	 * @throws ElementException if the object cannot be loaded
	 */
	private static <T> T loadObject( Element root, Class<T> base ) throws ElementException {
		//
		final Class<? extends T> clazz;
		if( root.hasAttribute( CLASSNAME ) ) {
			clazz = findClass( root, base );
		}
		else {
			clazz = base;
		}

		// Match children to class members
		final List<Field> fields = new ArrayList<>();
		for( Element e : root.getChildren() ) {
			final String name = e.getName();
			final Field field;
			try {
				field = clazz.getDeclaredField( name );
			}
			catch( NoSuchFieldException ex ) {
				throw new ElementException( "Unknown field: name=" + name + " class=" + clazz.getName(), e );
			}
			fields.add( field );
		}

		// Build list of parameter types
		final Class<?>[] types = new Class<?>[ fields.size() ];
		for( int n = 0; n < types.length; ++n ) {
			types[ n ] = fields.get( n ).getType();
		}

		// Find matching constructor
		final Constructor<? extends T> ctor = findConstructor( root, clazz, types );

		// Load arguments
		final Object[] args = new Object[ types.length ];
		for( int n = 0; n < args.length; ++n ) {
			// Ensure field can be accessed
			final Field f = fields.get( n );
			final boolean accessible = f.isAccessible();
			if( !accessible ) f.setAccessible( true );

			// Load argument
			final Element e = root.getChild( f.getName() );
			if( e.getChildren().isEmpty() ) {
				final String str = e.getText().trim();
				if( Util.isEmpty( str ) ) {
					// Attempt default ctor
					final Constructor<?> defaultCtor = findConstructor( e, f.getType(), new Class<?>[]{} );
					args[ n ] = instantiate( root, defaultCtor, new Object[]{} );
				}
				else {
					// Convert literal value
					args[ n ] = load( e, f.getType(), str );
				}
			}
			else {
				// Recurse
				args[ n ] = loadObject( e, f.getType() );
			}

			// Reset field
			if( !accessible ) f.setAccessible( false );
		}

		// Create object
		return instantiate( root, ctor, args );
	}

	/**
	 * Loads an object from a comma-delimited string using the registered converters.
	 * @param root		XML
	 * @param clazz		Type
	 * @param text		Comma-delimited value
	 * @return Object
	 * @throws ElementException if the object cannot be created
	 * @see #register(Class, StringConverter)
	 */
	private static <T> T load( Element root, Class<T> clazz, String text ) throws ElementException {
		// Lookup converter of this type
		final StringConverter<?> converter = converters.get( clazz );
		if( converter == null ) throw new ElementException( "No converter for class: " + clazz.getName(), root );

		// Convert values
		final Object obj;
		try {
			obj = converter.convert( text );
		}
		catch( NumberFormatException e ) {
			throw new ElementException( "Error converting to object: " + clazz.getName(), root );
		}

		// Convert to type
		if( clazz.isPrimitive() ) {
			return (T) obj;
			//return Util.toPrimitive( obj );
		}
		else {
			if( !clazz.isAssignableFrom( obj.getClass() ) ) {
				throw new ElementException( "Incorrect type: expected=" + clazz + ", actual=" + obj.getClass(), root );
			}
			return clazz.cast( obj );
		}
	}

	/**
	 * Finds a matching constructor.
	 * @param root		Parent element
	 * @param clazz		Class
	 * @param types		Signature
	 * @return Constructor
	 * @throws ElementException if a matching constructor cannot be found
	 */
	private static <T> Constructor<T> findConstructor( Element root, Class<T> clazz, Class<?>[] types ) throws ElementException {
		try {
			return clazz.getConstructor( types );
		}
		catch( NoSuchMethodException e ) {
			throw new ElementException( "No matching constructor", root );
		}
	}

	/**
	 * Instantiates an object.
	 * @param root		Parent element
	 * @param ctor		Constructor
	 * @param args		Arguments
	 * @return New object
	 * @throws ElementException if the object cannot be created
	 */
	private static <T> T instantiate( Element root, Constructor<T> ctor, Object[] args ) throws ElementException {
		try {
			return ctor.newInstance( args );
		}
		catch( Exception e ) {
			throw new ElementException( "Error instantiating object", root );
		}
	}
}
