package org.sarge.jove.texture;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sarge.jove.texture.Sampler.Descriptor;
import org.sarge.jove.texture.Sampler.Descriptor.Border;
import org.sarge.jove.texture.Sampler.Descriptor.Filter;
import org.sarge.jove.texture.Sampler.Descriptor.Wrap;

public class SamplerTest {
	@Test
	public void build() {
		// Build descriptor
		final Descriptor descriptor = new Descriptor.Builder()
			.min(Filter.LINEAR)
			.mag(Filter.NEAREST)
			.anisotrophy(42)
			.wrap(Wrap.REPEAT)
			.mirrored()
			.border(Border.WHITE)
			.mipmap(Filter.LINEAR)
			.build();
		assertNotNull(descriptor);

		// Check filtering
		assertEquals(Filter.LINEAR, descriptor.min());
		assertEquals(Filter.NEAREST, descriptor.mag());
		assertEquals(42, descriptor.anisotrophy());

		// Check wrapping policy
		assertEquals(Wrap.REPEAT, descriptor.wrap());
		assertEquals(true, descriptor.isMirrored());
		assertEquals(Border.WHITE, descriptor.border());

		// check mip-map
		assertEquals(Filter.LINEAR, descriptor.mipmap());
	}

	@Test
	public void buildInvalidWrappingPolicy() {
		final Descriptor.Builder builder = new Descriptor.Builder().wrap(Wrap.BORDER).mirrored();
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}
}
