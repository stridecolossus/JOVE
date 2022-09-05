package org.sarge.jove.particle;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.MathsUtil;

@Disabled
public class BallisticTrajectoryTest {
	private BallisticTrajectory trajectory;
	private float g;

	@BeforeEach
	void before() {
		trajectory = new BallisticTrajectory(MathsUtil.toRadians(60), 5);
		g = 2;
	}

	@Test
	void start() {
		System.out.println(trajectory.position(0, g));
		System.out.println(trajectory.vector(0, g));
	}

	@Test
	void calc() {
		for(int n = 0; n < 10; ++n) {
			System.out.println(trajectory.position(n, g)+" "+trajectory.vector(n, g).normalize());
		}
	}
}
