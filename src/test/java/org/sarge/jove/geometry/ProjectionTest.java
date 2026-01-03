package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.control.Camera;
import org.sarge.jove.util.MathsUtility;

class ProjectionTest {
	private Viewport viewport;

	@BeforeEach
	public void before() {
		viewport = new Viewport(new Dimensions(640, 480));
	}

	@Test
	public void perspective() {
		final Matrix expected = new Matrix.Builder()
				.set(0, 0, 0.75f)
				.set(1, 1, 1)
				.set(2, 2, 1.001f)
				.set(2, 3, -0.1001f)
				.set(3, 2, 1)
				.build();

		final Projection projection = Projection.perspective(MathsUtility.HALF_PI);
//		assertEquals(1, proj.height(dim));
		assertEquals(expected, projection.matrix(viewport));
	}

	// should be:
	// near = <0, 0, 1, -0.1>
	// far  = <0, 0, -1, 100>

	@Test
	void frustum() {
		final Camera camera = new Camera();
		final Matrix matrix = Projection.perspective(MathsUtility.toRadians(90)).matrix(viewport).multiply(camera.matrix());
		System.out.println(matrix);
		System.out.println("----------");

		extract("left", matrix, 4, 1);
		subtract("right", matrix, 4, 1);

		extract("top", matrix, 4, 2);
		subtract("bottom", matrix, 4, 2);

		extract("near", matrix, 4, 3);
		subtract("far", matrix, 4, 3);
	}

	private static void extract(String name, Matrix matrix, int a, int b) {
		final float[] col = new float[4];
		for(int n = 0; n < 4; ++n) {
			col[n] = matrix.get(n, a-1) + matrix.get(n, b-1);
		}
		System.out.println(name + "\t" + Arrays.toString(col));
	}

	private static void subtract(String name, Matrix matrix, int a, int b) {
		final float[] col = new float[4];
		for(int n = 0; n < 4; ++n) {
			col[n] = matrix.get(n, a-1) - matrix.get(n, b-1);
		}
		System.out.println(name + "\t" + Arrays.toString(col));
	}

	//For performance, you perform frustum culling on the CPU to avoid even submitting invisible objects to the pipeline.
	//This is done by extracting six mathematical planes from the View-Projection (VP) matrix
	//
	//the left plane in Vulkan is often calculated as Row_{3}+Row_{0}, and the right as Row_{3}-Row_{0}.
	//
	//Because Vulkan's depth range is \([0,1]\), the "Near" plane extraction uses \(Row_{2}\) (for \(z=0\))
	//rather than the \(Row_{3}+Row_{2}\) (for \(z=-1\)) used in OpenGL.
	//
	//Left Plane: (R3 + R0) (vector from first row + last row of VP) dot P <= 0.
	//Right Plane: (R3 - R0) dot P <= 0.
	//Bottom Plane: (R3 + R1) dot P <= 0.
	//Top Plane: (R3 - R1) dot P <= 0.
	//Near Plane: (R3 + R2) dot P <= 0.
	//Far Plane: (R3 - R2) dot P <= 0

	@Test
	public void orthographic() {
		final Projection flat = Projection.FLAT;
//		assertEquals(480f, flat.height(dim), 0.0001f);
//		System.out.println(flat.matrix(1, 100, dim));
//		// TODO
//		final Matrix expected = new Matrix.Builder()
//			.identity()
//			.set(0, 0, 0.75f)
//			.set(2, 2, -1.0002f)
//			.set(2, 3, -0.20002f)
//			.set(3, 2, -1f)
//			.build();
//		assertEquals(expected, perspective.matrix(0.1f, 1000f, dim));
	}
}
