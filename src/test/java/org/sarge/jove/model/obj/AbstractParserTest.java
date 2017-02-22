package org.sarge.jove.model.obj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;

public abstract class AbstractParserTest {
	protected ObjectModel model;
	protected Group group;
	protected ObjectMaterial mat;
	
	@Before
	public void abstractBefore() {
		// Create OBJ model
		model = mock(ObjectModel.class);
		
		// Add active group
		group = mock(Group.class);
		when(model.getGroup()).thenReturn(group);
		
		// Add active material
		mat = mock(ObjectMaterial.class);
		when(model.getMaterial()).thenReturn(mat);
	}
}
