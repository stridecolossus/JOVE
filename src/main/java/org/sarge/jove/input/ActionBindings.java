package org.sarge.jove.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
 * 		bindings.add("SomeAction", new SomeAction() );
 * 		...
 *
 * 		// Bind some events
 * 		bindings.bind( new InputEvent( ... ), "action-identifier" );
 * 		...
 *
 * 		// Bind an event and register action handler in one call
 * 		bindings.bind( new InputEvent( ... ), new SomeOtherAction() );
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
	public void add(String name, Action action) {
		actions.put(name, action);
	}

	/**
	 * Looks up the action bound to the given event.
	 * @param key Event descriptor
	 * @return Bound action if present
	 */
	public Optional<Action> getAction(EventKey key) {
		// Lookup binding for this event
		final String id = bindings.get(key);
		if(id == null) return Optional.empty();
		
		// Lookup action for this binding
		final Action action = actions.get(id);
		return Optional.ofNullable(action);
	}

	/**
	 * Binds an event to an action.
	 * @param key	Event descriptor
	 * @param id	Action identifier
	 * @throws IllegalArgumentException if the given action has not been added to this set of bindings
	 * @see #add(Action)
	 * @see Action#getName()
	 */
	public void bind(EventKey key, String id) {
		if(!actions.containsKey(id)) throw new IllegalArgumentException("Unknown action ID: " + id);
		bindings.put(key, id);
	}

	/**
	 * Convenience method that combines adding an action and binding to it.
	 * @param key		Event descriptor
	 * @param name		Action identifier
	 * @param action	Action
	 * @throws IllegalArgumentException if the action has already been added to this set of bindings
	 * @see #add(Action)
	 */
	public void bind(EventKey key, String name, Action action) {
		if(actions.containsKey(name)) throw new IllegalArgumentException("Action already added:" + name);
		add(name, action);
		bind(key, name);
	}

	/**
	 * Removes an event-action bindings.
	 * @param key Event descriptor
	 */
	public void remove(EventKey key) {
		bindings.remove(key);
	}

	/**
	 * Clears all bindings.
	 */
	public void clear() {
		bindings.clear();
	}

	/**
	 * Persists this set of bindings to the given output stream.
	 * @param out Output stream
	 * @throws IOException if the bindings cannot be persisted
	 */
	public void save(OutputStream out) throws IOException {
		// Build properties file
		final Properties props = new Properties();
		for(Entry<EventKey, String> entry : bindings.entrySet()) {
			final EventKey key = entry.getKey();
			final String id = entry.getValue();
			props.put(key.toString(), id);
		}

		// Persist bindings
		props.store(out, null);
	}

	/**
	 * Loads bindings from the given properties file.
	 * @param props Properties set
	 */
	public void load(InputStream in) throws IOException {
		// Load properties
		final Properties props = new Properties();
		props.load(in);

		// Build bindings
		for(Entry<Object, Object> entry : props.entrySet()) {
			final EventKey key = loadEventKey((String) entry.getKey());
			final String id = (String) entry.getValue();
			bindings.put(key, id);
		}
	}

	/**
	 * Creates an input event descriptor from the given text.
	 */
	private static EventKey loadEventKey(String text) throws IOException {
		// Tokenize event name
		final String[] tokens = text.split("\\+");

		// Lookup event type
		final EventType type = EventType.valueOf(tokens[0]);
		if(type == null) throw new IOException("Invalid event type: " + tokens[0]);

		// Determine event name
		switch(tokens.length) {
		case 1:
			return new EventKey(type);

		case 2:
			return new EventKey(type, tokens[1]);

		default:
			throw new IOException("Invalid input-event name: " + text);
		}
	}

	@Override
	public void handle(InputEvent event) {
		final Optional<Action> action = getAction(event.getEventKey());
		action.ifPresent(handler -> handler.execute(event));
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
