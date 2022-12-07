package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Playable.State;

class PlayableTest {
	@DisplayName("A playable that is stopped...")
	@Nested
	class Stopped {
		@DisplayName("can be started")
    	@Test
    	void play() {
    		assertEquals(true, State.STOP.isValidTransition(State.PLAY));
    	}

		@DisplayName("cannot be paused")
    	@Test
    	void pause() {
    		assertEquals(false, State.STOP.isValidTransition(State.PAUSE));
    	}

		@DisplayName("cannot be stopped")
    	@Test
    	void stop() {
    		assertEquals(false, State.STOP.isValidTransition(State.STOP));
    	}
	}

	@DisplayName("A playable that is playing...")
	@Nested
	class Playing {
		@DisplayName("cannot be played again")
    	@Test
    	void play() {
    		assertEquals(false, State.PLAY.isValidTransition(State.PLAY));
    	}

		@DisplayName("can be paused")
    	@Test
    	void pause() {
    		assertEquals(true, State.PLAY.isValidTransition(State.PAUSE));
    	}

		@DisplayName("can be stopped")
    	@Test
    	void stop() {
    		assertEquals(true, State.PLAY.isValidTransition(State.STOP));
    	}
	}

	@DisplayName("A playable that is paused...")
	@Nested
	class Paused {
		@DisplayName("can be resumed")
    	@Test
    	void resume() {
    		assertEquals(true, State.PAUSE.isValidTransition(State.PLAY));
    	}

		@DisplayName("cannot be paused again")
    	@Test
    	void pause() {
    		assertEquals(false, State.PAUSE.isValidTransition(State.PAUSE));
    	}

		@DisplayName("can be stopped")
    	@Test
    	void stop() {
    		assertEquals(true, State.PAUSE.isValidTransition(State.STOP));
    	}
	}
}
