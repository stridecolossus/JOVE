/**
 * Vulkan device memory framework.
 *
 * memory overview
 *
 * example
 * - load mem types
 * - selector
 * - allocator
 * - service
 * - init props
 * - allocate
 *
 * pooled allocator
 * routing
 *
 * JDK18 snippets
 *
 *
 * {@snippet lang="java" :
 * Allocator allocator = new DefaultAllocator(dev); // @highlight substring="allocator"
 * DeviceMemory = ...
 * }
 *
 * https://docs.oracle.com/en/java/javase/18/code-snippet/index.html
 * https://www.baeldung.com/java-doc-code-snippets
 *
 */
package org.sarge.jove.platform.vulkan.memory;
