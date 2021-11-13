package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;

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
		when(in.readInt()).thenReturn(2);
		assertEquals(2, helper.version(in));
	}

	@Test
	void loadVersionNumberInvalid() throws IOException {
		when(in.readInt()).thenReturn(3);
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
		final Bufferable obj = helper.buffer(in);
		assertNotNull(obj);
		assertEquals(1, obj.length());
		verify(in).readFully(bytes);
	}

	@Test
	void loadBufferableEmpty() throws IOException {
		when(in.readInt()).thenReturn(0);
		assertEquals(null, helper.buffer(in));
	}

	@Test
	void writeBuffer() throws IOException {
		final byte[] bytes = new byte[1];
		helper.write(Bufferable.of(bytes), out);
		verify(out).writeInt(1);
		verify(out).write(bytes);
	}

	@Test
	void loadLayout() throws IOException {
		// Init layout data
		when(in.readUTF()).then(AdditionalAnswers.returnsElementsOf(List.of("RGB", Float.class.getName())));
		when(in.readInt()).thenReturn(Float.BYTES);
		when(in.readBoolean()).thenReturn(true);

		// Load layout
		final Layout layout = helper.layout(in);
		assertEquals("RGB", layout.components());
		assertEquals(Float.BYTES, layout.bytes());
		assertEquals(Float.class, layout.type());
		assertEquals(true, layout.signed());
	}

	@Test
	void writeLayout() throws IOException {
		final Layout layout = Layout.of(3);
		helper.write(layout, out);
		verify(out).writeUTF("RGB");
		verify(out).writeInt(Float.BYTES);
		verify(out).writeUTF(Float.class.getName());
		verify(out).writeBoolean(true);
	}
}
