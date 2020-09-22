# Outline

```java
public class LogicalDevice {
	public class Queue {
		private Queue(Pointer handle, QueueFamily family) {
		}

		Pointer handle() {
		}

		public QueueFamily family() {
		}

		public LogicalDevice device() {
		}
	}

	LogicalDevice(Pointer handle, PhysicalDevice parent, Map<QueueFamily, List<Pointer>> queues) {
	}

	Pointer handle() {
	}

	public PhysicalDevice parent() {
	}

	public Map<QueueFamily, List<Queue>> queues() {
	}

	public void destroy() {
	}
}
```

# Builder

```java
	public static class Builder {
		public Builder parent(PhysicalDevice parent) {
		}

		public Builder features(VkPhysicalDeviceFeatures features) {
		}

		public Builder extension(String ext) {
		}

		public Builder layer(ValidationLayer layer) {
		}

		public Builder queue(QueueFamily family) {
		}

		public Builder queue(QueueFamily family, int num) {
		}
		
		public Builder queue(QueueFamily family, float[] priorities) {
		}

		public LogicalDevice build() {
		}
}
```
