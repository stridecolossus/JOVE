package org.sarge.jove.model.obj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.TextureCoordinate;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class ObjectModelTest {
	private ObjectModel model;
	
	@Before
	public void before() {
		model = new ObjectModel();
	}
	
	@Test
	public void constructor() {
		assertNotNull(model.getGroup());
		assertNull(model.getMaterial());
	}
	
	@Test
	public void newGroup() {
		final String name = "group";
		final Group group = model.newGroup(name);
		assertNotNull(group);
		assertEquals(name, group.getName());
		assertEquals(group, model.getGroup());
	}
	
	@Test
	public void newMaterial() {
		final String name = "mat";
		final ObjectMaterial mat = model.newMaterial(name);
		assertNotNull(mat);
		assertEquals(mat, model.getMaterial());
	}
	
	@Test
	public void addVertex() {
		model.add(Point.ORIGIN);
		assertEquals(Point.ORIGIN, model.getVertex(1));
	}

	@Test
	public void addNormal() {
		model.add(Vector.X_AXIS);
		assertEquals(Vector.X_AXIS, model.getNormal(1));
	}
	
	@Test
	public void addTextureCoordinate() {
		model.add(TextureCoordinate.TOP_LEFT);
		assertEquals(TextureCoordinate.TOP_LEFT, model.getTextureCoord(1));
	}
}
