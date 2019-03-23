package org.sarge.jove.texture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.texture.Image.Format;
import org.sarge.jove.texture.Texture.Descriptor;
import org.sarge.jove.texture.Texture.Descriptor.Filter;
import org.sarge.jove.texture.Texture.Descriptor.Wrap;

public class TextureTest {
	private Texture texture;
	private Descriptor descriptor;

	@BeforeEach
	public void before() {
		descriptor = new Descriptor.Builder()
			.header(new Image.Header(Format.RGBA, new Dimensions(3, 4)))
			.wrap(Wrap.CLAMP)
			.levels(5)
			.min(Filter.LINEAR)
			.mag(Filter.NEAREST)
			.mipmap(Filter.LINEAR)
			.build();
		texture = new Texture(descriptor);
	}

	@Test
	public void constructor() {
		assertEquals(descriptor, texture.descriptor());
	}

	@Test
	public void descriptor() {
		assertEquals(new Image.Header(Format.RGBA, new Dimensions(3, 4)), descriptor.header());
		assertEquals(2, descriptor.dimensions());
		assertEquals(Wrap.CLAMP, descriptor.wrap());
		assertEquals(5, descriptor.levels());
		assertEquals(Filter.LINEAR, descriptor.min());
		assertEquals(Filter.NEAREST, descriptor.mag());
		assertEquals(Filter.LINEAR, descriptor.mipmap());
	}

	@Test
	public void levels() {
		assertEquals(1, Texture.levels(new Dimensions(1, 1)));
		assertEquals(2, Texture.levels(new Dimensions(2, 2)));
		assertEquals(5, Texture.levels(new Dimensions(16, 16)));
		assertEquals(5, Texture.levels(new Dimensions(16, 1)));
		assertEquals(5, Texture.levels(new Dimensions(16, 31)));
	}

	@Test
	public void defaultLevels() {
		descriptor = new Descriptor.Builder().header(descriptor.header()).build();
		assertEquals(3, descriptor.levels());
	}
}
