package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sarge.jove.scene.graph.Material;

public class MaterialTest {
	@Test
	void none() {
		assertThrows(IllegalStateException.class, () -> Material.NONE.queue());
	}
}
