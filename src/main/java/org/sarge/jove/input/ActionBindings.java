package org.sarge.jove.input;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.sarge.lib.util.StrictMap;
import org.sarge.lib.util.ToString;

/**
 * Binds {@link EventKey}s to {@link Action}s.
 * <p>
 * The action bindings consist of two components:
 * <ul>
 * <li>a set of registered actions that is populated by the application</li>
 * <li>a set of mappings from {@link EventKey}s to action identifier that is mutable and can be persisted to/from a properties file</li>
 * </ul>
 * <p>
 * Usage:
 * <code>
 * 		// Register action handlers
 * 		final ActionBindings bindings = new ActionBindings();
 * 		bindings.add( new SomeAction() );
 * 		...
 *
 * 		// Bind some events
 * 		bindings.bind( new InputEvent( ... ), "action-identifier" );
 * 		...
 *
 * 		// Persist bindings
 * 		bindings.save( props );
 *
 * 		// Load saved bindings
 * 		bindings.clear();
 * 		bindings.load( props );
 * </code>
 * @author Sarge
 */
public class ActionBindings implements InputEventHandler {
	private final Map<EventKey, String> bindings = new StrictMap<>();
	private final Map<String, Action> actions = new StrictMap<>();

	/**
	 * Registers an action that can be bound.
	 * @param action Action to add
	 */
	public void add( Action action ) {
		actions.put( action.getName(), action );
	}

	/**
	 * Looks up the action bound to the given event.
	 * @param key Event descriptor
	 * @return Bound action or <tt>null</tt> if none
	 */
	public Action getAction( EventKey key ) {
		final String id = bindings.get( key );
		if( id == null ) return null;
		return actions.get( id );
	}

	/**
	 * Binds an event to an action.
	 * @param key	Event descriptor
	 * @param id	Action identifier
	 * @throws IllegalArgumentException if the given action has not been added to this set of bindings
	 * @see #add(Action)
	 * @see Action#getName()
	 */
	public void bind( EventKey key, String id ) {
		if( !actions.containsKey( id ) ) throw new IllegalArgumentException( "Unknown action ID: " + id );
		bindings.put( key, id );
	}

	/**
	 * Convenience method that combines adding an action and binding to it.
	 * @param key		Event descriptor
	 * @param action	Action
	 * @throws IllegalArgumentException if the action has already been added to this set of bindings
	 * @see #add(Action)
	 */
	public void bind( EventKey key, Action action ) {
		final String id = action.getName();
		if( actions.containsKey( id ) ) throw new IllegalArgumentException( "Action already added:" + id );
		add( action );
		bind( key, id );
	}

	/**
	 * Removes an event-action bindings.
	 * @param key Event descriptor
	 */
	public void remove( EventKey key ) {
		bindings.remove( key );
	}

	/**
	 * Clears all bindings.
	 */
	public void clear() {
		bindings.clear();
	}

	/**
	 * Persists this set of bindings to the given properties file.
	 * @param props Properties set
	 */
	public void save( Properties props ) {
		for( Entry<EventKey, String> entry : bindings.entrySet() ) {
			final EventKey key = entry.getKey();
			final String id = entry.getValue();
			props.put( key.toString(), id );
		}
	}

	/**
	 * Loads bindings from the given properties file.
	 * @param props Properties set
	 */
	public void load( Properties props ) throws IOException {
		for( Entry<Object, Object> entry : props.entrySet() ) {
			final EventKey key = loadEventKey( (String) entry.getKey() );
			final String id = (String) entry.getValue();
			bindings.put( key, id );
		}
	}

	/**
	 * Creates an input event descriptor from the given text.
	 */
	private static EventKey loadEventKey( String text ) throws IOException {
		// Tokenize event name
		final String[] tokens = text.split( "\\+" );

		// Lookup event type
		final EventType type = EventType.valueOf( tokens[0] );
		if( type == null ) throw new IOException( "Invalid event type: " + tokens[0] );

		// Determine event name
		final String name;
		switch( tokens.length ) {
		case 1:
			name = null;
			break;

		case 2:
			name = tokens[1];
			break;

		default:
			throw new IOException( "Invalid input-event name: " + text );
		}

		// Create event descriptor
		return new EventKey( type, name );
	}

	@Override
	public void handle( InputEvent event ) {
		// Lookup bound action
		final Action action = getAction( event.getEventKey() );

		// Execute action
		if( action != null ) {
			action.execute( event );
		}

		// Re-pool event
		InputEvent.POOL.restore( event );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
