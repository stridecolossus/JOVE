package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.platform.vulkan.VkPipelineCreateFlag.DERIVATIVE;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.EnumMask;

import com.sun.jna.Pointer;

class PipelineTest {
	private Pipeline pipeline;
	private PipelineLayout layout;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		layout = new PipelineLayout(new Handle(1), dev, new PushConstant(List.of()));
		pipeline = new Pipeline(new Handle(2), dev, VkPipelineBindPoint.GRAPHICS, layout, false);
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
		final VulkanLibrary lib = dev.library();
		final Command cmd = pipeline.bind();
		final var buffer = new MockCommandBuffer();
		cmd.record(lib, buffer);
		verify(lib).vkCmdBindPipeline(buffer, VkPipelineBindPoint.GRAPHICS, pipeline);
	}

	@Test
	void destroy() {
		pipeline.destroy();
		verify(dev.library()).vkDestroyPipeline(dev, pipeline, null);
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
    			base = new Pipeline(new Handle(3), dev, VkPipelineBindPoint.GRAPHICS, layout, true);
			}

    		@DisplayName("A pipeline can be derived from an existing pipeline")
    		@Test
    		void derive() {
    			builder.derive(base);
    			builder.build(dev, layout, null);
    			verify(delegate).populate(EnumMask.of(DERIVATIVE), layout, base.handle(), -1, info);
    		}

    		@DisplayName("A pipeline cannot be derived from a pipeline that does not allow derivatives")
    		@Test
    		void invalid() {
    			final Pipeline invalid = new Pipeline(new Handle(3), dev, VkPipelineBindPoint.GRAPHICS, layout, false);
    			assertThrows(IllegalArgumentException.class, () -> builder.derive(invalid));
    		}

    		@Disabled
    		@DisplayName("A pipeline cannot be derived from a pipeline of a different type")
    		@Test
    		void type() {
    			// TODO - not checked for in code, is this actually a VK constraint?
    			final Pipeline invalid = new Pipeline(new Handle(3), dev, VkPipelineBindPoint.COMPUTE, layout, true);
    			assertThrows(IllegalArgumentException.class, () -> builder.derive(invalid));
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
    			verify(delegate).populate(EnumMask.of(DERIVATIVE), layout, null, 1, info);
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
			final var cache = new PipelineCache(new Handle(3), dev);
			builder.build(dev, layout, cache);
			verify(delegate).create(dev, cache, new VkComputePipelineCreateInfo[]{info}, new Pointer[1]);
		}
	}
}
