package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

public class LeafNodeTest {
	private LeafNode node;

	@BeforeEach
	void before() {
		node = new LeafNode();
	}

	@DisplayName("A leaf node cannot have an aggregated bounding volume")
	@Test
	void aggregate() {
		final Volume vol = new AggregateVolume(SphereVolume.class);
		assertThrows(IllegalArgumentException.class, () -> node.set(vol));
	}
}
