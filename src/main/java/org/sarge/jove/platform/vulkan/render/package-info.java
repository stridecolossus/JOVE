/**
 * The <i>render</i> package comprises the various Vulkan components used for rendering and presentation.
 * <p>
 * The {@link Swapchain} is the controller for the rendering and presentation process and contains a number of colour attachment images.
 * <p>
 * An {@link Attachment} specifies the format and structure or colour or depth-stencil images.
 * <p>
 * A {@link RenderPass} is comprised of a number of {@link Subpass} that manages the attachment(s) used during each stage of rendering.
 * <p>
 * A {@link Framebuffer} is the set of attachments that are the target of the render pass, usually a colour attachment and optionally the depth-stencil buffer.
 * <p>
 */
package org.sarge.jove.platform.vulkan.render;
