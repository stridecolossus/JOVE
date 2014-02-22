package org.sarge.jove.input;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.sarge.lib.util.ListMap;
import org.sarge.lib.util.StrictMap;

/**
 * Mutable table of bindings associating {@link EventName}s with {@link Action}s.
 * <p>
 * Only one action can be bound to any given input event, whereas actions can be bound to multiple events.
 * @see #getEvents(Action)
 * <p>
 * Action bindings can be persisted to and from a properties file.
 * TODO - is this logical/possible? e.g. how to restore a camera action?
 * @see #save(Properties)
 * <p>
 * @author Sarge
 */
public class ActionBindings implements InputEventHandler {
	private final Map<EventName, Action> bindings = new StrictMap<>();
	private final ListMap<Action, EventName> reverse = new ListMap<>();

	/**
	 * Retrieves the action handler for the given event.
	 * @param event Event descriptor
	 * @return Action handler or <tt>null</tt> if no binding exists
	 */
	public Action getAction( EventName event ) {
		return bindings.get( event );
	}

	/**
	 * Retrieves the input-events that are bound to the given action handler.
	 * @param action Action handler
	 * @return List of input-events or <tt>null</tt> if no binding exists
	 */
	public List<EventName> getEvents( Action action ) {
		return reverse.get( action );
	}

	/**
	 * Binds an input-event to an action handler.
	 * @param event		Event descriptor
	 * @param action	Action handler
	 */
	public void add( EventName event, Action action ) {
		bindings.put( event, action );
		reverse.add( action, event );
	}

	/**
	 * Removes a binding.
	 * @param event Binding to remove
	 */
	public void remove( EventName event ) {
		final Action action = bindings.remove( event );
		reverse.remove( action, event );
	}

	/**
	 * Removes all bindings.
	 */
	public void clear() {
		bindings.clear();
		reverse.clear();
	}

	/**
	 * Loads action bindings from the given configuration file.
	 * @param props Bindings as a properties file
	 * @throws IOException if the file is invalid
	 * @see #save(Properties)
	 */
	public void load( Properties props ) throws IOException {
		for( Entry<Object, Object> entry : props.entrySet() ) {
			final EventName event = createInputEvent( (String) entry.getKey() );
			final Action action = createAction( (String) entry.getValue() );
			add( event, action );
		}
	}

	/**
	 * Creates an input event descriptor from the given text.
	 */
	private static EventName createInputEvent( String text ) throws IOException {
		// Tokenize input-event name
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
		return new EventName( type, name );
	}

	/**
	 * Instantiates an action handler with the given class-path.
	 * TODO - move to ReflectionUtils?
	 * TODO - this assumes *all* actions have default ctors *only*, this surely cannot be viable!?
	 */
	private static Action createAction( String classpath ) throws IOException {
		// Lookup action class
		final Class<?> clazz;
		try {
			clazz = Class.forName( classpath );
		}
		catch( ClassNotFoundException e ) {
			throw new IOException( "Unknown action class: " + classpath );
		}

		// Verify is an action
		if( !Action.class.isAssignableFrom( clazz ) ) throw new IOException( "Not an action: " + classpath );

		// Create handler instance
		try {
			return (Action) clazz.newInstance();
		}
		catch( Exception e ) {
			throw new RuntimeException( "Error instantiating action: " + classpath, e );
		}
	}

	/**
	 * Outputs this set of bindings to the given properties file.
	 * Each binding key is output in the format specified by {@link EventName#toString()}.
	 * @param props Properties file
	 * @see #load(Properties)
	 */
	public void save( Properties props ) {
		for( Entry<EventName, Action> entry : bindings.entrySet() ) {
			final String event = entry.getKey().toString();
			final String classpath = entry.getValue().getClass().getName();
			props.put( event, classpath );
		}
	}

	@Override
	public void handle( InputEvent event ) {
		final Action action = getAction( event.getEventKey() );
		if( action != null ) action.execute( event );
	}

	@Override
	public String toString() {
		return bindings.toString();
	}
}
