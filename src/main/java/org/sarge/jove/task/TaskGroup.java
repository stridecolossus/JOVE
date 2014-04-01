package org.sarge.jove.task;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ReflectionUtils;
import org.sarge.lib.util.StrictMap;
import org.sarge.lib.util.ToString;

/**
 * Task dependent on a set of sub-tasks.
 * <p>
 * A <i>dependency</i> is a common field in the parent and child tasks. On successful execution of a dependent task the result is copied (sing reflection) to the parent task.
 * <p>
 * Once all dependencies have completed successfully the parent task is started.
 * <p>
 * Inter-task dependencies can be inferred using the {@link #add(Task)} method or explicitly specified by {@link #add(Task, String)}.
 * <p>
 * Usage:
 * <code>
 * 		// Create a producer work-unit
 * 		final Runnable producer = new Runnable() {
 * 			private Texture texture;
 * 			...
 * 		};
 *
 * 		// Create a consumer work-unit
 * 		final Runnable consumer = new Runnable() {
 * 			private Texture texture;
 * 			...
 * 		}
 *
 *		// Create parent task
 *		final TaskGroup parent = new TaskGroup( consumer, ... );
 *
 * 		// Infer a dependency
 * 		final Task one = new Task( producer, ... );
 * 		group.add( one );
 *
 * 		// Specify an explicit dependency
 * 		final Task two = new Task( ... );
 * 		group.add( two, "property" );
 *
 * 		// Note parents can also be dependencies
 * 		final TaskGroup another = new TaskGroup( ... );
 * 		another.add( parent );
 *
 * 		// The root task starts the processing of the entire tree
 * 		another.start();
 * </code>
 * <p>
 * @author Sarge
 */
public class TaskGroup extends Task {
	/**
	 * Dependency descriptor.
	 */
	private class Dependency {
		private Field src, target;

		private void copy( Task task ) {
			try {
				// Extract result from producer
				final Object value = src.get( task.getRunnable() );
				if( value == null ) throw new RuntimeException( "Dependent task returned NULL result: " + task.getRunnable() );

				// Copy to consumer
				target.set( TaskGroup.this.getRunnable(), value );
			}
			catch( IllegalAccessException e ) {
				throw new RuntimeException( "Error copying dependency result: " + task.getRunnable(), e );
			}
		}
	}

	/**
	 * Listener for lifecycle notifications from the dependent set of tasks.
	 */
	protected final Listener listener = new Listener() {
		@Override
		public void notify( Task task, State state ) {
			// Handle state change
			assert pending.contains( task );
			switch( state ) {
			case FINISHED:
				// Mark dependency as completed and copy result
				pending.remove( task );
				final Dependency dep = children.get( task );
				dep.copy( task );
				break;

			case FAILED:
				// Cancel any remaining pending tasks and stop processing
				pending.remove( task );
				cancel();
				return;

			default:
				// Ignore other lifecycle notifications
				return;
			}

			// Start parent task when all dependencies are completed
			if( pending.isEmpty() ) {
				TaskGroup.super.start();
			}
		}
	};

	private final Map<Task, Dependency> children = new StrictMap<>();
	private final Set<Task> pending = new HashSet<>();

	/**
	 * Constructor.
	 * @param r				Underlying work-unit
	 * @param priority		Priority
	 * @param queue			Execution queue
	 */
	public TaskGroup( Runnable r, int priority, TaskQueue queue ) {
		super( r, priority, queue );
	}

	/**
	 * Adds a dependency to this task.
	 * <p>
	 * The dependent field is inferred to be the <b>only</b> common property in both tasks.
	 * <p>
	 * @param child Dependent task
	 * @throws IllegalArgumentException if the given child is the parent task
	 * @throws IllegalArgumentException if no dependency was found
	 * @throws IllegalArgumentException if there are multiple possible dependencies (ambiguity)
	 * @throws IllegalArgumentException if a dependency for a given field already exists in this group (duplicate)
	 */
	public void add( Task child ) {
		// Verify child
		Check.notNull( child );
		if( child == this ) throw new IllegalArgumentException( "Cannot add dependency to self: " + this );
		if( children.containsKey( child ) ) throw new IllegalArgumentException( "Duplicate task: " + child );

		// Enumerate fields in child task and order by name
		final Map<String, Field> fields = getFields( child.getRunnable().getClass() );

		// Find matching target field in parent
		Field target = null;
		for( Field f : ReflectionUtils.getMemberFields( this.getRunnable().getClass() ) ) {
			// Lookup field in child
			final Field src = fields.get( f.getName() );
			if( src == null ) continue;

			// Note matching field
			if( target == null ) {
				target = f;
			}
			else {
				throw new IllegalArgumentException( "Ambiguous dependency (multiple potential matches found): this=" + this + " child=" + child );
			}
		}
		if( target == null ) throw new IllegalArgumentException( "Mis-matched dependency (no common property found): this=" + this + " child=" + child );

		// Check for duplicate dependency
		for( Dependency d : children.values() ) {
			if( d.target.equals( target ) ) {
				throw new IllegalArgumentException( "Duplicate property [" + target.getName() + "] this=" + this + " child=" + child );
			}
		}

		// Register dependency
		add( child, fields.get( target.getName() ), target );
	}

	/**
	 * Adds a dependency with the given property name.
	 * @param child			Dependent task
	 * @param property		Property name
	 * @throws IllegalArgumentException if the specified property does not exist in both tasks
	 */
	public void add( Task child, String property ) {
		// Lookup source field
		final Field src = getField( child, property );
		if( src == null ) throw new IllegalArgumentException( "Cannot find source field: " + property );

		// Lookup target field
		final Field target = getField( this, property );
		if( target == null ) throw new IllegalArgumentException( "Cannot find target field: " + property );

		// Register dependency
		add( child, src, target );
	}

	/**
	 * Retrieves all member fields for the given class.
	 * @param clazz Class
	 * @return Member fields ordered by name
	 */
	private static Map<String, Field> getFields( Class<?> clazz ) {
		final Map<String, Field> fields = new HashMap<>();
		for( Field f : ReflectionUtils.getMemberFields( clazz ) ) {
			fields.put( f.getName(), f );
		}
		return fields;
	}

	/**
	 * Retrieves the specified property.
	 * @param task			Task
	 * @param property		Property name
	 * @return Field
	 */
	private static Field getField( Task task, String property ) {
		final Map<String, Field> fields = getFields( task.getRunnable().getClass() );
		return fields.get( property );
	}

	/**
	 * Adds a dependency.
	 * @param child		Dependent task
	 * @param src		Source field
	 * @param target	Target field
	 */
	private void add( Task child, Field src, Field target ) {
		// Create dependency
		final Dependency dep = new Dependency();
		dep.src = src;
		dep.target = target;
		children.put( child, dep );

		// Attach listener for child state-changes
		child.add( listener );
	}

	@Override
	public void start() {
		// Init list of pending children
		if( children.isEmpty() ) throw new IllegalArgumentException( "No dependencies specified" );
		assert pending.isEmpty();
		pending.addAll( children.keySet() );

		// Start dependencies
		for( Task t : pending ) {
			t.start();
		}
	}

	@Override
	public void cancel() {
		// Cancel any pending or active sub-tasks
		for( Task t : pending ) {
			t.cancel();
		}
		pending.clear();

		// Delegate
		super.cancel();
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
