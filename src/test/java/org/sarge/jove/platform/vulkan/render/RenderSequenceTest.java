package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

public class RenderSequenceTest {
	private Command cmd;
	private Buffer buffer;
	private RenderSequence seq;

	@BeforeEach
	void before() {
		cmd = mock(Command.class);
		buffer = mock(Buffer.class);
		seq = RenderSequence.of(List.of(cmd));
		assertNotNull(seq);
	}

	@Test
	void sequence() {
		seq.record(buffer);
		verify(buffer).add(cmd);
	}

	@Test
	void wrap() {
		final Command before = mock(Command.class);
		final Command after = mock(Command.class);
		final RenderSequence wrapper = seq.wrap(before, after);
		wrapper.record(buffer);
		verify(buffer).add(before);
		verify(buffer).add(cmd);
		verify(buffer).add(after);
	}

	@Test
	void compound() {
		final RenderSequence compound = RenderSequence.compound(List.of(seq, seq));
		compound.record(buffer);
		verify(buffer, times(2)).add(cmd);
	}
}
