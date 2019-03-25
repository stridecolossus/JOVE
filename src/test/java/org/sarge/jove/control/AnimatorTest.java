package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Frame;
import org.sarge.jove.control.Animator;
import org.sarge.jove.control.Player;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.Rotation.MutableRotation;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

public class AnimatorTest {
	private Animator animator;
	private Animation animation;
	private Frame frame;

	@BeforeEach
	public void before() {
		// Create animation
		animation = mock(Animation.class);
		animator = new Animator(5000L, animation);

		// Init frame listener
		frame = mock(Frame.class);
		when(frame.elapsed()).thenReturn(1000L);

		// Start animation
		animator.apply(Player.State.PLAY);
	}

	@Test
	public void constructor() {
		assertEquals(0L, animator.time());
		assertEquals(5000L, animator.duration());
		assertEquals(1f, animator.speed(), 0.0001f);
	}

	@Test
	public void setSpeed() {
		animator.speed(2f);
		assertEquals(2f, animator.speed(), 0.0001f);
	}

	@Test
	public void update() {
		animator.update(frame);
		verify(animation).update(animator);
		assertEquals(1000L, animator.time());
	}

	@Test
	public void updateSpeed() {
		animator.speed(2f);
		animator.update(frame);
		assertEquals(2000L, animator.time());
	}

	@Test
	public void updateNotPlaying() {
		animator.apply(Player.State.STOP);
		animator.update(frame);
		verifyZeroInteractions(animation);
	}

	@Test
	public void updateFinished() {
		when(frame.elapsed()).thenReturn(7500L);
		animator.update(frame);
		verify(animation).update(animator);
		assertEquals(false, animator.isPlaying());
		assertEquals(5000L, animator.time());
	}

	@Test
	public void updateRepeating() {
		animator.setRepeating(true);
		when(frame.elapsed()).thenReturn(7500L);
		animator.update(frame);
		verify(animation).update(animator);
		assertEquals(true, animator.isPlaying());
		assertEquals(2500L, animator.time());
	}

	@Test
	public void rotation() {
		final MutableRotation rot = new MutableRotation(Vector.Y_AXIS, 0);
		animator = new Animator(5000L, Animator.rotation(rot));
		animator.apply(Player.State.PLAY);
		when(frame.elapsed()).thenReturn(2500L);
		animator.update(frame);
		assertEquals(MathsUtil.PI, rot.angle());
	}
}