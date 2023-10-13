package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.sarge.jove.common.*;

public class DataHelperTest {
	private DataHelper helper;
	private DataInput in;
	private DataOutput out;

	@BeforeEach
	void before() {
		helper = new DataHelper(2);
		in = mock(DataInput.class);
		out = mock(DataOutput.class);
	}

	@Test
	void loadVersionNumber() throws IOException {
		Mockito.when(in.readInt()).thenReturn(2);
		assertEquals(2, helper.version(in));
	}

	@Test
	void loadVersionNumberInvalid() throws IOException {
		Mockito.when(in.readInt()).thenReturn(3);
		assertThrows(UnsupportedOperationException.class, () -> helper.version(in));
	}

	@Test
	void writeVersionNumber() throws IOException {
		helper.writeVersion(out);
		verify(out).writeInt(2);
	}

	@Test
	void loadBufferable() throws IOException {
		// Init input data
		final byte[] bytes = new byte[1];
		when(in.readInt()).thenReturn(bytes.length);

		// Load bufferable object
		final ByteSizedBufferable obj = helper.buffer(in);
		assertNotNull(obj);
		assertEquals(1, obj.length());
		verify(in).readFully(bytes);
	}

	@Test
	void loadBufferableEmpty() throws IOException {
		Mockito.when(in.readInt()).thenReturn(0);
		assertEquals(null, helper.buffer(in));
	}

	@Test
	void writeBuffer() throws IOException {
		final byte[] bytes = new byte[1];
		helper.write(ByteSizedBufferable.of(bytes), out);
		verify(out).writeInt(1);
		verify(out).write(bytes);
	}

	@Test
	void loadLayout() throws IOException {
		// Init layout data
		Mockito.when(in.readUTF()).thenReturn("FLOAT");
		Mockito.when(in.readInt()).then(AdditionalAnswers.returnsElementsOf(List.of(3, 4)));
		Mockito.when(in.readBoolean()).thenReturn(true);

		// Load layout
		final Layout layout = helper.layout(in);
		assertEquals(3, layout.count());
		assertEquals(Float.BYTES, layout.bytes());
		assertEquals(Layout.Type.FLOAT, layout.type());
		assertEquals(true, layout.signed());
	}

	@Test
	void writeLayout() throws IOException {
		final Layout layout = Layout.floats(3);
		helper.write(layout, out);
		verify(out).writeInt(3);
		verify(out).writeInt(Float.BYTES);
		verify(out).writeUTF("FLOAT");
		verify(out).writeBoolean(true);
	}
}
