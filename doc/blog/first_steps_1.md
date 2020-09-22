# Vulkan singleton

```java
public final class Vulkan {
	private static Vulkan VULKAN;

	public static Vulkan vulkan() {
		// TODO
		return VULKAN;
	}

	private final VulkanLibrary api;

	private Vulkan(VulkanLibrary api) {
		this.api = notNull(api);
	}

	VulkanLibrary api() {
		return api;
	}

	public Set<String> extensions() {
		return null; // TODO
	}

	public Set<Layer> layers() {
		return null; // TODO
	}
}
```

# Instance first-cut

```java
import com.sun.jna.Pointer;

public class Instance {
	public Instance(Vulkan vulkan, Set<String> extensions, Set<Layer> layers) {
		// TODO
	}

	public Pointer handle() {
		return null;
	}
}
```

# Instance test

```java
public class InstanceTest {
	private Instance instance;
	private Vulkan vulkan;

	@BeforeEach
	void before() {
		vulkan = mock(Vulkan.class);
		// TODO - mock extensions/layers
		instance = new Instance(vulkan, Set.of("ext"), Set.of(new ValidationLayer("layer", 42)));
	}

	@Test
	void constructor() {
		assertNotNull(instance.handle());
	}

	@Test
	void unavailableExtension() {
		assertThrows(IllegalArgumentException.class, () -> new Instance(vulkan, Set.of("cobblers"), Set.of(new ValidationLayer("layer", 42))));
	}

	@Test
	void unavailableValidationLayer() {
		assertThrows(IllegalArgumentException.class, () -> new Instance(vulkan, Set.of("ext"), Set.of(new ValidationLayer("layer", 999))));
	}
}
```

# Layer record

```java
public record Layer(String name, int version) {
	public Layer {
		Check.notEmpty(name);
		Check.oneOrMore(version);
	}

	public boolean isMember(Set<Layer> layers) {
		return false;
	}
}
```

# Layer test first-cut

```java
public class ValidationLayerTest {
	private ValidationLayer layer;

	@BeforeEach
	void before() {
		layer = new ValidationLayer("layer", 42);
	}

	@Test
	void constructor() {
		assertEquals("layer", layer.name());
		assertEquals(42, layer.version());
	}

	@Test
	void invalidVersionNumber() {
		assertThrows(IllegalArgumentException.class, () -> new ValidationLayer("layer", 0));
	}

	@Nested
	class MemberTests {
		private Set<ValidationLayer> set;

		@BeforeEach
		void before() {
			set = Set.of(layer);
		}

		@Test
		void self() {
			assertEquals(true, layer.isMember(set));
		}

		@Test
		void other() {
			assertEquals(false, new ValidationLayer("other", 42).isMember(set));
		}

		@Test
		void lower() {
			assertEquals(false, new ValidationLayer("layer", 1).isMember(set));
		}

		@Test
		void higher() {
			assertEquals(true, new ValidationLayer("layer", 999).isMember(set));
		}
	}
}
```

# Vulkan API first-cut

```java
interface VulkanLibrary extends Library {
	Version VERSION = new Version(1, 0, 2);

	static VulkanLibrary create() {
	    return null;
	}
}
```
