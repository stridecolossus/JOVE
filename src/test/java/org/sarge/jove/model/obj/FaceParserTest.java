package org.sarge.jove.model.obj;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.TextureCoordinate;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex;

public class FaceParserTest extends AbstractParserTest {
	private FaceParser parser;

	@Before
	public void before() {
		parser = new FaceParser();
		when(model.getVertex(1)).thenReturn(new Point());
		when(model.getTextureCoord(2)).thenReturn(new TextureCoordinate());
		when(model.getNormal(3)).thenReturn(new Vector());
	}
	
	private void check(Vertex expected) {
		// Get added vertex
		final ArgumentCaptor<Vertex> captor = ArgumentCaptor.forClass(Vertex.class);
		verify(group).add(captor.capture());
		verifyNoMoreInteractions(group);
		
		// Check matches expected vertex
		final Vertex actual = captor.getValue();
		assertEquals(expected, actual);
	}

	@Test
	public void parseVertexOnly() {
		parser.parse(new Scanner("1"), model);
		check(new Vertex(new Point()));
	}

	@Test
	public void parseVertexNormal() {
		parser.parse(new Scanner("1//3"), model);
		check(new Vertex(new Point(), new Vector(), null, null));
	}

	@Test
	public void parseAll() {
		parser.parse(new Scanner("1/2/3"), model);
		check(new Vertex(new Point(), new Vector(), null, new TextureCoordinate()));
	}
}
