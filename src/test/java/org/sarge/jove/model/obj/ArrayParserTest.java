package org.sarge.jove.model.obj;

import static org.junit.Assert.assertEquals;

import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Point;

public class ArrayParserTest extends AbstractParserTest {
	class MockArrayParser extends ArrayParser<Point> {
		public MockArrayParser() {
			super(3, Point::new);
		}
		
		@Override
		protected void add(Point obj, ObjectModel model) {
			assertEquals(new Point(1, 2, 3), obj);
		}
	}
	
	private MockArrayParser parser;
	
	@Before
	public void before() {
		parser = new MockArrayParser();
	}
	
	@Test
	public void parse() {
		parser.parse(new Scanner("1 2 3"), model);
	}
}
