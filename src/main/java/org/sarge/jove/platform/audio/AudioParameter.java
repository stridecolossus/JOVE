package org.sarge.jove.platform.audio;

import org.sarge.jove.util.IntEnum;

/**
 * OpenAL API parameters.
 */
enum AudioParameter implements IntEnum {
	// System
	DEVICE_SPECIFIER(0x1005),
	EXTENSIONS(0x1006),

	// Error codes
	INVALID_NAME(0xA001),
	ILLEGAL_ENUM(0xA002),
	INVALID_ENUM(0xA002),
	INVALID_VALUE(0xA003),
	INVALID_OPERATION(0xA004),
	OUT_OF_MEMORY(0xA005),

	// Audio formats
	MONO_8(0x1100),
	MONO_16(0x1101),
	STEREO_8(0x1102),
	STEREO_16(0x1103),

	// Gain
	GAIN(0x100A),
	MIN_GAIN(0x100D),
	MAX_GAIN(0x100E),

	// Listener and source orientation
	POSITION(0x1004),
	DIRECTION(0x1005),
	VELOCITY(0x1006),
	ORIENTATION(0x100F),

	// Source properties
	PITCH(0x1003),
	LOOPING(0x1007),
	BUFFER(0x1009),
	SOURCE_STATE(0x1010),
	INITIAL(0x1011),
	PLAYING(0x1012),
	PAUSED(0x1013),
	STOPPED(0x1014),

	// Streaming
	BUFFERS_QUEUED(0x1015),
	BUFFERS_PROCESSED(0x1016);

	private final int value;

	private AudioParameter(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
