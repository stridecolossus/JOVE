package org.sarge.jove.model.md5;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Quaternion;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.util.StringDataSource;

public class MD5ParserTest {
	private MD5Parser parser;
	private DataSource src;

	@Before
	public void before() {
		src = new StringDataSource();
		parser = new MD5Parser( src );
	}

	@Test
	public void readers() throws IOException {
		parser.open( "token 1 integer 2 3.0 \"string\" ( 4 5 6 ) ( 7 8 9 ) ( 0.1 0.2 )" );
		assertEquals( "token", parser.readToken() );
		assertEquals( 1, parser.readInteger() );
		assertEquals( 2, parser.readInteger( "integer" ) );
		assertFloatEquals( 3f, parser.readFloat() );
		assertEquals( "string", parser.readString() );
		assertEquals( new Point( 4, 5, 6 ), parser.readPoint() );
		assertEquals( new Quaternion( 0, 7, 8, 9 ), parser.readOrientation() );
		assertEquals( new TextureCoord( 0.1f, 0.2f ), parser.readTextureCoords() );
	}

	@Test( expected = IOException.class )
	public void readStringNoStartQuote() throws IOException {
		parser.open( "string" );
		parser.readString();
	}

	@Test( expected = IOException.class )
	public void readStringNoEndQuote() throws IOException {
		parser.open( "\"string" );
		parser.readString();
	}

	@Test
	public void readFloatArray() throws IOException {
		final float[] array = new float[ 3 ];
		parser.open( "( 0 1 2 )" );
		parser.readFloatArray( array );
		for( int n = 0; n < 3; ++n ) {
			assertFloatEquals( n, array[ n ] );
		}
	}

	@Test
	public void nextLine() throws IOException {
		parser.open( "one \n two " );
		parser.nextLine();
		assertEquals( "two", parser.readToken() );
	}

	@Test
	public void skipToken() throws IOException {
		parser.open( "token" );
		parser.skipToken( "token" );
	}

	@Test( expected = IOException.class )
	public void skipTokenInvalid() throws IOException {
		parser.open( "cobblers" );
		parser.skipToken( "token" );
	}

	@Test
	public void startSection() throws IOException {
		parser.open( "section { } " );
		parser.startSection( "section" );
		parser.endSection();
	}

	@Test( expected = IOException.class )
	public void startSectionAlreadyStarted() throws IOException {
		parser.open( "section { } " );
		parser.startSection( "section" );
		parser.startSection( "section" );
	}

	@Test( expected = IOException.class )
	public void endSectionNotStarted() throws IOException {
		parser.open( "whatever" );
		parser.endSection();
	}

	@Test
	public void readHeader() throws IOException {
		parser.open( "MD5Version 10 commandline \"yada yada\" " );
		final int ver = parser.readHeader();
		assertEquals( 10, ver );
	}

	@Test( expected = IOException.class )
	public void readHeaderUnsupported() throws IOException {
		parser.open( "MD5Version 11" );
		parser.readHeader();
	}

	@Test( expected = IOException.class )
	public void openAlreadyOpened() throws IOException {
		parser.open( "whatever" );
		parser.open( "whatever" );
	}

	@Test
	public void close() throws IOException {
		parser.open( "whatever" );
		parser.close();
		parser.open( "reopened" );
	}
}
