package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer.Recorder;

public class RenderSequenceTest {
	private Command cmd;
	private Recorder recorder;
	private RenderSequence seq;

	@BeforeEach
	void before() {
		cmd = mock(Command.class);
		recorder = mock(Recorder.class);
		seq = RenderSequence.of(List.of(cmd));
		assertNotNull(seq);
	}

	@Test
	void sequence() {
		seq.record(recorder);
		verify(recorder).add(cmd);
	}

	@Test
	void wrap() {
		final Command before = mock(Command.class);
		final Command after = mock(Command.class);
		final RenderSequence wrapper = seq.wrap(before, after);
		wrapper.record(recorder);
		verify(recorder).add(before);
		verify(recorder).add(cmd);
		verify(recorder).add(after);
	}

	@Test
	void compound() {
		final RenderSequence compound = RenderSequence.compound(List.of(seq, seq));
		compound.record(recorder);
		verify(recorder, times(2)).add(cmd);
	}
}
