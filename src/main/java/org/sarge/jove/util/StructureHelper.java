package org.sarge.jove.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * JNA helper utilities.
 * @author Sarge
 */
public final class StructureHelper {
	/**
	 * PRedicate for structure fields.
	 */
	private static final Predicate<Field> STRUCTURE_FIELD = field -> {
		final int mods = field.getModifiers();
		if(!Modifier.isPublic(mods)) return false;
		if(Modifier.isStatic(mods)) return false;
		return true;
	};

	private StructureHelper() {
	}

// TODO
//	However, I realize there may be a more elegant solution now, rather than write/read/write...
	//we could just write once.
	//We just change the structure's memory backing from its autoallocated location to the contiguous location and then write to it.
	//Untested, but this might work: for (int n....) { structures[n].useMemory(mem.share(n * size)); structures[n].write(); }
	//(JNA keeps track of references to the native memory and will free it when it's no longer backing a java object, you don't need to do that yourself.)
	//– Daniel Widdis Mar 18 at 16:06

	/**
	 * Allocates a contiguous memory block for a pointer-to-structure array.
	 * @param structures Structures
	 * @return Contiguous memory block or <tt>null</tt> for an empty list
	 * @param <T> Structure type
	 */
	public static <T extends Structure> Memory structures(Collection<T> structures) {
		// Check for empty case
		if(structures.isEmpty()) {
			return null;
		}

		// Allocate contiguous memory block
		final Structure[] array = structures.toArray(Structure[]::new);
		final int size = array[0].size();
		final Memory mem = new Memory(array.length * size);

		// Copy structures
		for(int n = 0; n < array.length; ++n) {
			if(array[n] != null) {
				array[n].write(); // TODO - what is this actually doing? following line returns zeros unless write() is invoked
				final byte[] bytes = array[n].getPointer().getByteArray(0, size);
				mem.write(n * size, bytes, 0, bytes.length);
			}
		}

		return mem;
	}

	/**
	 * Allocates a contiguous memory block for a pointer-to-pointers array.
	 * @param pointers Pointers
	 * @return Pointer
	 */
	public static Memory pointers(Collection<Pointer> pointers) {
		final int size = pointers.size();
		if(size == 0) {
			return null;
		}

		final Pointer[] array = pointers.toArray(Pointer[]::new);
		final Memory mem = new Memory(Native.POINTER_SIZE * size);
		for(int n = 0; n < size; ++n) {
			final Pointer ptr = array[n];
			if(ptr != null) {
				mem.setPointer(Native.POINTER_SIZE * n, array[n]);
			}
		}
		return mem;
	}

	/**
	 * Allocates a contiguous memory block for a pointer-to-float array.
	 * @param array Float array
	 * @return Pointer
	 */
	public static Memory floats(float[] array) {
		if(array.length == 0) return null;
		final Memory mem = new Memory(array.length * Float.BYTES);
		mem.write(0, array, 0, array.length);
		return mem;
	}

	/**
	 * Allocates a contiguous memory block for a pointer-to-integer array.
	 * @param array Integer array
	 * @return Pointer
	 */
	public static Memory integers(int[] array) {
		if(array.length == 0) return null;
		final Memory mem = new Memory(array.length * Integer.BYTES);
		mem.write(0, array, 0, array.length);
		return mem;
	}

	/**
	 * Recursively clones a JNA structure using reflection.
	 * @param src		Source
	 * @param dest		Destination
	 * @return Copied structure
	 */
	public static <T extends Structure> T copy(T src, T dest) {
		final Consumer<Field> copy = field -> {
			try {
				final Object value = field.get(src);
				if(value == null) {
					// Ignore empty fields
					return;
				}
				else
				if(Structure.class.isAssignableFrom(field.getType())) {
					// Recurse structure fields
					final Structure struct = (Structure) field.getType().getDeclaredConstructor().newInstance();
					copy((Structure) value, struct);
					field.set(dest, struct);
				}
				else {
					// Otherwise copy field
					field.set(dest, value);
				}
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		};
		fields(src).forEach(copy);
		return dest;
	}

	/**
	 * @param struct JNA structure
	 * @return Structure fields
	 */
	public static Stream<Field> fields(Structure struct) {
		return Arrays.stream(struct.getClass().getDeclaredFields()).filter(STRUCTURE_FIELD);
	}
}
