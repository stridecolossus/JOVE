# OBJ Model Loader

## File Format

An OBJ model is a text-based data format consisting of a series of *commands* to define a static model.
Each line starts with a *command token* followed by a number of space-delimited arguments.

The most common commands are:

| command 	| arguments 	| purpose 				| example 			|
| -------   | ---------		| -------				| -------			|
| v 	  	| x y x	  		| vertex position		| v 0.1 0.2 0.3 	|
| vn		| x y z			| normal				| vn 0.4 0.5 0.6	|
| vt		| u v			| texture coordinate	| vt 0.7 0.8		|
| f			| see below		| face or triangle		| f 1//3 4//6 7//9	|
| s			| n/a			| smoothing group		| s					|

The *face* command specifies a number vertices consisting of a vertex position and optionally a normal and texture coordinate (delimited by the slash character).

| example 				| description 								|
| f 1 2 3				| triangle with vertex positions only 		|
| f 1/2 3/4 5/6			| triangle also with texture coordinates 	|
| f 1/2/3 4/5/6 7/8/9	| also with normals 						|
| f 1//2 3//4 5//6		| normals but no texture coordinates 		|

In addition face indices can also be negative specifying a reverse index from the end of a list.

Example:
```
v 0.1 0.2 0.3
v 0.4 0.5 0.6
v 0.7 0.8 0.9
vt 0 0
vt 0 1
vt 1 0
f 1/1 2/2 3/3
```

## Model Loader

Since we are dealing with a text-based format the loader reads the *lines()* from an input reader and filters empty lines or comments:

```java
public void load(Reader r) throws IOException {
	try(final LineNumberReader in = new LineNumberReader(r)) {
		try {
			in.lines()
				.map(String::trim)
				.filter(Predicate.not(String::isBlank))
				.filter(Predicate.not(this::isComment))
				.forEach(line -> parse(line, model));
		}
		catch(Exception e) {
			throw new IOException(String.format("%s at line %d", e.getMessage(), in.getLineNumber()), e);
		}
	}
}
```

The `isComment` method is as follows:

```java
private boolean isComment(String line) {
	return comments.stream().anyMatch(line::startsWith);
}
```

and we add a setter that allows the user to specify the comment token(s).

The `parse()` method first separates the command token from the arguments and then delegates to the `Parser` for that command:

```java
private void parse(String line, ObjectModel model) {
	// Tokenize line
	final String[] parts = StringUtils.split(line, null, 2);

	// Lookup command parser
	final Parser parser = parsers.get(parts[0]);
	if(parser == null) {
		if(ignore) {
			return;
		}
		else {
			throw new IllegalArgumentException(String.format("Unsupported OBJ command: [%s]", parts[0]));
		}
	}

	// Delegate
	final String[] args = StringUtils.split(parts[1]);
	parser.parse(args, model);
}
```

OBJ models are notoriously flakey so the `ignore` flag is another configurable property that determines whether to throw an error if an unknown command is encountered.

The `Parser` is defined as follows:

```java
public interface Parser {
	/**
	 * Parses the given arguments.
	 * @param args 		Arguments
	 * @param model		OBJ model
	 * @throws NumberFormatException is the data cannot be parsed
	 */
	void parse(String[] args, ObjectModel model);

	/**
	 * Parser that ignores the arguments.
	 */
	Parser IGNORE = (args, model) -> {
		// Does nowt
	};
}
```

where the `ObjectModel` is a transient working representation of the OBJ model.

## Vertex Components

To parse the vertex components (position, normal, texture coordinate) we provide a factory method:

```java
/**
 * Creates a parser for a floating-point array command.
 * @param <T> Data-type
 * @param size			Expected size of the data
 * @param ctor			Array constructor
 * @param setter		Model setter method
 * @return New array parser
 */
static <T> Parser of(int size, Function<float[], T> ctor, BiConsumer<ObjectModel, T> setter) {
	return (args, model) -> {
		// Validate
		if(args.length != size) {
			throw new IllegalArgumentException(String.format("Invalid number of tokens: expected=%d actual=%d", size, args.length));
		}

		// Convert to array
		final float[] array = new float[size];
		for(int n = 0; n < size; ++n) {
			array[n] = Float.parseFloat(args[n]);
		}

		// Create object using array constructor
		final T value = ctor.apply(array);

		// Add to transient model
		setter.accept(model, value);
	};
}
```

This parser performs the following:
1. Converts the arguments to a floating-point array
2. Constructs a vertex component from this array
3. Add the result to the transient model

For example, the parser for the vertex position command is defined thus:

```java
Parser.of(Point.SIZE, Point::new, ObjectModel::vertex);
```

We also need to add array constructors to the relevant domain classes.

## Faces

The parser for the face command 
TODO
- optional bits
- lookup()
- add to model

## Building the Model

TODO

We register the default built-in command parsers in the constructor of the loader and provide a method to register further parsers as required:

```java
private void init() {
	add("v", Parser.of(Point.SIZE, Point::new, ObjectModel::vertex));
	add("vt", Parser.of(Coordinate2D.SIZE, Coordinate2D::new, ObjectModel::coord));
	add("vn", Parser.of(Vector.SIZE, Vector::new, ObjectModel::normal));
	add("f", Parser.FACE);
	add("s", Parser.IGNORE);
	add("g", Parser.IGNORE);
}
```

# Indexed Model


