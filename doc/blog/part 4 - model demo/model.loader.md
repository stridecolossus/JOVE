# OBJ Model Loader

An OBJ model is a text-based data format consisting of a series of *commands* that define vertices, normals, texture coordinates and faces.
Each line starts with a *command token* followed by a number of arguments.
The most common commands are:

| command 	| arguments 	| purpose 				| example 			|
| -------   | ---------		| -------				| -------			|
| v 	  	| x y x	  		| vertex position		| v 0.1 0.2 0.3 	|
| vn		| x y z			| normal				| vn 0.4 0.5 0.6	|
| vt		| u v			| texture coordinate	| vt 0.7 0.8		|
| f			| see below		| face or triangle		| f 1//3 4//6 7//9	|
| s			| n/a			| smoothing group		| s					|



An OBJ model generally has the following structure:

```
v 1 2 3
v 4 5 6
...
vn 1 2 3
vn 4 5 6
...
vt 1 2
vt 3 4
...
f 1/2/3 4/5/6 7/8/9
...
```

blah blah

```java
final x = 42;
```

fdfjdksfkldsfjsda
dsfadsfasd

