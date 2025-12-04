package org.sarge.jove.platform.vulkan.generator;

import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * The <i>tokenizer</i> breaks up the input file into tokens.
 * TODO - word boundaries
 * @author Sarge
 */
class Tokenizer {
	private final Iterator<String> iterator;
	private String peek;

	/**
	 * Constructor.
	 * @param text Input text
	 */
	public Tokenizer(String text) {
		this.iterator = text
				.lines()
				.map(String::trim)
				.filter(Predicate.not(String::isEmpty))
				.filter(Predicate.not(Tokenizer::isComment))
				.filter(new LineFilter("/*", "*/"))
				.filter(new LineFilter("#ifndef VK_NO_PROTOTYPES", "#endif"))
				.filter(macros())
				.flatMap(tokenize("\\s+"))
				.flatMap(tokenize("\\b"))
				.iterator();
	}

	/**
	 * @return Whether the next line is a comment
	 */
	private static boolean isComment(String line) {
		return line.startsWith("//");
	}

	/**
	 * Line filter for macro definitions.
	 */
	private static Predicate<String> macros() {
		return line -> {
			// Check is a macro
			if(!line.startsWith("#")) {
				return true;
			}

			// Skip everything except constants
			if(!line.startsWith("#define")) {
				return false;
			}

			// Check is a constant
			return line.split("\\s+").length == 3;
		};
	}

	/**
	 * Splits a string into tokens.
	 */
	private static Function<String, Stream<String>> tokenize(String delimiter) {
		return text -> Arrays.stream(text.split(delimiter));
	}

	/**
	 * Skips lines between the given tokens.
	 * TODO - dubious
	 */
	private static class LineFilter implements Predicate<String> {
		private final String start, end;
		private boolean skip;

		/**
		 * Constructor.
		 * @param start		Start token
		 * @param end		End token
		 */
		public LineFilter(String start, String end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public boolean test(String line) {
			if(skip) {
				if(line.startsWith(end)) {
					skip = false;
				}
				return false;
			}
			else {
				if(line.startsWith(start)) {
					skip = true;
					return false;
				}
				else {
					return true;
				}
			}
		}
	}

	/**
	 * @return Whether there are any remaining tokens
	 */
	public boolean hasNext() {
		return Objects.nonNull(peek) || iterator.hasNext();
	}

	/**
	 * Consumes the next token.
	 * @return Next token
	 * @throws NoSuchElementException if there are no more tokens
	 * @see #hasNext()
	 */
	public String next() {
		if(peek == null) {
			return iterator.next();
		}
		else {
			final String token = peek;
			peek = null;
			return token;
		}
	}

	/**
	 * Optionally consumes the next token if it matches the given string.
	 * @param token Token to match
	 * @return Whether matched
	 * @throws NoSuchElementException if there are no more tokens
	 */
	public boolean peek(String token) {
		// Peek next token
		if(peek == null) {
			peek = iterator.next();
		}

		// Consume if matched
		if(peek.equals(token)) {
			peek = null;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Skips the next token.
	 * @param token Expected token to skip
	 * @throws NoSuchElementException if there are no more tokens
	 * @throws IllegalArgumentException if {@link #token} is empty
	 * @throws IllegalArgumentException if the token does not match
	 */
	public Tokenizer skip(String token) {
		requireNotEmpty(token);

		final String actual = next();
		if(!actual.equals(token)) {
			throw new IllegalArgumentException("Expected [%s] but found [%s]".formatted(token, actual));
		}

		return this;
	}

	/**
	 * Parses the next token as an integer or applies the given mapper.
	 * TODO - radix
	 * @param mapper Mapper for constant tokens
	 * @return Integer
	 * @throws NoSuchElementException if there are no more tokens
	 * @throws NumberFormatException if the next token is not a valid integer
	 * @throws NumberFormatException if {@link #mapper} returns {@code null} for a constant token
	 */
	public int integer(Function<String, Integer> mapper) {
		final boolean negative = peek("-");
		final String token = next();
		if(negative || Character.isDigit(token.charAt(0))) {
			final int value = parse(token.replace("_", ""));
			return negative ? -value : +value;
		}
		else {
			final Integer value = mapper.apply(token);
			if(value == null) {
				throw new NumberFormatException("Invalid integer token: " + token);
			}
			return value;
		}
	}

	/**
	 * Parse an integer token.
	 */
	private static int parse(String token) {
		if((token.length() > 2) && (token.charAt(0) == '0')) {
			// Parse given radix
			final int radix = radix(token.charAt(1));
			final String number = token.substring(2, token.length());
			return Integer.parseInt(number, radix);
		}
		else {
			// Otherwise assume is decimal
			return Integer.parseInt(token);
		}
	}

	/**
	 * @return Radix
	 */
	private static int radix(char radix) {
		return switch(radix) {
			case 'x' -> 16;
			case 'o' -> 8;
			case 'b' -> 2;
			default -> throw new NumberFormatException("Invalid radix: " + radix);
		};
	}
}
