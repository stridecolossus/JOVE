package org.sarge.jove.io;

import static org.sarge.lib.util.Check.zeroOrMore;

import java.io.*;
import java.util.Set;
import java.util.function.*;

/**
 * A <i>text loader</i> is a helper class that loads the lines from a file and delegates to a loader function.
 * <p>
 * Example:
 * <p>
 * <pre>
 * Reader in = ...
 * TextLoader loader = new TextLoader();
 * Consumer&lt;String&gt; parser = str -> { ... };
 * loader.load(in, parser);
 * </pre>
 * <p>
 * @author Sarge
 */
public class TextLoader {
	private Set<String> comments = Set.of("#");
	private int skip;

	/**
	 * Tests whether a given line is a comment.
	 * @param line Line
	 * @return Whether the given line starts with a comment token
	 * @see #setComments(Set)
	 */
	protected boolean isComment(String line) {
		return comments.stream().anyMatch(line::startsWith);
	}

	/**
	 * Sets the comment string(s).
	 * @param comments Comment strings
	 */
	public void setComments(Set<String> comments) {
		this.comments = Set.copyOf(comments);
	}

	/**
	 * Sets the number of header lines to skip (default is none).
	 * @param skip Number of lines to skip
	 */
	public void setSkipHeaderLines(int skip) {
		this.skip = zeroOrMore(skip);
	}

	/**
	 * Loads text from a reader and delegates to the given loader function.
	 * @param reader		Reader
	 * @param consumer		Consumer for a line
	 * @throws IOException if the text cannot be loaded
	 */
	public void load(Reader reader, Consumer<String> consumer) throws IOException {
		final LineNumberReader wrapper = new LineNumberReader(reader);
		try(wrapper) {
			wrapper
				.lines()
				.skip(skip)
				.map(String::trim)
				.filter(Predicate.not(String::isEmpty))
				.filter(Predicate.not(this::isComment))
				.forEach(consumer);
		}
		catch(IOException e) {
			throw e;
		}
		catch(Exception e) {
			throw new IOException(String.format("%s at line %d", e.getMessage(), wrapper.getLineNumber()), e);
		}
	}
}
