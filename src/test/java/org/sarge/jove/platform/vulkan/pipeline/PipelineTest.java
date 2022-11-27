package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.platform.vulkan.VkPipelineCreateFlag.DERIVATIVE;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;

class PipelineTest extends AbstractVulkanTest {
	private Pipeline pipeline;
	private PipelineLayout layout;

	@BeforeEach
	void before() {
		layout = mock(PipelineLayout.class);
		pipeline = new Pipeline(new Handle(1), dev, VkPipelineBindPoint.GRAPHICS, layout, false);
	}

	@Test
	void constructor() {
		assertEquals(layout, pipeline.layout());
		assertEquals(VkPipelineBindPoint.GRAPHICS, pipeline.type());
		assertEquals(false, pipeline.isAllowDerivatives());
		assertEquals(false, pipeline.isDestroyed());
	}

	@Test
	void bind() {
		final Command cmd = pipeline.bind();
		final Command.Buffer cb = mock(Command.Buffer.class);
		cmd.record(lib, cb);
		verify(lib).vkCmdBindPipeline(cb, VkPipelineBindPoint.GRAPHICS, pipeline);
	}

	@Test
	void destroy() {
		pipeline.destroy();
		verify(lib).vkDestroyPipeline(dev, pipeline, null);
	}

	@Nested
	class BuilderTests {
		private Pipeline.Builder<VkComputePipelineCreateInfo> builder;
		private DelegatePipelineBuilder<VkComputePipelineCreateInfo> delegate;
		private VkComputePipelineCreateInfo info;

		@SuppressWarnings("unchecked")
		@BeforeEach
		void before() {
			delegate = mock(DelegatePipelineBuilder.class);
			builder = new Pipeline.Builder<>(delegate);
			info = new VkComputePipelineCreateInfo();
			when(delegate.type()).thenReturn(VkPipelineBindPoint.COMPUTE);
			when(delegate.identity()).thenReturn(info);
		}

		@Nested
		class BasePipelineTests {
			private Pipeline base;

			@BeforeEach
			void before() {
    			base = mock(Pipeline.class);
    			when(base.handle()).thenReturn(new Handle(3));
    			when(base.isAllowDerivatives()).thenReturn(true);
			}

    		@DisplayName("A pipeline can be derived from an existing pipeline")
    		@Test
    		void derive() {
    			builder.derive(base);
    			builder.build(dev, layout, null);
    			verify(delegate).populate(BitMask.reduce(DERIVATIVE), layout, base.handle(), -1, info);
    		}

    		@DisplayName("A pipeline cannot be derived from a pipeline that does not allow derivatives")
    		@Test
    		void invalid() {
    			final Pipeline base = mock(Pipeline.class);
    			assertThrows(IllegalArgumentException.class, () -> builder.derive(base));
    		}

    		@DisplayName("A pipeline cannot be derived from a pipeline of a different type")
    		@Test
    		void type() {
    			final Pipeline base = mock(Pipeline.class);
    			assertThrows(IllegalArgumentException.class, () -> builder.derive(base));
    			// TODO
    		}

    		@DisplayName("A pipeline cannot be derived more than once")
    		@Test
    		void already() {
    			builder.derive(base);
    			assertThrows(IllegalArgumentException.class, () -> builder.derive(base));
    		}
		}

		@Nested
		class ArrayTests {
			private Pipeline.Builder<VkComputePipelineCreateInfo> peer;

			@BeforeEach
			void before() {
    			peer = new Pipeline.Builder<>(delegate);
			}

    		@DisplayName("Multiple pipelines can be instantiated in one operation")
			@Test
			void add() {
    			builder.add(peer);
				assertEquals(2, builder.build(dev, layout, null).size());
    		}

    		@DisplayName("A pipeline can be derived from a peer builder")
			@Test
			void derive() {
				peer.allowDerivatives();
				builder.derive(peer);
				assertEquals(2, builder.build(dev, layout, null).size());
    			verify(delegate).populate(BitMask.reduce(DERIVATIVE), layout, null, 1, info);
			}

    		@DisplayName("A pipeline cannot be derived from a peer that does not allow derivatives")
    		@Test
    		void invalid() {
    			assertThrows(IllegalArgumentException.class, () -> builder.derive(peer));
    		}

    		@DisplayName("A pipeline cannot be derived from itself")
    		@Test
    		void self() {
    			assertThrows(IllegalArgumentException.class, () -> builder.derive(builder));
    		}

    		@DisplayName("A pipeline cannot be derived more than once")
    		@Test
    		void already() {
				peer.allowDerivatives();
				builder.derive(peer);
    			assertThrows(IllegalArgumentException.class, () -> builder.derive(peer));
    		}
		}

		@Test
		void build() {
			final PipelineCache cache = mock(PipelineCache.class);
			builder.build(dev, layout, cache);
			verify(delegate).create(dev, cache, new VkComputePipelineCreateInfo[]{info}, new Pointer[1]);
		}
	}
}
