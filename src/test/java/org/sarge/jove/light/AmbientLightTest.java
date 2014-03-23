package org.sarge.jove.light;

public class AmbientLightTest extends AbstractLightTest<AmbientLight> {
	@Override
	protected AmbientLight createLight() {
		return new AmbientLight();
	}

	@Override
	protected int getExpectedType() {
		return 1;
	}
}
