package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.scene.LocalMaterial.MaterialVisitor;

public class LocalMaterialTest {
	private LocalMaterial local;
	private Material mat;
	private MaterialVisitor visitor;

	@BeforeEach
	void before() {
		mat = mock(Material.class);
		visitor = new MaterialVisitor();
	}

	@Nested
	class Inherited {
		@BeforeEach
		void before() {
			local = new LocalMaterial();
		}

		@Test
		void constructor() {
			assertEquals(null, local.material());
		}

		@Test
		void inherit() {
			final var parent = new LocalMaterial(mat);
			visitor.update(parent);
			visitor.update(local);
			assertEquals(mat, local.material());
		}

		@Test
		void none() {
			visitor.update(local);
			assertEquals(null, local.material());
		}
	}

	@Nested
	class Local {
		@BeforeEach
		void before() {
			local = new LocalMaterial(mat);
		}

		@Test
		void constructor() {
			assertEquals(mat, local.material());
		}

		@Test
		void ignored() {
			visitor.update(new LocalMaterial(mock(Material.class)));
			visitor.update(local);
			assertEquals(mat, local.material());
		}

		@Test
		void none() {
			visitor.update(local);
			assertEquals(mat, local.material());
		}
	}
}
