package org.sarge.jove.scene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.material.Material;
import org.sarge.jove.material.RenderProperty;
import org.sarge.lib.util.Util;

public class RenderContextTest {
	private RenderContext ctx;
	private RenderingSystem sys;
	private Node parent;

	@Before
	public void before() {
		sys = mock( RenderingSystem.class );
		ctx = new RenderContext( sys );
		parent = mock( Node.class );
	}

	@Test
	public void constructor() {
		assertEquals( 0, ctx.getElapsed() );
		assertFloatEquals( 0, ctx.getFramesPerSecond() );
		assertEquals( sys, ctx.getRenderingSystem() );
		assertEquals( null, ctx.getScene() );
		assertNotNull( ctx.getStack() );
		assertEquals( true, ctx.getStack().isEmpty() );
	}

	@Ignore
	@Test
	public void update() {
		Util.kip( 1000 );
		ctx.update();
		assertTrue( ctx.getElapsed() > 0 );
		assertTrue( ctx.getFramesPerSecond() > 0 );
	}

	@Ignore // TODO
	@Test
	public void visit() {
		final Renderable mesh = mock( Renderable.class );
		when( parent.getRenderable() ).thenReturn( mesh );
		ctx.visit( parent );
		verify( parent ).getParent();
		verify( mesh ).render( ctx );
		assertEquals( 1, ctx.getStack().size() );
		assertEquals( parent, ctx.getStack().peek() );
	}

	@Ignore // TODO
	@Test
	public void visitNodeStack() {
		// Create a material
		final Material mat = mock( Material.class );
		final RenderProperty prop = mock( RenderProperty.class );
		when( mat.getRenderProperties() ).thenReturn( Collections.singletonMap( "prop", prop ) );

		// Visit parent and check material applied
		when( parent.getMaterial() ).thenReturn( mat );
		ctx.visit( parent );
		verify( mat ).apply( ctx );

		// Create another material with over-ridden property and a different property
		final Material other = mock( Material.class );
		final RenderProperty override = mock( RenderProperty.class );
		final RenderProperty another = mock( RenderProperty.class );
		final Map<String, RenderProperty> map = new HashMap<>();
		map.put( "prop", override );
		map.put( "another", another );
		when( other.getRenderProperties() ).thenReturn( map );

		// Visit a sibling
		final Node one = mock( Node.class );
		when( one.getMaterial() ).thenReturn( other );
		when( one.getParent() ).thenReturn( parent );
		ctx.visit( one );

		// Check added to stack
		assertEquals( 2, ctx.getStack().size() );
		assertEquals( one, ctx.getStack().get( 0 ) );
		assertEquals( parent, ctx.getStack().get( 1 ) );

		// Check properties
		verifyNoMoreInteractions( prop );
		verify( other ).apply( ctx );

		// Visit another sibling with no different material
		final Node two = mock( Node.class );
		when( two.getParent() ).thenReturn( parent );
		ctx.visit( two );

		// Check replaces sibling on stack
		assertEquals( 2, ctx.getStack().size() );
		assertEquals( two, ctx.getStack().get( 0 ) );
		assertEquals( parent, ctx.getStack().get( 1 ) );

		// Check over-ridden property restored and other property reset
		verify( prop ).apply( sys );
		verify( another ).reset( sys );
	}

	@Test
	public void reset() {
		// TODO
	}
}
