---
title: Code Generating the Vulkan API
---

## Overview

For many years we had been developing a personal project for an OpenGL based library and a suite of example applications.
This library was implemented using [LWJGL](https://www.lwjgl.org/) which provides Java bindings for the native OpenGL library (amongst others).

We intended to retire the OpenGL project and start afresh with a Vulkan based 3D engine.
LWJGL had recently implemented a Vulkan port and we expected to be able to use the new bindings to get to grips with the Vulkan API.

However things did not work out as anticipated. Not at all.

As we worked through the [tutorial](https://vulkan-tutorial.com/) we found we were spending more time trying to understand the LWJGL bindings instead of learning how to use Vulkan.

There were a few reasons for this:

- The various Vulkan bindings provided by LWJGL are code-generated from the native library but all of the internal workings are exposed as **public** members and methods.  This completely obfuscates the intended purpose and usage of the class.

- In addition each component provides multiple getter/setters for the same field and a slew of allocation factory methods (over twenty) which presumably have a purpose but are not explained.  Without any documentation there is no clear direction for which methods we should be using, whether there are pros and cons, etc.

- The JavaDoc is also code-generated but is focused on _how_ the code was generated rather than _what_ its actually doing.  The developer is constantly forced to context switch to the Kronos website or the Vulkan header to work out how we should be using the bindings.  (UPDATE: It looks like the LWJGL JavaDoc now includes the relevant documentation which is a significant improvement).

- In the tutorial the application name is a simple string, in LWJGL we had to instantiate a memory stack and invoke a static helper to allocate a buffer for the string.  However there is no explanation for _why_ we need to do this, whether there are alternatives, do we need to release the string, etc.

- This was exacerbated by the lack of decent examples and tutorials - those that we found all seemed to do the same thing in different ways without any explanation of _why_ a certain approach was used.  In addition most examples were basically C code masquerading as Java, with little or no in-code documentation or modularity, resulting in a wall-o-code that was virtually impossible to follow.

- The use of static imports was slightly annoying - to find a given class or method one just has to know its parent package, or add them to the content assist in the IDE, or switch off import organisation altogether and cut-and-paste _every_ import into _every_ source file.

Note this is not intended to be negative review of LWJGL - we have used it with great results in our previous OpenGL project and it is widely used for Vulkan.  Unfortunately our own experience was frankly demoralising and we had barely scratched the surface of the Vulkan API - we eventually gave up in disgust.

Sometime later we were encouraged by a friend to make a second attempt - our first design decision was that unless LWJGL had materially changed we would look for an alternative.

---

## Technology

### Alternatives

Having made the decision not to use LWJGL for the native library bindings we needed an alternative technology.

Straight JNI we immediately discounted - no one in their right mind would choose to implement JNI bindings for an API as large as Vulkan.  It had also (thankfully) been many years since we wrote any C/C++ code and we certainly didn't intend starting now.

There is a on-going JSR for a pure-Java alternative to JNI but it didn't appear to have progressed much (if at all) in the intervening period and there was little prospect of it being available any time soon.

We next looked at SWIG which is the code-generation technology used by LWJGL but we were not encouraged - the tool seems to require descriptors to define the bindings to the native layer and we have already covered our issues with the code-generated results.

Finally we came across JNA - having never had to deal with a native library directly (professionally or for personal projects) it was new to us but our initial impressions were promising:
- The premise of auto-magically mapping Java interfaces and types to the native API was very appealing.
- In particular the support for mapping to C structures would be important given the large number that are used by Vulkan.
- The library seemed to have a large and active user base with plenty of posts on stack-overflow (for example).
- The documentation was generally excellent and there seemed plenty of tutorials and examples available.

We had a possible winner.

### JNA

To see whether JNA would suit our purposes we first tried it against a simpler native library.  We had already planned to use [GLFW](https://www.glfw.org/) for desktop related functionality such as managing windows, displays, input devices, etc. and it also integrates nicely with Vulkan (as we will see later on).

We implemented the bulk of what would become the _desktop_ package of JOVE in a couple of hours, the progress reflecting our initial positive impressions:
- Instantiating the native library was relatively painless.
- Defining a Java interface to represent the native API was also relatively simple with JNA generally providing logical mappings for method parameters.
- Although GLFW doesn't require much in the way of structured data we found using JNA structures to be logical and straight-forward.
- The library also supports callbacks specified as Java interfaces.

On a high we stripped LWJGL from our project and replaced the Vulkan components with hand-crafted JNA interfaces and structures.  We progressed to the point of instantiating the logical device in the space of an hour or so without any of the road-blocks or surprises that LWJGL threw at us.

In particular:
- There are no mysterious management methods and marshalling to/from the native layer is generally transparent - the application name is simply a string.
- Other than the fact that JNA mandates that all structure fields are public the internal workings are largely hidden.
- Where we did come across problems or confusing situations there was plenty of documentation, examples, tutorials, etc. available.

At this point we paused to take stock because of course there was the elephant in the room - Vulkan is a massive API with a large number of API calls, enumerations and structures.
Some of the components are also absolutely huge such as the `VkStructureType` enumeration or the `VkPhysicalDeviceLimits` structure.
Hand-crafting even a fraction of the API could be done but it would be very tedious and the likelihood of introducing errors quite high (e.g. accidentally removing a field).

We needed a code generator.

---

## Generation Game

### Overview

Having decided that JNA was the way to go for the native bindings we still needed some mechanism to actually generate the API.

We first noted down some requirements for the code generator:

- We will consider the generator to be complete once we have generated an acceptable proportion of the API rather than attempting to cover every possible use-case.
i.e. we want to avoid diminishing returns on the time and effort to cover every edge case.

- That said the generated code will be treated as read-only, i.e. we will attempt to avoid fiddling the generated source code where possible.

- The generator will be invoked manually rather than being a part of an automated build process (which makes things _much_ simpler).

- For future versions of the Vulkan API we assume Kronos will take the same approach as OpenGL whereby new iterations of the API are extensions and additions rather than replacements for existing components (i.e. which we can code generate separately).

- Any tools and libraries we use should follow our general goal of being well-documented and supported.

### Tools

We first tried a tool called _JNAeator_ that generates JNA bindings for a given header file, this seemed perfect for our requirements.  Unfortunately the tool generated a seemingly random package structure and the generated code looked more like the SWIG bindings than the nice, neat code we had hand-crafted.  It also used yet another library called _BridJ_ and the fact that it took us some time to find a website for this tool was not encouraging.

So we looked for a more general header parser that we could use to code generate the bindings ourselves.  We expected (probably naively) that there would be some library or tool out there that we could use to parse a C/C++ header to enumerate the structures, enumerations and API methods.

After some research we largely drew a blank - the only option seemed to be an obscure Eclipse component called CDT that is used for code assist.  It wasn't an actual library as such (there is no maven or project page), we had to include a JAR file directly in our project.  CDT builds an AST (Abstract Source Tree?) from a C/C++ source file which is a node-tree representing the various elements of the code.

It did the job but the exercise was very painful:
- CDT isn't a public library so the documentation was virtually non-existent.
- As it turned out most of the information we wanted was mapped to a single node type which largely made CDT pointless for our project.
- Extracting the relevant information was extremely difficult - we didn't understand most of the terminology so it was just blind searching across mysteriously named fields and types.
- The library provided a visitor to walk the tree but required the developer to set public booleans to select the relevant data as well - WTF!
- Perhaps we were missing the point but we seemed to have to keep casting different types of node to find even the most basic information.

In retrospect we spent far too much time messing around with CDT and it certainly doesn't adhere to our goal of only using well documented third-party tools.  If and when we need to re-generate the API we will replace it with a home-brewed parser (unless we find one in the meantime).  For that reason we will largely gloss over the details of how we used CDT.

### Code Generation

To generate the Java enumerations we used [Apache Velocity](https://velocity.apache.org/), an old but active template library ideal for what we were doing.  In particular it provides support for collections of data which would be using for enumeration constants and structure fields.

Generally the code generation process is:
1. Extract the relevant data from the AST.
2. Construct a map of the data to be injected into a template.
3. Invoke the template engine with this map to generate the source file.
4. Write the file.

The generator itself is a simple wrapper for the Velocity engine:

```java
public class TemplateProcessor {
    private final VelocityEngine engine = new VelocityEngine();

    public TemplateProcessor(String prefix) {
        final Properties props = new Properties();
        props.setProperty("resource.loader", "file");
        props.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("file.resource.loader.path", prefix);
        props.setProperty("file.resource.loader.cache", "false");
        engine.init(props);
    }

    public String generate(String name, Map<String, Object> data) {
        // Load template
        final Template template = engine.getTemplate(name);

        // Init context
        final VelocityContext ctx = new VelocityContext(new HashMap<>(data));

        // Generate source
        final StringWriter out = new StringWriter();
        template.merge(ctx, out);
        return out.toString();
    }
}
```

### Enumerations

We started with enumerations as these are the simplest of the components we need to generate.

The following data is extracted from the AST for each enumeration:
* enumeration name
* fields
    * name
    * value

This is the Velocity template for an enumeration:

```java
package $package;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum $name implements IntegerEnumeration {
#foreach($entry in $values.entrySet())
     ${entry.key}($entry.value)#if($foreach.hasNext),#else;#end
     
#end

    private final int value;
    
    private $name(int value) {
        this.value = value;
    }

    @Override
    public int value() {
        return value;
    }
}
```

Notes:

- The tokens prefixed by the dollar character are the injected arguments.

- The various tokens prefixed by the hash character are Velocity directives whose purpose should be fairly self-evident.

- We discuss the purpose of the `IntegerEnumeration` at the end of this chapter.

The line that actually generates a enumeration constant might be slightly confusing at first glance:

```java
${entry.key}($entry.value)#if($foreach.hasNext),#else;#end
```

The purpose of the `if..else..end` directive is to add commas between each constant and a final semi-colon at the end.

Using the generator the following Vulkan enumeration:

```c
typedef enum VkImageUsageFlagBits {
    VK_IMAGE_USAGE_TRANSFER_SRC_BIT = 0x00000001,
    VK_IMAGE_USAGE_TRANSFER_DST_BIT = 0x00000002,
    VK_IMAGE_USAGE_SAMPLED_BIT = 0x00000004,
    VK_IMAGE_USAGE_STORAGE_BIT = 0x00000008,
    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT = 0x00000010,
    VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT = 0x00000020,
    VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT = 0x00000040,
    VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT = 0x00000080,
    VK_IMAGE_USAGE_SHADING_RATE_IMAGE_BIT_NV = 0x00000100,
    VK_IMAGE_USAGE_FRAGMENT_DENSITY_MAP_BIT_EXT = 0x00000200,
    VK_IMAGE_USAGE_FLAG_BITS_MAX_ENUM = 0x7FFFFFFF
} VkImageUsageFlagBits;

typedef VkFlags VkImageUsageFlags;
```

becomes:

```java
public enum VkImageUsageFlag implements IntegerEnumeration {
     VK_IMAGE_USAGE_TRANSFER_SRC_BIT(1),     
     VK_IMAGE_USAGE_TRANSFER_DST_BIT(2),     
     VK_IMAGE_USAGE_SAMPLED_BIT(4),     
     VK_IMAGE_USAGE_STORAGE_BIT(8),     
     VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT(16),     
     VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT(32),     
     VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT(64),     
     VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT(128),     
     VK_IMAGE_USAGE_SHADING_RATE_IMAGE_BIT_NV(256),     
     VK_IMAGE_USAGE_FRAGMENT_DENSITY_MAP_BIT_EXT(512);

    private final int value;
    
    private VkImageUsageFlag(int value) {
        this.value = value;
    }

    @Override
    public int value() {
        return value;
    }
}
```

Notes:

- Enumerations that are bit-masks are suffixed with `Bits` with an additional `typedef` that essentially removes this suffix (as in the example above) - the code generator strips the suffix.

- Most of the enumerations have synthetic values that specify the minimum, maximum, range, etc. of the enumeration - these are discarded as superfluous.

### Structures

The data for a structure is:
* structure name
* fields
    * name
    * type
    * array length

Generation of the structures is slightly more complex as we have to map the Vulkan and native types to the equivalent Java/JNA types.  We implemented a type mapper specified by the following resource:

```java
# Primitives
int8_t             -> byte
uint8_t            -> byte
int32_t            -> int
uint32_t           -> int
int64_t            -> long
uint64_t           -> long
size_t             -> long

# Strings
char*              -> String
char[]             -> byte

# Pointers
void*              -> com.sun.jna.Pointer
char**             -> com.sun.jna.Pointer

# Over-rides
VkBool32           -> org.sarge.jove.platform.vulkan.VulkanBoolean

# Macros
VkInstance         -> org.sarge.jove.Handle
VkPhysicalDevice   -> org.sarge.jove.Handle
VkDevice           -> org.sarge.jove.Handle
...
```

All other types were simply copied as-is (which also neatly handled the case for enumerations).

The template for a Vulkan structure looks like this:

```java
package $package;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
#foreach($import in $imports)
import $import;
#end

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
#foreach($field in $fields)
    "$field.name"#if($foreach.hasNext),
#end
#end

})
public class $name extends VulkanStructure {
#foreach($field in $fields)
#if($field.name == "sType")
    public $field.type $field.name = VkStructureType.VK_STRUCTURE_TYPE_${sType};
#else
#if($field.length == 0)
    public $field.type $field.name;
#else
    public ${field.type}[] $field.name = new ${field.type}[${field.length}];
#end
#end
#end
}
```

Notes:

- We also generate the imports based on the type mappings used for the structure fields.

- The fields in a JNA structure must also be specified in the `@FieldOrder` annotation.

- We also initialise all array type fields - this is required by JNA in order to size the memory of the structure.

All top-level Vulkan structures have the _sType_ field which identifies its type to the native layer.

Since the values in the `VkStructureType` are highly logical and regular we can generate the enumeration constant from the name of the structure:

```java
// Inject the synthetic structure identifier
final String[] tokens = StringUtils.splitByCharacterTypeCamelCase(name.substring(2, name.length()));
final String sType = String.join("_", tokens).toUpperCase();
values.put("sType", sType);
```

For example the type for the `VkApplicationInfo` structure is `VK_STRUCTURE_TYPE_APPLICATION_INFO`.

This saves us the effort of having to manually populate this field when we use the structure - Bonus!

### Methods

In the end we decided not to code generate the API methods for a variety of reasons:

- Although we could re-use the type mapper from the structure generator we anticipate that we _will_ have to manually fiddle with the signatures of the API methods, so we might as well craft them by hand.

- We would also like to group related API methods, both for ease of finding a method and to break up the overall library API.  Obviously the native header has no notion of packaging so we would have to do this grouping manually anyway.

- Finally we also intend to document each method as we introduce it to JOVE, partially for future reference but to also better understand the method.

### Conclusion

The code generator ran in a few milliseconds so we could iteratively modify the code until we achieved an acceptable level of results.

As it turned out there were only two generated structures that didn't automatically compile and since these were for an extension we had never heard of we simply deleted them.

The generator produced 390 structures and 142 enumerations (for version 1.1.101.0).

At the time of writing the API consists of 91 methods so the decision to implement them manually has not been particularly onerous.