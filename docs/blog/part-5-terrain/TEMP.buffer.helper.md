### Buffer Helper

To populate the data buffer for the push constants we take the opportunity to implement a new helper utility for managing NIO buffers.

A __direct__ byte buffer is allocated as follows:

```java
public final class BufferHelper {
    /**
     * Native byte order for a bufferable object.
     */
    public static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

    private BufferHelper() {
    }

    /**
     * Allocates a <b>direct</b> byte buffer of the given length with {@link #NATIVE_ORDER}.
     * @param len Buffer length
     * @return New byte buffer
     */
    public static ByteBuffer allocate(int len) {
        return ByteBuffer.allocateDirect(len).order(NATIVE_ORDER);
    }
}
```

A byte array can be written to a buffer:

```java
public static void write(byte[] array, ByteBuffer bb) {
    if(bb.isDirect()) {
        for(byte b : array) {
            bb.put(b);
        }
    }
    else {
        bb.put(array);
    }
}
```

The utility class also supports conversion of an NIO buffer to a byte array:

```java
public static byte[] array(ByteBuffer bb) {
    if(bb.isDirect()) {
        bb.rewind();
        int len = bb.limit();
        byte[] bytes = new byte[len];
        for(int n = 0; n < len; ++n) {
            bytes[n] = bb.get();
        }
        return bytes;
    }
    else {
        return bb.array();
    }
}
```

And the reverse operation to wrap an array with a buffer:

```java
public static ByteBuffer buffer(byte[] array) {
    ByteBuffer bb = allocate(array.length);
    write(array, bb);
    return bb;
}
```

Note that direct NIO buffers generally do not support the optional bulk methods.

Existing code that transforms to/from byte buffers is refactored using the new utility methods, e.g. shader SPIV code.

As a further convenience for applying updates to push constants (or to uniform buffers) the following method can be used to insert data into a buffer:

```java
public static void insert(int index, Bufferable data, ByteBuffer bb) {
    int pos = index * data.length();
    bb.position(pos);
    data.buffer(bb);
}
```

This is useful for buffers that are essentially an 'array' of some type of bufferable object (which we use below).

Finally in the same vein we add a new factory method to the bufferable class to wrap a JNA structure:

```java
static Bufferable of(Structure struct) {
    return new Bufferable() {
        @Override
        public int length() {
            return struct.size();
        }

        @Override
        public void buffer(ByteBuffer bb) {
            byte[] array = struct.getPointer().getByteArray(0, struct.size());
            BufferHelper.write(array, bb);
        }
    };
}
```

This allows arbitrary JNA structures to be used to populate push constants or a uniform buffer which will become useful in later chapters.