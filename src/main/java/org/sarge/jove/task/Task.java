package org.sarge.jove.task;

/**
 * Task definition.
 * @author Sarge
 */
public interface Task {
	/**
	 * Task state.
	 */
	enum State {
		PENDING,
		QUEUED,
		RUNNING,
		FINISHED,
		FAILED,
		CANCELLED;

		/**
		 * @param next Next state
		 * @return Whether the given next state is a valid state-change from this state
		 */
		public boolean isValid( State next ) {
			switch( next ) {
			case PENDING:
				// Cannot set a task as pending
				return false;

			case QUEUED:
				// Moves from pending to queued
				return this == PENDING;

			case RUNNING:
				// From queued to running
				return this == QUEUED;

			case CANCELLED:
				// Can only cancel active tasks
				return ( this == PENDING ) || ( this == QUEUED ) || ( this == RUNNING );

			default:
				// Moves from running to done states
				return this == RUNNING;
			}
		}
	}

	/**
	 * Task state-change listener.
	 */
	public static interface Listener {
		/**
		 * Notifies a task state-change event.
		 * @param task		Changed task
		 * @param state		New state
		 */
		void notify( Task task, State state );
	}

	/**
	 * @return Current state of this task
	 */
	State getState();

	/**
	 * Starts this task.
	 */
	void start();

	/**
	 * Cancels this task.
	 */
	void cancel();
}
