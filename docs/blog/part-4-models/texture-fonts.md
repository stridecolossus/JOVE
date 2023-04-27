---
title: Rendering Text
---

---

## Contents

- [Overview](#overview)
- [Glyphs](#glyphs)
- [Metadata](#glyph-metadata)

---

## Overview

Rendering text using Vulkan is (or can be) a surprisingly complex challenge for a variety of reasons:

* Most of the available frameworks (including the Java AWT implementation) are ultimately based on TrueType fonts where the various character _glphys_ are represented under-the-hood using splines and vectors, which are then converted to bitmaps for rendering.  However the GPU rendering pipeline is specialised around shaders and only works in terms of vertices and texture coordinates.  Therefore the most common approach is to create a texture containing the glyphs for a given font and point size (usually arranged as a grid).  Arbitrary text can be then rendered as a vertex buffer comprising a quad for each character with texture coordinates derived from the grid.

* Unless the font is mono-spaced the character glyphs have different widths, known as the _advance_ of each character, implying that each glyph also requires additional metadata for layout purposes.

* Multi-line text implies a rendering approach where each word is wrapped against a _margin_ and optionally justified or centred, with further metadata for font heights and line spacing.

* Many fonts also alter the spacing between certain pairs of characters to achieve a better looking result (known as _kerning_ pairs), a good example is the capital A and W characters which can be rendered slightly overlapping each other.  Unfortunately the necessary kerning information is often not visible requiring some sort of off-line utility that generates both the texture font and the metadata on a glyph-by-glyph basis.

* Depending on the specific font certain groups of characters can also be combined into a single _ligature_ glyph, for example lower-case `i` following `f` in Times New Roman fonts.

* All of the above imply that colour blending is required for text rendering given that glyphs can often overlap.

Note that for the moment ligatures and non-Latin fonts will be out-of-scope.

The following new components will be required:

* A representation of the glyph metadata.

* The off-line tool to generate the texture font and metadata.

* A custom mesh builder that constructs the vertex buffer for a given piece of text, including word and line-wrapping.

* The colour blending pipeline stage.

TODO - static vs dynamic (i.e. add char)

Initially we will generate a texture image for a simple mono-spaced demo before addressing font metadata and proper spacing with colour blending later in the chapter.

---

## Glyphs

### Glyph Font

The following new type specifies the metadata for a glyph character:

```java
public record Glyph(float advance)
```

Where _advance_ defines the _cursor_ position of the following character.

A glyph _font_ describes a set of glyphs which are assumed to be arranged as a grid in the associated texture:

```java
public class GlyphFont {
    private final int start;
    private final List<Glyph> glyphs;
    private final int tiles;
}
```

Where _tiles_ specifies the granularity of the texture font, i.e. the number of rows and columns in the grid.

The _start_ property is the index of first character in the font, glyphs can then be retrieved relative to this offset:

```java
public Glyph glyph(char ch) {
    return glyphs.get(ch - start);
}
```

### Glyph Mesh Builder

The font is composed into a new mesh builder implementation for glyph-based text:

```java
public class GlyphMeshBuilder {
    private final GlyphFont font;
    private final MeshBuilder mesh = new MeshBuilder(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT, Coordinate2D.LAYOUT));
    private final int tiles;
    private final float size;
    private float scale = 1;
    private float x, y;

    public GlyphMeshBuilder(GlyphFont font) {
        this.font = notNull(font);
        this.tiles = font.tiles();
        this.size = 1f / tiles;
    }
}
```

Where:

* The _scale_ sets the size of the rendered text.

* And _x_ and _y_ specifies the _cursor_ position of the next character to be added to the mesh.

The builder generates a quad for each character added to the mesh:

```java
public GlyphMeshBuilder add(char ch) {
    // Lookup glyph for this character
    Glyph glyph = font.glyph(ch);

    // Render glyph
    if(!Character.isWhitespace(ch)) {
        build(ch);
    }

    // Advance cursor for next character
    x += glyph.advance();

    return this;
}
```

Note that whitespace characters only advance the cursor since there is no point rendering blank glyphs.

The quad for each character is comprised of two counter-clockwise triangles:

```java
```

The texture coordinates for each quad are calculated by the following helper:

```java
private Corners corners(int index) {
    float u = (index % tiles) * size;
    float v = (index / tiles) * size;
    var topLeft = new Coordinate2D(u, v);
    var bottomRight = new Coordinate2D(u + size, v + size);
    return new Corners(topLeft, bottomRight);
}
```

Where `Corners` is a simple convenience type for a pair of texture coordinate corners.

This implementation assumes that the glyphs are arranged in row-major order in the texture.

### Glyph Texture

The final step is to actually generate the texture font image that will be used by the above mesh builder.

There are many utility applications in the wild that generate texture fonts and the associated metadata, however the data formats are often very strange (and there does not appear to be many standards) which would require tedious formatting and/or parsing.  Additionally many of the tools do not export all the required metadata (particularly kerning properties).  Our requirements are fairly trivial so it makes sense to create our own custom tool using Java AWT fonts.

The generator is essentially a builder with the following properties:

```java
public class TextureFontGenerator {
    private int size = 512;
    private int tiles = 16;
    private char start = 0;
    private Color back = new Color(0, 0, 0, 0);
    private Color text = Color.WHITE;
}
```

Where:

* The _size_ is the pixel dimensions of the texture (which is assumed to be square).

* And _tiles_ is the number of glyphs in each direction, i.e. a 16 x 16 grid.

The texture image and metadata are generated by an _instance_ of the generator for a given font:

```java
public class Instance {
    private final Font font;

    public BufferedImage image() {
        ...
    }

    public GlyphFont metadata() {
        ...
    }
}
```

The `image` method creates the actual texture:

```java    
// Create texture font image
var image = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);

// Clear translucent background
var g = (Graphics2D) image.getGraphics();
g.setBackground(back);
g.clearRect(0, 0, size, size);

// Set text colour
g.setFont(font);
g.setColor(text);

...

return image;
```

Each character in the configured range is then drawn to the image as a grid:

```java
int w = size / tiles;
int h = g.getFontMetrics().getHeight();
char ch = start;
for(int r = 0; r < tiles; ++r) {
    int y = r * w + h;
    for(int c = 0; c < tiles; ++c) {
        int x = c * w;
        g.drawChars(new char[]{ch}, 0, 1, x, y);
        ++ch;
    }
}
```

### Command Line Utility

To wrap the generator as a command line utility we employed the rather neat [picocli](https://github.com/remkop/picocli#readme) library that handles argument parsing and auto-magically generates error messages, help and command completion.

First a command line runner is created that defines the various application parameters and options:

```java
@Command(name="texturefont", description="Generates the image grid and associated metadata for a texture font")
private static class Runner implements Callable<Integer> {
    private enum Style {
        PLAIN,
        BOLD,
        ITALIC
    }

    @Parameters(index="0", description="Name of the font to be generated")
    private String name;

    @Parameters(index="1", description="Point size")
    private int size;

    @Option(names={"-s", "--style"}, description="Font style: ${COMPLETION-CANDIDATES}")
    private Style style = Style.PLAIN;

    @Option(names={"-k", "--kerning"}, description="Disable kerning")
    private boolean kerning = true;
}
```

Note that a local enumeration is defined for the various font styles which is more explicit than the plain integer constants used in the AWT `Font` class (using the same ordinal values).

The runner creates the font from the supplied parameters and outputs the resultant texture image:

```java
public Integer call() throws Exception {
    Font font = new Font(name, style.ordinal(), size);
    var generator = new TextureFontGenerator();
    Instance instance = generator.new Instance(font);
    BufferedImage image = instance.image();
    String filename = String.format("%s%d.png", font.getFontName(), font.getSize());
    ImageIO.write(image, ext, new File(filename));
    return 0;
}
```

Which is invoked as follows:

```java
public static void main(String[] args) {
    var cmd = new CommandLine(new Runner());
    int exit = cmd.execute(args);
    System.exit(exit);
}
```

If a required parameter is missing or a supplied option is invalid `picocli` generates a handy usage message, for example:

```java
Invalid value for option '--style': expected one of [PLAIN, BOLD, ITALIC] (case-sensitive) but was 'COBBLERS'
Usage: texturefont [-k] [-s=<style>] <name> <size>
      <name>            Name of the font to be generated
      <size>            Point size
  -k, --kerning         Disable kerning
  -s, --style=<style>   Font style: PLAIN, BOLD, ITALIC
```

Cool.

### Integration #1

To exercise the texture font the image is generated by the command line utility which should produce something along these lines:

![Texture Font](text.font.png)

Next a new demo application is created consisting of:

* A rendering pipeline with the basic vertex and texture sampling fragment shaders.

* A texture for the font image.

* And a vertex buffer for the text mesh.

The VBO is configured with a hard-coded glyph font for the moment:

```java
@Configuration
public class VertexBufferConfiguration {
    @Bean
    static GlyphFont font() {
        float advance = 1 / 32f;
        var glyph = new Glyph(advance);
        return new GlyphFont(0, Collections.nCopies(256, glyph), 16);
    }

    @Bean
    static Mesh mesh(GlyphFont font) {
        return new GlyphMeshBuilder(font)
            .scale(2)
            .add("Hello, world!")
            .mesh();
}
```

Running the new demo should generate a fairly simple (if ugly) piece of mono-spaced text:

![Text Rendering Example](text.monospaced.png)

---

## Glyph Metadata

### Persistence

To correctly space the text the mesh builder also needs the metadata for the glyph font, which will be persisted as a YAML document.

First the list of glyphs in the font are transformed:

```java
public static void write(GlyphFont font, Writer out) {
    var glyphs = font.glyphs
        .stream()
        .map(Loader::write)
        .toList();
}
```

Using the following helper to output the properties for a given glyph:

```java
private static Object write(Glyph glyph) {
    var map = new HashMap<String, Object>();
    map.put("advance", glyph.advance());
    return map;
}
```

Next a map is created for the properties of the font including the glyphs:

```java
var data = Map.of(
    "start",    font.start,
    "tiles",    font.tiles,
    "glyphs",   glyphs
);
```

Which is then output as a YAML document:

```java
Yaml yaml = new Yaml();
yaml.dump(data, out);
```

The persistence code is wrapped into a new loader component:

```java
public static class Loader implements ResourceLoader<Element, GlyphFont> {
    private final YamlLoader loader = new YamlLoader();

    public Element map(InputStream in) throws IOException {
        return loader.load(new InputStreamReader(in));
    }
}
```

Where the `Element` class is a custom wrapper for node-based documents such as XML or YAML that somewhat simplifies document parsing.

The `load` method parses the document to a glyph font:

```java
public GlyphFont load(Element doc) throws Exception {
    int start = doc.text("start").map(Integer::parseInt).orElse(0);
    int tiles = doc.text("tiles").map(Integer::parseInt).orElse(16);
    List<Glyph> glyphs = doc.child("glyphs").children().map(Loader::glyph).toList();
    return new GlyphFont(start, glyphs, tiles);
}
```

Where each glyph is parsed by the following helper method:

```java
private static Glyph glyph(Element doc) {
    float advance = doc.child("advance").transform(Float::parseFloat);
    return new Glyph(advance);
}
```

### Integration #2

The texture font utility is extended to generate the glyph metadata as a second step after creating the image:

```java
public GlyphFont metadata() {
    // Retrieve font metrics
    List<Glyph> glyphs = IntStream
        .range(start, end())
        .mapToObj(this::glyph)
        .toList();

    // Create glyph font
    return new GlyphFont(start, glyphs, tiles);
}
```

Where `end` determines the end character based on the size of the texture, which for the moment is assumed to be a contiguous range:

```java
private int end() {
    return start + tiles * tiles;
}
```

The character advance of each glyph character is determined from the `metrics` of the font scaled to the texture:

```java
Glyph glyph(int ch) {
    int advance = metrics.charWidth(ch);
    float scaled = advance / (float) size;
    return new Glyph(scaled);
}
```

The generated metadata is then output along with the texture image:

```java
public Integer call() throws Exception {
    ...
    GlyphFont.Loader.write(metadata, new FileWriter(filename(font, "yaml")));
}
```

Where `filename` is a local helper to build the filename for a given font:

```java
private static String filename(Font font, String ext) {
    return String.format("%s%d.%s", font.getFontName(), font.getSize(), ext);
}
```

In the application code the hard-coded bean is modified to load the generated metadata:

```java
static GlyphFont font(DataSource classpath) {
    var loader = new ResourceLoaderAdapter<>(classpath, new GlyphFont.Loader());
    return loader.load("Arial16.yaml");
}
```

After running the utility to generate the metadata, the demo should now render the text spaced according to the character advance of each glyph:

![Overlapping Glyphs](text.spaced.png)

### Kerning

With a basic persistence mechanism in place the remaining metadata can be implemented.

First the glyph class is modified to include kerning pairs:

```java
public record Glyph(float advance, Map<Character, Float> kerning) {
    public static final Map<Character, Float> DEFAULT_KERNING = Map.of();
}
```

A second `advance` method is added to determine the advance of a glyph based on the _following_ character falling back to the default if there is no kerning for a given pair:

```java
public float advance(char next) {
    return kerning.getOrDefault(next, advance);
}
```

The glyph font loader is modified to load the kerning pairs from the metadata:

```java
private static Glyph glyph(Element doc) {
    float advance = doc.child("advance").transform(Float::parseFloat);
    var kerning = doc.optional("kerning").map(Loader::kerning).orElse(Glyph.DEFAULT_KERNING);
    return new Glyph(advance, kerning);
}
```

Which consists of a list of pairs:

```java
private static Map<Character, Float> kerning(Element doc) {
    return doc
        .children()
        .map(Loader::pair)
        .collect(toMap(Entry::getKey, Entry::getValue));
}
```

Each comprising the following character and the advance override:

```java
private static Entry<Character, Float> pair(Element doc) {
    char ch = doc.name().charAt(0);
    float advance = doc.text().transform(Float::parseFloat);
    return Map.entry(ch, advance);
}
```

The output method is also modified accordingly:

```java
private static Object write(Glyph glyph) {
    ...
    var kerning = glyph.kerning();
    if(!kerning.isEmpty()) {
        map.put("kerning", kerning);
    }
    return map;
}
```

The final required change is to activate kerning in the generator when the AWT font is instantiated:

```java
private Font font() {
    Font font = new Font(name, style.ordinal(), size);
    if(kerning) {
        return font.deriveFont(Map.of(TextAttribute.KERNING, TextAttribute.KERNING_ON));
    }
    else {
        return font;
    }
}
```

After regenerated the metadata the demo should now take kerning pairs into account when rendering the text, however glyphs can now overlap


### Colour Blending

The texture font has a translucent black background but at the moment the alpha channel is ignored, to correctly render overlapping glyphs the _colour blending_ pipeline stage is required.

There are two mutually exclusive approaches for colour blending:

* Global bitwise combination specified by the `VkPipelineColorBlendStateCreateInfo` structure.

* Per framebuffer configuration (the more common approach) specified by a `VkPipelineColorBlendAttachmentState` for each framebuffer attachment.

The pipeline stage builder is defined as follows:

```java
public class ColourBlendStageBuilder extends AbstractStageBuilder<VkPipelineColorBlendStateCreateInfo> {
    private final VkPipelineColorBlendStateCreateInfo info = new VkPipelineColorBlendStateCreateInfo();
    private final List<AttachmentBuilder> attachments = new ArrayList<>();

    public ColourBlendStageBuilder() {
        info.flags = 0;
        info.logicOpEnable = false;
        info.logicOp = VkLogicOp.COPY;
        Arrays.fill(info.blendConstants, 1);
    }
}
```

The text demo will use the second approach which requires a nested builder for the attachment colour blending configuration:

```java
public AttachmentBuilder attachment() {
    var builder = new AttachmentBuilder();
    builder.enabled = true;
    attachments.add(builder);
    return builder;
}
```

The attachment configuration specifies the source and destination blend factors for the colour and alpha channels:

```java
public class AttachmentBuilder {
    private static final List<VkColorComponent> MASK = Arrays.asList(VkColorComponent.values());

    private final BlendOperationBuilder colour = new BlendOperationBuilder(VkBlendFactor.SRC_ALPHA, VkBlendFactor.ONE_MINUS_SRC_ALPHA);
    private final BlendOperationBuilder alpha = new BlendOperationBuilder(VkBlendFactor.ONE, VkBlendFactor.ZERO);
    private List<VkColorComponent> mask = MASK;
    private boolean enabled;
}
```

Which populates the relevant structure as follows:

```java
private void populate(VkPipelineColorBlendAttachmentState info) {
    // Init descriptor
    info.blendEnable = enabled;

    // Init colour blending operation
    info.srcColorBlendFactor = colour.src;
    info.dstColorBlendFactor = colour.dest;
    info.colorBlendOp = colour.blend;

    // Init alpha blending operation
    info.srcAlphaBlendFactor = alpha.src;
    info.dstAlphaBlendFactor = alpha.dest;
    info.alphaBlendOp = alpha.blend;

    // Init colour write mask
    info.colorWriteMask = new BitMask<>(mask);
}
```

And is added to the global settings:

```java
VkPipelineColorBlendStateCreateInfo get() {
    if(attachments.isEmpty()) {
        attachments.add(new AttachmentBuilder());
    }

    info.attachmentCount = attachments.size();
    info.pAttachments = StructureCollector.pointer(attachments, new VkPipelineColorBlendAttachmentState(), AttachmentBuilder::populate);
    return info;
}
```

Note that at least one attachment must be configured, hence the logic in the above builder code to add a default attachment with colour blending disabled.

The only integration change is to enable colour blending in the pipeline configuration of the demo:

```java
public Pipeline pipeline(...) {
    return new GraphicsPipelineBuilder(pass)
        ...
        .blend()
            .attachment()
                .build()
            .build()
        .build(...)
}
```

The demo should now render the kerning pairs without overlapping glyphs:

![Kerning Pairs](text.kerning.png)

---

## Summary

In this chapter the following new components were introduced:

- The `Glyph` and `GlyphFont` classes to model and persist a texture font image and associated metadata.

- The `GlyphMeshBuilder` used to construct a vertex buffer of glyphs for a piece of text.

- A utility application to generate a texture font image and associated metadata.

- The colour blending pipeline stage.
