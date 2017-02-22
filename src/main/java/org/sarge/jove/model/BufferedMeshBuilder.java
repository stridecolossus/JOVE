package org.sarge.jove.model;

import java.nio.FloatBuffer;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.BufferFactory;

public class BufferedMeshBuilder {
	private FloatBuffer buffer;
	
	public BufferedMeshBuilder(int size) {
		this.buffer = BufferFactory.createFloatBuffer(size);
	}
	
	public void add(float... values) {
		for(float f : values) {
			buffer.put(f);
		}
	}
	
	public void add(Bufferable... objects) {
		for(Bufferable obj : objects) {
			obj.append(buffer);
		}
	}
	
	public FloatBuffer build() {
		final FloatBuffer result = buffer;
		buffer = BufferFactory.createFloatBuffer(buffer.limit());
		return result;
	}
}
