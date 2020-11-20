package org.sarge.jove.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Action.PositionAction;
import org.sarge.jove.control.Action.ValueAction;
import org.sarge.jove.control.InputEvent.Source;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.util.Check;

/**
 * An <i>action bindings</i> maps input events to actions.
 * TODO - doc
 */
public class Bindings implements Consumer<InputEvent<?>> {
	private final Map<Action<?>, Set<Type>> actions = new HashMap<>();
	private final Map<Type, Action<?>> bindings = new HashMap<>();

	/**
	 * Adds an action to this set of bindings.
	 * @param action Action<?> handler
	 * @throws IllegalArgumentException for a duplicate action
	 */
	public void add(Action<?> action) {
		Check.notNull(action);
		if(actions.containsKey(action)) throw new IllegalArgumentException("Duplicate action: " + action);
		actions.put(action, new HashSet<>());
		// TODO - addLocal()
	}

	/**
	 * @return Actions in this set of bindings
	 */
	public Stream<Action<?>> actions() {
		return actions.keySet().stream();
	}

	/**
	 * Helper - Looks up the bindings for the given action.
	 * @param action Action<?>
	 * @return Bindings
	 */
	private Set<Type> get(Action<?> action) {
		final var<Type> bindings = actions.get(action);
		if(bindings == null) throw new IllegalArgumentException("Action<?> not present: " + action);
		return bindings;
	}

	/**
	 * Looks up all events bound to the given action.
	 * @param action Action<?> handler
	 * @return Input events bound to the given action
	 * @throws IllegalArgumentException if the action is not present in this set of bindings
	 */
	public Stream<Type> bindings(Action<?> action) {
		return get(action).stream();
	}

	/**
	 * Looks up the action bound to an event.
	 * @param type Input type
	 * @return Action<?>
	 */
	public Optional<Action<?>> binding(Type type) {
		return Optional.ofNullable(bindings.get(type));
	}

	/**
	 * Binds an input event to the given action.
	 * @param <T> Event type
	 * @param type			Input event
	 * @param action		Action<?> handler
	 * @throws IllegalStateException if the event is already bound
	 */
	public <T extends Type> void bind(T type, Action<T> action) {
		Check.notNull(type);
		Check.notNull(action);
		if(bindings.containsKey(type)) throw new IllegalStateException("Event is already bound: " + type);
		actions.computeIfAbsent(action, ignored -> new HashSet<>()).add(type);
		bindings.put(type, action);
	}

	public <T extends Type> void bind(Source<T> src, Action<T> action) {
		final var types = src.types();
		if(types.size() != 1) throw new IllegalArgumentException("Expected an input source with exactly one event type: " + src);
		bind(types.get(0), action);
	}

	/**
	 * Removes the binding for the given type of event.
	 * @param type Event type
	 */
	public void remove(Type type) {
		final Action<?> action = bindings.remove(type);
		if(action != null) {
			actions.get(action).remove(type);
		}
	}

	/**
	 * Removes <b>all</b> bindings for the given action.
	 * @param action Action<?>
	 * @throws IllegalArgumentException if the action is not present
	 */
	public void remove(Action<?> action) {
		final var set = get(action);
		set.forEach(bindings::remove);
		set.clear();
	}

	/**
	 * Removes <b>all</b> bindings.
	 */
	public void clear() {
		actions.values().forEach(Set::clear);
		bindings.clear();
	}

	@Override
	public void accept(InputEvent<?> event) {
		// Lookup binding
		final Action<?> action = bindings.get(event.type());

		// Ignore if none
		if(action == null) {
			return;
		}

		// Delegate
		// TODO - this is horrible, can we double-dispatch somehow? or wrap action with a typed entry class of some sort? (ideally doing any casting/switch at bind-time)
		if(event instanceof Button button) {
			final Runnable actual = (Runnable) action;
			actual.run();
		}
		else
		if(event instanceof Position.Event pos) {
			final PositionAction actual = (PositionAction) action;
			actual.handle(pos.x(), pos.y());
		}
		else
		if(event instanceof Axis.Event axis) {
			final ValueAction actual = (ValueAction) action;
			actual.handle(axis.value());
		}
		else {
			throw new UnsupportedOperationException(event.getClass().getName());
		}
	}

//	/**
//	 * Writes this set of bindings to the given output stream.
//	 * TODO - doc format
//	 * @param out Output stream
//	 */
//	public void save(Writer out) {
//		try(final var writer = new PrintWriter(out)) {
//			for(Action<?> action : actions.keySet()) {
//				final StringJoiner str = new StringJoiner(StringUtils.SPACE);
//				str.add(action.toString());
//				actions.get(action).stream().map(Bindings::write).forEach(str::add);
//				writer.println(str);
//			}
//		}
//	}
//
//	private static String write(Type type) {
//		return new StringJoiner(Type.DELIMITER)
//				.add(type.getClass().getSimpleName())
//				.add(type.name())
//				.toString();
//	}
//
//	/**
//	 * Loads a set of bindings from the given input stream.
//	 * @param in Input stream
//	 * @throws IOException if the bindings cannot be loaded
//	 * @throws IllegalArgumentException if a binding cannot be parsed
//	 * @see #save(Writer)
//	 */
//	public void load(Reader r) throws IOException {
//		try(final var in = new LineNumberReader(r)) {
//			try {
//				in.lines().forEach(this::load);
//			}
//			catch(RuntimeException e) {
//				throw new IllegalArgumentException(e.getMessage() + " at line " + in.getLineNumber(), e);
//			}
//		}
//	}
//
//	/**
//	 * Loads bindings for an action.
//	 * @throws IllegalArgumentException if a binding cannot be parsed or the action is not present
//	 */
//	private void load(String line) {
//		// Tokenize
//		final String[] tokens = StringUtils.split(line);
//
//		// Lookup action
//		final Action<?> action = actions
//				.keySet()
//				.stream()
//				.filter(e -> e.toString().equals(tokens[0]))
//				.findAny()
//				.orElseThrow(() -> new IllegalArgumentException("Action not present: " + tokens[0]));
//
//		// Parse events
//		Arrays.stream(tokens)
//				.skip(1)
//				.map(Bindings::parse)
//				//.forEach(e -> bind(e, action));
//				.forEach(e -> bindings.put(e, action)); // TODO
//	}
//
//	/**
//	 * Parses an event type from its string representation.
//	 */
//	private static Type parse(String str) {
//		// Tokenize event type
//		final String[] parts = StringUtils.split(str, Type.DELIMITER, 2);
//		if(parts.length != 2) throw new IllegalArgumentException("Invalid event binding");
//
//		// Parse event type
//		return switch(parts[0]) {
//			case "Position" -> new Position(parts[1]);
//			case "Button" -> Button.parse(parts[1]);
//			case "Axis" -> new Axis(parts[1]);
//			default -> throw new IllegalArgumentException("Invalid event type: " + parts[0]);
//		};
//	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("actions", actions.size())
				.append("bindings", bindings.size())
				.build();
	}
}
