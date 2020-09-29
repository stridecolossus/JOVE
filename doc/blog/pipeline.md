# Handle class

```java
public final class Handle {
	/**
	 * Native type converter for a handle.
	 */
	public static final TypeConverter CONVERTER = new TypeConverter() {
		@Override
		public Class<?> nativeType() {
			return Pointer.class;
		}

		@Override
		public Object toNative(Object value, ToNativeContext context) {
			final Handle handle = (Handle) value;
			return handle.handle;
		}

		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			throw new UnsupportedOperationException();
		}
	};

	private final Pointer handle;

	/**
	 * Constructor.
	 * @param handle Pointer handle
	 */
	public Handle(Pointer handle) {
		this.handle = notNull(handle);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
```

# Refactor API

```java
void vkDestroyInstance(Handle instance, Handle allocator);
```

# Refactor message handler

```java
private void destroy(Pointer handle) {
	final Object[] args = new Object[]{Instance.this.handle, handle, null};
	destroy.invoke(Void.class, args, options());
}

/**
 * @return Type converter options
 */
private Map<String, ?> options() {
	return Map.of(Library.OPTION_TYPE_MAPPER, VulkanLibrary.MAPPER);
}
```

