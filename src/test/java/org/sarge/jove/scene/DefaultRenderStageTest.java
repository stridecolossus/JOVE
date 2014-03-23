package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.jove.scene.RenderStage.SortOrder;

public class DefaultRenderStageTest {
	@Test
	public void getSortOrder() {
		assertEquals( SortOrder.FRONT_TO_BACK, DefaultRenderStage.OPAQUE.getSortOrder() );
		assertEquals( SortOrder.NONE, DefaultRenderStage.SKY.getSortOrder() );
		assertEquals( SortOrder.BACK_TO_FRONT, DefaultRenderStage.TRANSLUCENT.getSortOrder() );
		assertEquals( SortOrder.BACK_TO_FRONT, DefaultRenderStage.POST_PROCESS.getSortOrder() );
	}
}
