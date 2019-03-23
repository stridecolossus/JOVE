package org.sarge.jove.platform.openal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class OpenAudioLibraryTest {
	@Tag("openal")
	@Test
	public void create() {
		OpenAudioLibrary.create();
	}
}
