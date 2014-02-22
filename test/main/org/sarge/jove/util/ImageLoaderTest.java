package org.sarge.jove.util;

import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sarge.lib.io.ClasspathDataSource;

public class ImageLoaderTest {
	private ImageLoader loader;

	@Before
	public void before() {
		loader = new ImageLoader( new ClasspathDataSource( ImageLoaderTest.class ) );
	}

	@Test
	public void load() throws IOException {
		final BufferedImage image = loader.load( "thiswayup.jpg" );
		assertNotNull( image );
	}
}
