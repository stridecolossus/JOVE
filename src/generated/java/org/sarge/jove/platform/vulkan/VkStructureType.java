package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkStructureType implements IntEnum {
 	APPLICATION_INFO(0),
 	INSTANCE_CREATE_INFO(1),
 	DEVICE_QUEUE_CREATE_INFO(2),
 	DEVICE_CREATE_INFO(3),
 	SUBMIT_INFO(4),
 	MEMORY_ALLOCATE_INFO(5),
 	MAPPED_MEMORY_RANGE(6),
 	BIND_SPARSE_INFO(7),
 	FENCE_CREATE_INFO(8),
 	SEMAPHORE_CREATE_INFO(9),
 	EVENT_CREATE_INFO(10),
 	QUERY_POOL_CREATE_INFO(11),
 	BUFFER_CREATE_INFO(12),
 	BUFFER_VIEW_CREATE_INFO(13),
 	IMAGE_CREATE_INFO(14),
 	IMAGE_VIEW_CREATE_INFO(15),
 	SHADER_MODULE_CREATE_INFO(16),
 	PIPELINE_CACHE_CREATE_INFO(17),
 	PIPELINE_SHADER_STAGE_CREATE_INFO(18),
 	PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO(19),
 	PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO(20),
 	PIPELINE_TESSELLATION_STATE_CREATE_INFO(21),
 	PIPELINE_VIEWPORT_STATE_CREATE_INFO(22),
 	PIPELINE_RASTERIZATION_STATE_CREATE_INFO(23),
 	PIPELINE_MULTISAMPLE_STATE_CREATE_INFO(24),
 	PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO(25),
 	PIPELINE_COLOR_BLEND_STATE_CREATE_INFO(26),
 	PIPELINE_DYNAMIC_STATE_CREATE_INFO(27),
 	GRAPHICS_PIPELINE_CREATE_INFO(28),
 	COMPUTE_PIPELINE_CREATE_INFO(29),
 	PIPELINE_LAYOUT_CREATE_INFO(30),
 	SAMPLER_CREATE_INFO(31),
 	DESCRIPTOR_SET_LAYOUT_CREATE_INFO(32),
 	DESCRIPTOR_POOL_CREATE_INFO(33),
 	DESCRIPTOR_SET_ALLOCATE_INFO(34),
 	WRITE_DESCRIPTOR_SET(35),
 	COPY_DESCRIPTOR_SET(36),
 	FRAMEBUFFER_CREATE_INFO(37),
 	RENDER_PASS_CREATE_INFO(38),
 	COMMAND_POOL_CREATE_INFO(39),
 	COMMAND_BUFFER_ALLOCATE_INFO(40),
 	COMMAND_BUFFER_INHERITANCE_INFO(41),
 	COMMAND_BUFFER_BEGIN_INFO(42),
 	RENDER_PASS_BEGIN_INFO(43),
 	BUFFER_MEMORY_BARRIER(44),
 	IMAGE_MEMORY_BARRIER(45),
 	MEMORY_BARRIER(46),
 	LOADER_INSTANCE_CREATE_INFO(47),
 	LOADER_DEVICE_CREATE_INFO(48),
 	PHYSICAL_DEVICE_SUBGROUP_PROPERTIES(1000094000),
 	BIND_BUFFER_MEMORY_INFO(1000157000),
 	BIND_IMAGE_MEMORY_INFO(1000157001),
 	PHYSICAL_DEVICE_16BIT_STORAGE_FEATURES(1000083000),
 	MEMORY_DEDICATED_REQUIREMENTS(1000127000),
 	MEMORY_DEDICATED_ALLOCATE_INFO(1000127001),
 	MEMORY_ALLOCATE_FLAGS_INFO(1000060000),
 	DEVICE_GROUP_RENDER_PASS_BEGIN_INFO(1000060003),
 	DEVICE_GROUP_COMMAND_BUFFER_BEGIN_INFO(1000060004),
 	DEVICE_GROUP_SUBMIT_INFO(1000060005),
 	DEVICE_GROUP_BIND_SPARSE_INFO(1000060006),
 	BIND_BUFFER_MEMORY_DEVICE_GROUP_INFO(1000060013),
 	BIND_IMAGE_MEMORY_DEVICE_GROUP_INFO(1000060014),
 	PHYSICAL_DEVICE_GROUP_PROPERTIES(1000070000),
 	DEVICE_GROUP_DEVICE_CREATE_INFO(1000070001),
 	BUFFER_MEMORY_REQUIREMENTS_INFO_2(1000146000),
 	IMAGE_MEMORY_REQUIREMENTS_INFO_2(1000146001),
 	IMAGE_SPARSE_MEMORY_REQUIREMENTS_INFO_2(1000146002),
 	MEMORY_REQUIREMENTS_2(1000146003),
 	SPARSE_IMAGE_MEMORY_REQUIREMENTS_2(1000146004),
 	PHYSICAL_DEVICE_FEATURES_2(1000059000),
 	PHYSICAL_DEVICE_PROPERTIES_2(1000059001),
 	FORMAT_PROPERTIES_2(1000059002),
 	IMAGE_FORMAT_PROPERTIES_2(1000059003),
 	PHYSICAL_DEVICE_IMAGE_FORMAT_INFO_2(1000059004),
 	QUEUE_FAMILY_PROPERTIES_2(1000059005),
 	PHYSICAL_DEVICE_MEMORY_PROPERTIES_2(1000059006),
 	SPARSE_IMAGE_FORMAT_PROPERTIES_2(1000059007),
 	PHYSICAL_DEVICE_SPARSE_IMAGE_FORMAT_INFO_2(1000059008),
 	PHYSICAL_DEVICE_POINT_CLIPPING_PROPERTIES(1000117000),
 	RENDER_PASS_INPUT_ATTACHMENT_ASPECT_CREATE_INFO(1000117001),
 	IMAGE_VIEW_USAGE_CREATE_INFO(1000117002),
 	PIPELINE_TESSELLATION_DOMAIN_ORIGIN_STATE_CREATE_INFO(1000117003),
 	RENDER_PASS_MULTIVIEW_CREATE_INFO(1000053000),
 	PHYSICAL_DEVICE_MULTIVIEW_FEATURES(1000053001),
 	PHYSICAL_DEVICE_MULTIVIEW_PROPERTIES(1000053002),
 	PHYSICAL_DEVICE_VARIABLE_POINTER_FEATURES(1000120000),
 	PROTECTED_SUBMIT_INFO(1000145000),
 	PHYSICAL_DEVICE_PROTECTED_MEMORY_FEATURES(1000145001),
 	PHYSICAL_DEVICE_PROTECTED_MEMORY_PROPERTIES(1000145002),
 	DEVICE_QUEUE_INFO_2(1000145003),
 	SAMPLER_YCBCR_CONVERSION_CREATE_INFO(1000156000),
 	SAMPLER_YCBCR_CONVERSION_INFO(1000156001),
 	BIND_IMAGE_PLANE_MEMORY_INFO(1000156002),
 	IMAGE_PLANE_MEMORY_REQUIREMENTS_INFO(1000156003),
 	PHYSICAL_DEVICE_SAMPLER_YCBCR_CONVERSION_FEATURES(1000156004),
 	SAMPLER_YCBCR_CONVERSION_IMAGE_FORMAT_PROPERTIES(1000156005),
 	DESCRIPTOR_UPDATE_TEMPLATE_CREATE_INFO(1000085000),
 	PHYSICAL_DEVICE_EXTERNAL_IMAGE_FORMAT_INFO(1000071000),
 	EXTERNAL_IMAGE_FORMAT_PROPERTIES(1000071001),
 	PHYSICAL_DEVICE_EXTERNAL_BUFFER_INFO(1000071002),
 	EXTERNAL_BUFFER_PROPERTIES(1000071003),
 	PHYSICAL_DEVICE_ID_PROPERTIES(1000071004),
 	EXTERNAL_MEMORY_BUFFER_CREATE_INFO(1000072000),
 	EXTERNAL_MEMORY_IMAGE_CREATE_INFO(1000072001),
 	EXPORT_MEMORY_ALLOCATE_INFO(1000072002),
 	PHYSICAL_DEVICE_EXTERNAL_FENCE_INFO(1000112000),
 	EXTERNAL_FENCE_PROPERTIES(1000112001),
 	EXPORT_FENCE_CREATE_INFO(1000113000),
 	EXPORT_SEMAPHORE_CREATE_INFO(1000077000),
 	PHYSICAL_DEVICE_EXTERNAL_SEMAPHORE_INFO(1000076000),
 	EXTERNAL_SEMAPHORE_PROPERTIES(1000076001),
 	PHYSICAL_DEVICE_MAINTENANCE_3_PROPERTIES(1000168000),
 	DESCRIPTOR_SET_LAYOUT_SUPPORT(1000168001),
 	PHYSICAL_DEVICE_SHADER_DRAW_PARAMETER_FEATURES(1000063000),
 	SWAPCHAIN_CREATE_INFO_KHR(1000001000),
 	PRESENT_INFO_KHR(1000001001),
 	DEVICE_GROUP_PRESENT_CAPABILITIES_KHR(1000060007),
 	IMAGE_SWAPCHAIN_CREATE_INFO_KHR(1000060008),
 	BIND_IMAGE_MEMORY_SWAPCHAIN_INFO_KHR(1000060009),
 	ACQUIRE_NEXT_IMAGE_INFO_KHR(1000060010),
 	DEVICE_GROUP_PRESENT_INFO_KHR(1000060011),
 	DEVICE_GROUP_SWAPCHAIN_CREATE_INFO_KHR(1000060012),
 	DISPLAY_MODE_CREATE_INFO_KHR(1000002000),
 	DISPLAY_SURFACE_CREATE_INFO_KHR(1000002001),
 	DISPLAY_PRESENT_INFO_KHR(1000003000),
 	XLIB_SURFACE_CREATE_INFO_KHR(1000004000),
 	XCB_SURFACE_CREATE_INFO_KHR(1000005000),
 	WAYLAND_SURFACE_CREATE_INFO_KHR(1000006000),
 	ANDROID_SURFACE_CREATE_INFO_KHR(1000008000),
 	WIN32_SURFACE_CREATE_INFO_KHR(1000009000),
 	DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT(1000011000),
 	PIPELINE_RASTERIZATION_STATE_RASTERIZATION_ORDER_AMD(1000018000),
 	DEBUG_MARKER_OBJECT_NAME_INFO_EXT(1000022000),
 	DEBUG_MARKER_OBJECT_TAG_INFO_EXT(1000022001),
 	DEBUG_MARKER_MARKER_INFO_EXT(1000022002),
 	DEDICATED_ALLOCATION_IMAGE_CREATE_INFO_NV(1000026000),
 	DEDICATED_ALLOCATION_BUFFER_CREATE_INFO_NV(1000026001),
 	DEDICATED_ALLOCATION_MEMORY_ALLOCATE_INFO_NV(1000026002),
 	PHYSICAL_DEVICE_TRANSFORM_FEEDBACK_FEATURES_EXT(1000028000),
 	PHYSICAL_DEVICE_TRANSFORM_FEEDBACK_PROPERTIES_EXT(1000028001),
 	PIPELINE_RASTERIZATION_STATE_STREAM_CREATE_INFO_EXT(1000028002),
 	TEXTURE_LOD_GATHER_FORMAT_PROPERTIES_AMD(1000041000),
 	PHYSICAL_DEVICE_CORNER_SAMPLED_IMAGE_FEATURES_NV(1000050000),
 	EXTERNAL_MEMORY_IMAGE_CREATE_INFO_NV(1000056000),
 	EXPORT_MEMORY_ALLOCATE_INFO_NV(1000056001),
 	IMPORT_MEMORY_WIN32_HANDLE_INFO_NV(1000057000),
 	EXPORT_MEMORY_WIN32_HANDLE_INFO_NV(1000057001),
 	WIN32_KEYED_MUTEX_ACQUIRE_RELEASE_INFO_NV(1000058000),
 	VALIDATION_FLAGS_EXT(1000061000),
 	VI_SURFACE_CREATE_INFO_NN(1000062000),
 	IMAGE_VIEW_ASTC_DECODE_MODE_EXT(1000067000),
 	PHYSICAL_DEVICE_ASTC_DECODE_FEATURES_EXT(1000067001),
 	IMPORT_MEMORY_WIN32_HANDLE_INFO_KHR(1000073000),
 	EXPORT_MEMORY_WIN32_HANDLE_INFO_KHR(1000073001),
 	MEMORY_WIN32_HANDLE_PROPERTIES_KHR(1000073002),
 	MEMORY_GET_WIN32_HANDLE_INFO_KHR(1000073003),
 	IMPORT_MEMORY_FD_INFO_KHR(1000074000),
 	MEMORY_FD_PROPERTIES_KHR(1000074001),
 	MEMORY_GET_FD_INFO_KHR(1000074002),
 	WIN32_KEYED_MUTEX_ACQUIRE_RELEASE_INFO_KHR(1000075000),
 	IMPORT_SEMAPHORE_WIN32_HANDLE_INFO_KHR(1000078000),
 	EXPORT_SEMAPHORE_WIN32_HANDLE_INFO_KHR(1000078001),
 	D3D12_FENCE_SUBMIT_INFO_KHR(1000078002),
 	SEMAPHORE_GET_WIN32_HANDLE_INFO_KHR(1000078003),
 	IMPORT_SEMAPHORE_FD_INFO_KHR(1000079000),
 	SEMAPHORE_GET_FD_INFO_KHR(1000079001),
 	PHYSICAL_DEVICE_PUSH_DESCRIPTOR_PROPERTIES_KHR(1000080000),
 	COMMAND_BUFFER_INHERITANCE_CONDITIONAL_RENDERING_INFO_EXT(1000081000),
 	PHYSICAL_DEVICE_CONDITIONAL_RENDERING_FEATURES_EXT(1000081001),
 	CONDITIONAL_RENDERING_BEGIN_INFO_EXT(1000081002),
 	PHYSICAL_DEVICE_FLOAT16_INT8_FEATURES_KHR(1000082000),
 	PRESENT_REGIONS_KHR(1000084000),
 	OBJECT_TABLE_CREATE_INFO_NVX(1000086000),
 	INDIRECT_COMMANDS_LAYOUT_CREATE_INFO_NVX(1000086001),
 	CMD_PROCESS_COMMANDS_INFO_NVX(1000086002),
 	CMD_RESERVE_SPACE_FOR_COMMANDS_INFO_NVX(1000086003),
 	DEVICE_GENERATED_COMMANDS_LIMITS_NVX(1000086004),
 	DEVICE_GENERATED_COMMANDS_FEATURES_NVX(1000086005),
 	PIPELINE_VIEWPORT_W_SCALING_STATE_CREATE_INFO_NV(1000087000),
 	SURFACE_CAPABILITIES_2_EXT(1000090000),
 	DISPLAY_POWER_INFO_EXT(1000091000),
 	DEVICE_EVENT_INFO_EXT(1000091001),
 	DISPLAY_EVENT_INFO_EXT(1000091002),
 	SWAPCHAIN_COUNTER_CREATE_INFO_EXT(1000091003),
 	PRESENT_TIMES_INFO_GOOGLE(1000092000),
 	PHYSICAL_DEVICE_MULTIVIEW_PER_VIEW_ATTRIBUTES_PROPERTIES_NVX(1000097000),
 	PIPELINE_VIEWPORT_SWIZZLE_STATE_CREATE_INFO_NV(1000098000),
 	PHYSICAL_DEVICE_DISCARD_RECTANGLE_PROPERTIES_EXT(1000099000),
 	PIPELINE_DISCARD_RECTANGLE_STATE_CREATE_INFO_EXT(1000099001),
 	PHYSICAL_DEVICE_CONSERVATIVE_RASTERIZATION_PROPERTIES_EXT(1000101000),
 	PIPELINE_RASTERIZATION_CONSERVATIVE_STATE_CREATE_INFO_EXT(1000101001),
 	PHYSICAL_DEVICE_DEPTH_CLIP_ENABLE_FEATURES_EXT(1000102000),
 	PIPELINE_RASTERIZATION_DEPTH_CLIP_STATE_CREATE_INFO_EXT(1000102001),
 	HDR_METADATA_EXT(1000105000),
 	ATTACHMENT_DESCRIPTION_2_KHR(1000109000),
 	ATTACHMENT_REFERENCE_2_KHR(1000109001),
 	SUBPASS_DESCRIPTION_2_KHR(1000109002),
 	SUBPASS_DEPENDENCY_2_KHR(1000109003),
 	RENDER_PASS_CREATE_INFO_2_KHR(1000109004),
 	SUBPASS_BEGIN_INFO_KHR(1000109005),
 	SUBPASS_END_INFO_KHR(1000109006),
 	SHARED_PRESENT_SURFACE_CAPABILITIES_KHR(1000111000),
 	IMPORT_FENCE_WIN32_HANDLE_INFO_KHR(1000114000),
 	EXPORT_FENCE_WIN32_HANDLE_INFO_KHR(1000114001),
 	FENCE_GET_WIN32_HANDLE_INFO_KHR(1000114002),
 	IMPORT_FENCE_FD_INFO_KHR(1000115000),
 	FENCE_GET_FD_INFO_KHR(1000115001),
 	PHYSICAL_DEVICE_SURFACE_INFO_2_KHR(1000119000),
 	SURFACE_CAPABILITIES_2_KHR(1000119001),
 	SURFACE_FORMAT_2_KHR(1000119002),
 	DISPLAY_PROPERTIES_2_KHR(1000121000),
 	DISPLAY_PLANE_PROPERTIES_2_KHR(1000121001),
 	DISPLAY_MODE_PROPERTIES_2_KHR(1000121002),
 	DISPLAY_PLANE_INFO_2_KHR(1000121003),
 	DISPLAY_PLANE_CAPABILITIES_2_KHR(1000121004),
 	IOS_SURFACE_CREATE_INFO_MVK(1000122000),
 	MACOS_SURFACE_CREATE_INFO_MVK(1000123000),
 	DEBUG_UTILS_OBJECT_NAME_INFO_EXT(1000128000),
 	DEBUG_UTILS_OBJECT_TAG_INFO_EXT(1000128001),
 	DEBUG_UTILS_LABEL_EXT(1000128002),
 	DEBUG_UTILS_MESSENGER_CALLBACK_DATA_EXT(1000128003),
 	DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT(1000128004),
 	ANDROID_HARDWARE_BUFFER_USAGE_ANDROID(1000129000),
 	ANDROID_HARDWARE_BUFFER_PROPERTIES_ANDROID(1000129001),
 	ANDROID_HARDWARE_BUFFER_FORMAT_PROPERTIES_ANDROID(1000129002),
 	IMPORT_ANDROID_HARDWARE_BUFFER_INFO_ANDROID(1000129003),
 	MEMORY_GET_ANDROID_HARDWARE_BUFFER_INFO_ANDROID(1000129004),
 	EXTERNAL_FORMAT_ANDROID(1000129005),
 	PHYSICAL_DEVICE_SAMPLER_FILTER_MINMAX_PROPERTIES_EXT(1000130000),
 	SAMPLER_REDUCTION_MODE_CREATE_INFO_EXT(1000130001),
 	PHYSICAL_DEVICE_INLINE_UNIFORM_BLOCK_FEATURES_EXT(1000138000),
 	PHYSICAL_DEVICE_INLINE_UNIFORM_BLOCK_PROPERTIES_EXT(1000138001),
 	WRITE_DESCRIPTOR_SET_INLINE_UNIFORM_BLOCK_EXT(1000138002),
 	DESCRIPTOR_POOL_INLINE_UNIFORM_BLOCK_CREATE_INFO_EXT(1000138003),
 	SAMPLE_LOCATIONS_INFO_EXT(1000143000),
 	RENDER_PASS_SAMPLE_LOCATIONS_BEGIN_INFO_EXT(1000143001),
 	PIPELINE_SAMPLE_LOCATIONS_STATE_CREATE_INFO_EXT(1000143002),
 	PHYSICAL_DEVICE_SAMPLE_LOCATIONS_PROPERTIES_EXT(1000143003),
 	MULTISAMPLE_PROPERTIES_EXT(1000143004),
 	IMAGE_FORMAT_LIST_CREATE_INFO_KHR(1000147000),
 	PHYSICAL_DEVICE_BLEND_OPERATION_ADVANCED_FEATURES_EXT(1000148000),
 	PHYSICAL_DEVICE_BLEND_OPERATION_ADVANCED_PROPERTIES_EXT(1000148001),
 	PIPELINE_COLOR_BLEND_ADVANCED_STATE_CREATE_INFO_EXT(1000148002),
 	PIPELINE_COVERAGE_TO_COLOR_STATE_CREATE_INFO_NV(1000149000),
 	PIPELINE_COVERAGE_MODULATION_STATE_CREATE_INFO_NV(1000152000),
 	DRM_FORMAT_MODIFIER_PROPERTIES_LIST_EXT(1000158000),
 	DRM_FORMAT_MODIFIER_PROPERTIES_EXT(1000158001),
 	PHYSICAL_DEVICE_IMAGE_DRM_FORMAT_MODIFIER_INFO_EXT(1000158002),
 	IMAGE_DRM_FORMAT_MODIFIER_LIST_CREATE_INFO_EXT(1000158003),
 	IMAGE_DRM_FORMAT_MODIFIER_EXPLICIT_CREATE_INFO_EXT(1000158004),
 	IMAGE_DRM_FORMAT_MODIFIER_PROPERTIES_EXT(1000158005),
 	VALIDATION_CACHE_CREATE_INFO_EXT(1000160000),
 	SHADER_MODULE_VALIDATION_CACHE_CREATE_INFO_EXT(1000160001),
 	DESCRIPTOR_SET_LAYOUT_BINDING_FLAGS_CREATE_INFO_EXT(1000161000),
 	PHYSICAL_DEVICE_DESCRIPTOR_INDEXING_FEATURES_EXT(1000161001),
 	PHYSICAL_DEVICE_DESCRIPTOR_INDEXING_PROPERTIES_EXT(1000161002),
 	DESCRIPTOR_SET_VARIABLE_DESCRIPTOR_COUNT_ALLOCATE_INFO_EXT(1000161003),
 	DESCRIPTOR_SET_VARIABLE_DESCRIPTOR_COUNT_LAYOUT_SUPPORT_EXT(1000161004),
 	PIPELINE_VIEWPORT_SHADING_RATE_IMAGE_STATE_CREATE_INFO_NV(1000164000),
 	PHYSICAL_DEVICE_SHADING_RATE_IMAGE_FEATURES_NV(1000164001),
 	PHYSICAL_DEVICE_SHADING_RATE_IMAGE_PROPERTIES_NV(1000164002),
 	PIPELINE_VIEWPORT_COARSE_SAMPLE_ORDER_STATE_CREATE_INFO_NV(1000164005),
 	RAY_TRACING_PIPELINE_CREATE_INFO_NV(1000165000),
 	ACCELERATION_STRUCTURE_CREATE_INFO_NV(1000165001),
 	GEOMETRY_NV(1000165003),
 	GEOMETRY_TRIANGLES_NV(1000165004),
 	GEOMETRY_AABB_NV(1000165005),
 	BIND_ACCELERATION_STRUCTURE_MEMORY_INFO_NV(1000165006),
 	WRITE_DESCRIPTOR_SET_ACCELERATION_STRUCTURE_NV(1000165007),
 	ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_INFO_NV(1000165008),
 	PHYSICAL_DEVICE_RAY_TRACING_PROPERTIES_NV(1000165009),
 	RAY_TRACING_SHADER_GROUP_CREATE_INFO_NV(1000165011),
 	ACCELERATION_STRUCTURE_INFO_NV(1000165012),
 	PHYSICAL_DEVICE_REPRESENTATIVE_FRAGMENT_TEST_FEATURES_NV(1000166000),
 	PIPELINE_REPRESENTATIVE_FRAGMENT_TEST_STATE_CREATE_INFO_NV(1000166001),
 	PHYSICAL_DEVICE_IMAGE_VIEW_IMAGE_FORMAT_INFO_EXT(1000170000),
 	FILTER_CUBIC_IMAGE_VIEW_IMAGE_FORMAT_PROPERTIES_EXT(1000170001),
 	DEVICE_QUEUE_GLOBAL_PRIORITY_CREATE_INFO_EXT(1000174000),
 	PHYSICAL_DEVICE_8BIT_STORAGE_FEATURES_KHR(1000177000),
 	IMPORT_MEMORY_HOST_POINTER_INFO_EXT(1000178000),
 	MEMORY_HOST_POINTER_PROPERTIES_EXT(1000178001),
 	PHYSICAL_DEVICE_EXTERNAL_MEMORY_HOST_PROPERTIES_EXT(1000178002),
 	PHYSICAL_DEVICE_SHADER_ATOMIC_INT64_FEATURES_KHR(1000180000),
 	CALIBRATED_TIMESTAMP_INFO_EXT(1000184000),
 	PHYSICAL_DEVICE_SHADER_CORE_PROPERTIES_AMD(1000185000),
 	DEVICE_MEMORY_OVERALLOCATION_CREATE_INFO_AMD(1000189000),
 	PHYSICAL_DEVICE_VERTEX_ATTRIBUTE_DIVISOR_PROPERTIES_EXT(1000190000),
 	PIPELINE_VERTEX_INPUT_DIVISOR_STATE_CREATE_INFO_EXT(1000190001),
 	PHYSICAL_DEVICE_VERTEX_ATTRIBUTE_DIVISOR_FEATURES_EXT(1000190002),
 	PHYSICAL_DEVICE_DRIVER_PROPERTIES_KHR(1000196000),
 	PHYSICAL_DEVICE_FLOAT_CONTROLS_PROPERTIES_KHR(1000197000),
 	PHYSICAL_DEVICE_DEPTH_STENCIL_RESOLVE_PROPERTIES_KHR(1000199000),
 	SUBPASS_DESCRIPTION_DEPTH_STENCIL_RESOLVE_KHR(1000199001),
 	PHYSICAL_DEVICE_COMPUTE_SHADER_DERIVATIVES_FEATURES_NV(1000201000),
 	PHYSICAL_DEVICE_MESH_SHADER_FEATURES_NV(1000202000),
 	PHYSICAL_DEVICE_MESH_SHADER_PROPERTIES_NV(1000202001),
 	PHYSICAL_DEVICE_FRAGMENT_SHADER_BARYCENTRIC_FEATURES_NV(1000203000),
 	PHYSICAL_DEVICE_SHADER_IMAGE_FOOTPRINT_FEATURES_NV(1000204000),
 	PIPELINE_VIEWPORT_EXCLUSIVE_SCISSOR_STATE_CREATE_INFO_NV(1000205000),
 	PHYSICAL_DEVICE_EXCLUSIVE_SCISSOR_FEATURES_NV(1000205002),
 	CHECKPOINT_DATA_NV(1000206000),
 	QUEUE_FAMILY_CHECKPOINT_PROPERTIES_NV(1000206001),
 	PHYSICAL_DEVICE_VULKAN_MEMORY_MODEL_FEATURES_KHR(1000211000),
 	PHYSICAL_DEVICE_PCI_BUS_INFO_PROPERTIES_EXT(1000212000),
 	IMAGEPIPE_SURFACE_CREATE_INFO_FUCHSIA(1000214000),
 	PHYSICAL_DEVICE_FRAGMENT_DENSITY_MAP_FEATURES_EXT(1000218000),
 	PHYSICAL_DEVICE_FRAGMENT_DENSITY_MAP_PROPERTIES_EXT(1000218001),
 	RENDER_PASS_FRAGMENT_DENSITY_MAP_CREATE_INFO_EXT(1000218002),
 	PHYSICAL_DEVICE_SCALAR_BLOCK_LAYOUT_FEATURES_EXT(1000221000),
 	PHYSICAL_DEVICE_MEMORY_BUDGET_PROPERTIES_EXT(1000237000),
 	PHYSICAL_DEVICE_MEMORY_PRIORITY_FEATURES_EXT(1000238000),
 	MEMORY_PRIORITY_ALLOCATE_INFO_EXT(1000238001),
 	PHYSICAL_DEVICE_DEDICATED_ALLOCATION_IMAGE_ALIASING_FEATURES_NV(1000240000),
 	PHYSICAL_DEVICE_BUFFER_ADDRESS_FEATURES_EXT(1000244000),
 	BUFFER_DEVICE_ADDRESS_INFO_EXT(1000244001),
 	BUFFER_DEVICE_ADDRESS_CREATE_INFO_EXT(1000244002),
 	IMAGE_STENCIL_USAGE_CREATE_INFO_EXT(1000246000),
 	VALIDATION_FEATURES_EXT(1000247000),
 	PHYSICAL_DEVICE_COOPERATIVE_MATRIX_FEATURES_NV(1000249000),
 	COOPERATIVE_MATRIX_PROPERTIES_NV(1000249001),
 	PHYSICAL_DEVICE_COOPERATIVE_MATRIX_PROPERTIES_NV(1000249002),
 	DEBUG_REPORT_CREATE_INFO_EXT(1000011000),
 	RENDER_PASS_MULTIVIEW_CREATE_INFO_KHR(1000053000),
 	PHYSICAL_DEVICE_MULTIVIEW_FEATURES_KHR(1000053001),
 	PHYSICAL_DEVICE_MULTIVIEW_PROPERTIES_KHR(1000053002),
 	PHYSICAL_DEVICE_FEATURES_2_KHR(1000059000),
 	PHYSICAL_DEVICE_PROPERTIES_2_KHR(1000059001),
 	FORMAT_PROPERTIES_2_KHR(1000059002),
 	IMAGE_FORMAT_PROPERTIES_2_KHR(1000059003),
 	PHYSICAL_DEVICE_IMAGE_FORMAT_INFO_2_KHR(1000059004),
 	QUEUE_FAMILY_PROPERTIES_2_KHR(1000059005),
 	PHYSICAL_DEVICE_MEMORY_PROPERTIES_2_KHR(1000059006),
 	SPARSE_IMAGE_FORMAT_PROPERTIES_2_KHR(1000059007),
 	PHYSICAL_DEVICE_SPARSE_IMAGE_FORMAT_INFO_2_KHR(1000059008),
 	MEMORY_ALLOCATE_FLAGS_INFO_KHR(1000060000),
 	DEVICE_GROUP_RENDER_PASS_BEGIN_INFO_KHR(1000060003),
 	DEVICE_GROUP_COMMAND_BUFFER_BEGIN_INFO_KHR(1000060004),
 	DEVICE_GROUP_SUBMIT_INFO_KHR(1000060005),
 	DEVICE_GROUP_BIND_SPARSE_INFO_KHR(1000060006),
 	BIND_BUFFER_MEMORY_DEVICE_GROUP_INFO_KHR(1000060013),
 	BIND_IMAGE_MEMORY_DEVICE_GROUP_INFO_KHR(1000060014),
 	PHYSICAL_DEVICE_GROUP_PROPERTIES_KHR(1000070000),
 	DEVICE_GROUP_DEVICE_CREATE_INFO_KHR(1000070001),
 	PHYSICAL_DEVICE_EXTERNAL_IMAGE_FORMAT_INFO_KHR(1000071000),
 	EXTERNAL_IMAGE_FORMAT_PROPERTIES_KHR(1000071001),
 	PHYSICAL_DEVICE_EXTERNAL_BUFFER_INFO_KHR(1000071002),
 	EXTERNAL_BUFFER_PROPERTIES_KHR(1000071003),
 	PHYSICAL_DEVICE_ID_PROPERTIES_KHR(1000071004),
 	EXTERNAL_MEMORY_BUFFER_CREATE_INFO_KHR(1000072000),
 	EXTERNAL_MEMORY_IMAGE_CREATE_INFO_KHR(1000072001),
 	EXPORT_MEMORY_ALLOCATE_INFO_KHR(1000072002),
 	PHYSICAL_DEVICE_EXTERNAL_SEMAPHORE_INFO_KHR(1000076000),
 	EXTERNAL_SEMAPHORE_PROPERTIES_KHR(1000076001),
 	EXPORT_SEMAPHORE_CREATE_INFO_KHR(1000077000),
 	PHYSICAL_DEVICE_16BIT_STORAGE_FEATURES_KHR(1000083000),
 	DESCRIPTOR_UPDATE_TEMPLATE_CREATE_INFO_KHR(1000085000),
 	SURFACE_CAPABILITIES2_EXT(1000090000),
 	PHYSICAL_DEVICE_EXTERNAL_FENCE_INFO_KHR(1000112000),
 	EXTERNAL_FENCE_PROPERTIES_KHR(1000112001),
 	EXPORT_FENCE_CREATE_INFO_KHR(1000113000),
 	PHYSICAL_DEVICE_POINT_CLIPPING_PROPERTIES_KHR(1000117000),
 	RENDER_PASS_INPUT_ATTACHMENT_ASPECT_CREATE_INFO_KHR(1000117001),
 	IMAGE_VIEW_USAGE_CREATE_INFO_KHR(1000117002),
 	PIPELINE_TESSELLATION_DOMAIN_ORIGIN_STATE_CREATE_INFO_KHR(1000117003),
 	PHYSICAL_DEVICE_VARIABLE_POINTER_FEATURES_KHR(1000120000),
 	MEMORY_DEDICATED_REQUIREMENTS_KHR(1000127000),
 	MEMORY_DEDICATED_ALLOCATE_INFO_KHR(1000127001),
 	BUFFER_MEMORY_REQUIREMENTS_INFO_2_KHR(1000146000),
 	IMAGE_MEMORY_REQUIREMENTS_INFO_2_KHR(1000146001),
 	IMAGE_SPARSE_MEMORY_REQUIREMENTS_INFO_2_KHR(1000146002),
 	MEMORY_REQUIREMENTS_2_KHR(1000146003),
 	SPARSE_IMAGE_MEMORY_REQUIREMENTS_2_KHR(1000146004),
 	SAMPLER_YCBCR_CONVERSION_CREATE_INFO_KHR(1000156000),
 	SAMPLER_YCBCR_CONVERSION_INFO_KHR(1000156001),
 	BIND_IMAGE_PLANE_MEMORY_INFO_KHR(1000156002),
 	IMAGE_PLANE_MEMORY_REQUIREMENTS_INFO_KHR(1000156003),
 	PHYSICAL_DEVICE_SAMPLER_YCBCR_CONVERSION_FEATURES_KHR(1000156004),
 	SAMPLER_YCBCR_CONVERSION_IMAGE_FORMAT_PROPERTIES_KHR(1000156005),
 	BIND_BUFFER_MEMORY_INFO_KHR(1000157000),
 	BIND_IMAGE_MEMORY_INFO_KHR(1000157001),
 	PHYSICAL_DEVICE_MAINTENANCE_3_PROPERTIES_KHR(1000168000),
 	DESCRIPTOR_SET_LAYOUT_SUPPORT_KHR(1000168001);

	private final int value;

	private VkStructureType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
