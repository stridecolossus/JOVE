# Instance

```java
/**
 * An <i>instance</i> is the root object for a Vulkan application.
 * @author Sarge
 */
public class Instance {
	private final Vulkan vulkan;
	private final Pointer handle;

	/**
	 * Constructor.
	 * @param vulkan		Vulkan
	 * @param handle		Instance handle
	 */
	private Instance(Vulkan vulkan, Pointer handle) {
		this.vulkan = notNull(vulkan);
		this.handle = notNull(handle);
	}

	/**
	 * @return Vulkan API
	 */
	public VulkanLibrary vulkan() {
		return vulkan.api();
	}

	/**
	 * @return Instance handle
	 */
	public Pointer handle() {
		return handle;
	}

	/**
	 * Destroys this instance.
	 */
	public void destroy() {
		// TODO
	}

	/**
	 * Builder for a Vulkan instance.
	 */
	public static class Builder {
		/**
		 * Sets the application name.
		 * @param name Application name
		 */
		public Builder name(String name) {
			return this;
		}

		/**
		 * Sets the required minimum Vulkan version.
		 * @param ver Minimum version
		 */
		public Builder version(Version ver) {
			return this;
		}

		/**
		 * Registers a required extension.
		 * @param ext Extension name
		 * @see Vulkan#extensions()
		 */
		public Builder extension(String ext) {
			return this;
		}

		/**
		 * Registers a required validation layer.
		 * @param layer Validation layer descriptor
		 * @see Vulkan#layers()
		 */
		public Builder layer(ValidationLayer layer) {
			return this;
		}

		/**
		 * Constructs this instance.
		 * @return New instance
		 * @throws ServiceException if the instance cannot be created
		 */
		public Instance build() {
			return null;
		}
	}
}
```

# Test

```java
@Tag(INTEGRATION_TEST)
public class InstanceTest {
	private Instance.Builder builder;

	@BeforeEach
	void before() {
		builder = new Instance.Builder().name("test");
	}

	@Test
	void buildMissingName() {
		assertThrows(IllegalArgumentException.class, () -> new Instance.Builder().build());
	}

	@Test
	void build() {
		final Instance instance = builder.build();
		assertNotNull(instance);
		assertNotNull(instance.handle());
		assertNotNull(instance.vulkan());
	}

	@Test
	void buildExtension() {
		builder.extension(Vulkan.EXTENSION_DEBUG_UTILS).build();
	}

	@Test
	void buildExtensionUnknown() {
		assertThrows(ServiceException.class, () -> builder.extension("cobblers").build());
	}

	@Test
	void buildValidationLayer() {
		builder.layer(ValidationLayer.STANDARD_VALIDATION).build();
	}

	@Test
	void buildValidationLayerUnknown() {
		assertThrows(ServiceException.class, () -> builder.layer(new ValidationLayer("cobblers")).build());
	}
}
```

# Builder

```java
		private String name;
		private Version ver = VulkanLibrary.VERSION;
		private final Set<String> extensions = new HashSet<>();
		private final Set<ValidationLayer> layers = new HashSet<>();
		
		public Builder extension(String ext) {
			Check.notEmpty(ext);
			extensions.add(ext);
			return this;
		}

		public Instance build() {
			// Init application descriptor
			Check.notEmpty(name);
			final VkApplicationInfo app = new VkApplicationInfo();
			app.pApplicationName = name;
			app.applicationVersion = ver.toInteger();

			// Init instance descriptor
			final VkInstanceCreateInfo info = new VkInstanceCreateInfo();
			info.pApplicationInfo = app;

			// Populate required extensions
			info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
			info.enabledExtensionCount = extensions.size();

			// Populate required layers
			final String[] layerNames = layers.stream().map(ValidationLayer::name).toArray(String[]::new);
			info.ppEnabledLayerNames = new StringArray(layerNames);
			info.enabledLayerCount = layerNames.length;

			// Create instance
			final PointerByReference handle = vulkan.factory().reference();
			check(vulkan.api().vkCreateInstance(info, null, handle));

			// Create instance wrapper
			return new Instance(vulkan, handle.getValue());
		}
```

# Integration tests

```java
public class InstanceTest {
	@Test
	void buildMissingName() {
		assertThrows(IllegalArgumentException.class, () -> new Instance.Builder(mock(Vulkan.class)).build());
	}

	@Test
	void build() {
		// Create Vulkan
		final Vulkan vulkan = mock(Vulkan.class);

		// Create API
		final VulkanLibrary api = mock(VulkanLibrary.class);
		when(vulkan.api()).thenReturn(api);

		// Init reference factory
		final ReferenceFactory factory = mock(ReferenceFactory.class);
		when(vulkan.factory()).thenReturn(factory);

		// Create instance handle
		final PointerByReference handle = new PointerByReference(new Pointer(42));
		when(factory.reference()).thenReturn(handle);

		// Mock API invocation
		final ArgumentCaptor<VkInstanceCreateInfo> captor = ArgumentCaptor.forClass(VkInstanceCreateInfo.class);
		when(api.vkCreateInstance(captor.capture(), isNull(), eq(handle))).thenReturn(VulkanLibrary.SUCCESS);

		// Create instance
		final Version ver = new Version(1, 2, 3);
		final Instance instance = new Instance.Builder(vulkan)
				.name("test")
				.version(ver)
				.extension("ext")
				.layer(new ValidationLayer("layer"))
				.build();

		// Check instance
		assertNotNull(instance);
		assertNotNull(instance.handle());
		assertEquals(api, instance.vulkan());

		// Check instance descriptor
		final VkInstanceCreateInfo info = captor.getValue();
		assertEquals(1, info.enabledExtensionCount);
		assertEquals(1, info.enabledLayerCount);
		assertNotNull(info.ppEnabledExtensionNames);
		assertNotNull(info.ppEnabledLayerNames);

		// Check application descriptor
		final VkApplicationInfo app = info.pApplicationInfo;
		assertNotNull(app);
		assertEquals("test", app.pApplicationName);
		assertEquals("JOVE", app.pEngineName);
		assertEquals(ver.toInteger(), app.apiVersion);
	}

	@Tag(INTEGRATION_TEST)
	@Nested
	class IntegrationTests {
		private Vulkan vulkan;
		private Instance.Builder builder;
		private Instance instance;

		@BeforeEach
		void before() {
			vulkan = Vulkan.vulkan();
			builder = new Instance.Builder(vulkan).name("test");
		}

		@AfterEach
		void after() {
			if(instance != null) {
				instance.destroy();
				instance = null;
			}
		}

		@Test
		void build() {
			// Create instance
			instance = builder
					.extension(Vulkan.EXTENSION_DEBUG_UTILS)
					.layer(ValidationLayer.STANDARD_VALIDATION)
					.build();

			// Check instance
			assertNotNull(instance);
			assertNotNull(instance.handle());
			assertEquals(vulkan.api(), instance.vulkan());

			// Destroy instance
			instance.destroy();
		}

		@Test
		void unknownExtension() {
			builder.extension("cobblers");
			expect(VkResult.VK_ERROR_EXTENSION_NOT_PRESENT);
		}

		@Test
		void unknownLayer() {
			builder.layer(new ValidationLayer("cobblers"));
			expect(VkResult.VK_ERROR_LAYER_NOT_PRESENT);
		}

		private void expect(VkResult expected) {
			final VulkanException e = assertThrows(VulkanException.class, builder::build);
			assertEquals(expected.value(), e.result);
		}
	}
}
```

# Exception handling

```java
	/**
	 * Checks the result of a Vulkan operation.
	 * @param result Result code
	 * @throws VulkanException if the given result is not {@link VkResult#VK_SUCCESS}
	 */
	static void check(int result) {
		if(result != SUCCESS) {
			throw new VulkanException(result);
		}
	}

public class VulkanException extends ServiceException {
	public final int result;

	/**
	 * Constructor.
	 * @param message
	 * @param result
	 */
	public VulkanException(String message, int result) {
		super(String.format("%s: [%d] %s", message, result, reason(result)));
		this.result = result;
	}

	public VulkanException(int result) {
		this("Vulkan error", result);
	}

	private static String reason(int result) {
		try {
			final VkResult value = IntegerEnumeration.map(VkResult.class, result);
			return value.name();
		}
		catch(IllegalArgumentException e) {
			return "Unknown error code";
		}
	}
}
```

# Destructor

```java
	/**
	 * Destroys this instance.
	 */
	public void destroy() {
		api.vkDestroyInstance(handle, null);
	}
```
