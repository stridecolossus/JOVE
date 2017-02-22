package org.sarge.jove.model.obj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Vertex;
import org.sarge.lib.util.AbstractTest;

public class GroupTest extends AbstractTest {
	private Group group;
	private ObjectMaterial mat;
	
	@Before
	public void before() {
		group = new Group("group");
		mat = mock(ObjectMaterial.class);
	}
	
	@Test
	public void constructor() {
		assertEquals("group", group.getName());
		assertNotNull(group.getMaterial());
		assertFalse(group.getMaterial().isPresent());
	}
	
	@Test
	public void setMaterial() {
		group.setMaterial(mat);
		assertEquals(mat, group.getMaterial().get());
	}
	
	@Test
	public void setMaterialAlreadyAssigned() {
		group.setMaterial(mat);
		expect(IllegalArgumentException.class, "Material already set");
		group.setMaterial(mat);
	}
	
	@Test
	public void addVertex() {
		final Vertex v = new Vertex(Point.ORIGIN);
		group.add(v);
	}
}
