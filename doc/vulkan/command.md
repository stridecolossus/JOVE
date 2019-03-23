# Command Pools and Buffers

## Creating a Command Pool

A command pool is created as follows:

```java
final Command.Pool pool = Command.Pool.create(dev, family);
```

where `dev` is the logical device and `family` is a queue family for that device.

A number of command buffers can then be allocated from the pool:

```java
final List<Command.Buffer> buffers = pool.allocate(3, true);
```

The `allocate` method accepts the required number of buffers and a boolean indicating whether they are _primary_ or _secondary_.

## Recording

A command buffer is used to record a series of commands illustrated in the following example:

```java
final Command draw = (lib, buffer) -> lib.vkCmdDraw(buffer, 3, 1, 0, 0);

buffer
	.begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT)
	.add(pass.begin(fb, extent, clear))
	.add(pipeline.bind())
	.add(draw)
	.add(RenderPass.END_COMMAND)
	.end();
```

The buffer tracks its internal state and will throw `IllegalStateException` if used incorrectly, e.g. recording over a buffer that has not been reset. 

## Submitting a Command Buffer to a Queue

Using a command buffer to perform some work requires creating a `Work` instance with the appropriate synchronisation and submitting it to a `WorkQueue`. 

Example:

```java
final WorkQueue.Work work = new WorkQueue.Work.Builder()
	.add(buffer)
	.wait(waitSemaphore)
	.wait(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
	.signal(signalSemaphore)
	.build();

final WorkQueue queue = ...
queue.submit(work);
```

## Command Buffer Lifecycle

A command buffer can be restored to its initial undefined state by the `reset` method.

A command buffer that 

TODO

## Cleanup

The command pool is a resource that should be destroyed when no longer required.

Note that the command buffers themselves are managed by the pool and are not explicitly destroyed.
