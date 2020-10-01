package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.VkShaderModuleCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.util.BufferFactory;
import org.sarge.jove.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>shader</i> is a reference to a Vulkan shader module.
 * @author Sarge
 */
public class Shader extends AbstractVulkanObject {
	/**
	 * Creates a shader module.
	 * @param dev		Parent device
	 * @param code		Shader code
	 * @return New shader
	 */
	public static Shader create(LogicalDevice dev, byte[] code) {
		// Buffer shader code
		final ByteBuffer buffer = BufferFactory.byteBuffer(code.length);
		buffer.put(code);
		buffer.flip();

		// Create descriptor
		final VkShaderModuleCreateInfo info = new VkShaderModuleCreateInfo();
		info.codeSize = code.length;
		info.pCode = buffer;

		// Allocate shader
		final VulkanLibrary lib = dev.library();
		final PointerByReference shader = lib.factory().pointer();
		check(lib.vkCreateShaderModule(dev.handle(), info, null, shader));

		// Create shader
		return new Shader(shader.getValue(), dev);
	}

	/**
	 * Constructor.
	 * @param handle 		Shader module handle
	 * @param dev			Device
	 */
	private Shader(Pointer handle, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroyShaderModule);
	}

	/**
	 * Loader for shader modules.
	 */
	public static class Loader {
		/**
		 * Creates a shader loader for the given directory.
		 * @param dir Directory
		 * @param dev Logical device
		 * @return New shader loader
		 */
		public static Loader create(Path dir, LogicalDevice dev) {
			Check.notNull(dir);
			final Function<String, byte[]> loader = filename -> load(dir, filename);
			return new Loader(loader, dev);
		}

		/**
		 * Creates a shader loader for the given directory.
		 * @param dir Directory name
		 * @param dev Logical device
		 * @return New shader loader
		 * @throws IllegalArgumentException if the directory does not exist
		 */
		public static Loader create(String dir, LogicalDevice dev) {
			final File file = new File(dir);
			if(!file.exists()) throw new IllegalArgumentException("Shader directory does not exist: " + dir);
			return create(file.toPath(), dev);
		}

		/**
		 * Helper - Loads shader code from the given file.
		 * @param dir			Directory path
		 * @param filename		Shader filename
		 * @return Shader code
		 * @throws ServiceException if the shader cannot be loaded
		 */
		private static byte[] load(Path dir, String filename) {
			final Path path = dir.resolve(filename);
			try(final var in = Files.newInputStream(path)) {
				return in.readAllBytes();
			}
			catch(IOException e) {
				throw new ServiceException("Error loading shader: " + path, e);
			}
		}

		private final Function<String, byte[]> loader;
		private final LogicalDevice dev;

		/**
		 * Constructor.
		 * @param loader 		Opens an input-stream for a given shader
		 * @param dev			Logical device
		 */
		public Loader(Function<String, byte[]> loader, LogicalDevice dev) {
			this.loader = notNull(loader);
			this.dev = notNull(dev);
		}

		/**
		 * Loads a shader module.
		 * @param filename Shader module filename
		 * @return New shader module
		 * @throws ServiceException if a shader cannot be loaded
		 */
		public Shader load(String filename) {
			return Shader.create(dev, loader.apply(filename));
		}
	}
}
