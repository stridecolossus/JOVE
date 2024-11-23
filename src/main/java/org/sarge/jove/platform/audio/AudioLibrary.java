package org.sarge.jove.platform.audio;

/**
 * OpenAL library.
 * @author Sarge
 */
interface AudioLibrary extends AudioDevice.Library, AudioContext.Library, AudioListener.Library, AudioBuffer.Library, AudioSource.Library {
	/**
	 * Creates the OpenAL audio system.
	 * @return Audio system
	 * @throws RuntimeException if OpenAL cannot be started
	 */
	static AudioLibrary create() {
//		// Determine library
//		final String name = switch(Platform.getOSType()) {
//			case Platform.WINDOWS -> "OpenAL32";
//    		case Platform.LINUX -> "OpenAL32";
//    		default -> throw new RuntimeException("Unsupported platform for OpenAL: " + Platform.getOSType());
//    	};
//
//    	// Init JNA type converters
//		final var mapper = new DefaultTypeMapper();
//		mapper.addTypeConverter(Boolean.class, new NativeBooleanConverter());
//		mapper.addTypeConverter(IntEnum.class, IntEnum.CONVERTER);
//		mapper.addTypeConverter(Handle.class, Handle.CONVERTER);
//		mapper.addTypeConverter(NativeObject.class, NativeObject.CONVERTER);
//
//		// Load library
//		return Native.load(name, AudioLibrary.class, Map.of(Library.OPTION_TYPE_MAPPER, mapper));
		return null;
	}

	/**
	 * Retrieves the latest error.
	 * @return Error code
	 */
	int alGetError();
}
