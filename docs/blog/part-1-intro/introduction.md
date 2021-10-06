---
title: Introduction
---

## Introduction

In this first section we set the scene for the project:

* What are we trying to achieve?

* And why?

* What are the goals and design philosophies of the project?

* Why is it Java-based?

* How do we bind to the native Vulkan libraries?

If the reader has no interest in the _why_ and wants to get straight to the nitty-gritty of the _how_ this chapter can easily be skipped.

---

## Background

For several years as a personal project we had developed a Java-based OpenGL library and suite of demo applications.  With the advent of Vulkan we decided to archive the OpenGL project and start afresh with a Vulkan implementation which we named __JOVE__.

This is not a small amount of effort, our rationale for such an undertaking is:

* A general interest in 3D graphics and mathematics.

* The challenge of abstracting a complex native library to an object orientated (OO) design.

* A personal project that can picked up or put aside as mood dictates.

* A test-bed for learning new development approaches, technologies and Java features.

---

## Approach

Although this is a personal project we _will_ strive to apply best practice to our design and development (as we would if it were a professional contract).

In general our goals for this (or any other) project are:

* Maintainability - The prime directive: code should be designed and implemented such that it can be easily extended, refactored and fixed.

* Clarity - The purpose of any code component should be clear and coherent, i.e. we aim for simplicity where possible.

* Testability - All code is developed with testing in mind from the outset.

To attempt to achieve these goals our approach and principles are as follows:

* Implement the minimal functionality (or MVP) to deliver the next objective of the project.

* Aggressive refactoring of existing code where we find we have made questionable design decisions or poorly implemented functionality.

* High test coverage with unit-tests developed _test-first_ or _test-in-parallel_ with the source code.

* Detailed documentation throughout.

* Third-party libraries and technologies must be well-documented and widely supported.

Some of these might seem high-flown or even pointless for a personal project.  However one advantage of being of a certain age is that experience teaches us that following sound development principles from the start avoids pain and bugs in the future (especially for a large project perhaps developed over several years).

On the other hand this _is_ a personal project and we allow ourselves some freedom in our decisions that might not be possible under the constraints of a real-world project - we can reinvent as many wheels as we choose if there is sufficient reason (or challenge) in doing so.

---

## Tutorial Structure

We have attempted to structure this blog so that each chapter builds on the preceding sections to incrementally deliver the functionality of the JOVE library, roughly following the [Vulkan tutorial](https://vulkan-tutorial.com/).

Each chapter generally consists of:

* An introduction that covers the purpose of the functionality to be developed.

* A walk-through of the design and development of new or refactored software components to deliver that functionality, including any challenges or problems that arose.

* A retrospective of any identified improvements and enhancements that lead to refactoring of the existing code-base.

* A summary of the software developed in that chapter.

Development of JOVE will be inherently complex with Vulkan requiring many inter-dependant components.  To adhere to the minimal functionality principle we will often develop temporary or skeleton implementations that are refactored later on.  For example, the graphics pipeline _requires_ a pipeline layout object (which has no relevance at that point) so the initial implementation is an empty skeleton sufficient to progress the project.

---

## Technology Choices

Finally in this introductory chapter we cover the various supporting technologies, frameworks and libraries used in the JOVE project.

On the face of it Java may seem an odd choice for a project which requires interacting with a very complex native library (not a strength of Java).  However it the language with which we have most experience (both personal and professional) and as previously mentioned one of the interesting challenges is how to develop an OO project dependant on a C/C++ native, which is the focus of the next chapter.

We develop using the latest stable JDK release supported by the IDE (Java 16 at the time of writing).  Although not particularly relevant to the blog our IDE of choice is Eclipse (mainly out of habit).

The JOVE library and associated demo applications are implemented as Maven projects backed by a [Git repository](https://github.com/stridecolossus/JOVE).

Besides Vulkan itself the JOVE project also uses the following supporting libraries:

| dependency            | purpose |
| Apache Commons        | General helpful utilities and supporting classes |
| Library               | Argument validation (another personal project) |
| JUnit                 | Unit-testing framework |
| Mockito               | Mocking and stubbing |
| JNA                   | Interaction with native libraries |
| GLFW                  | Management of native windows and input-devices |
| Spring Boot           | Dependency injection for demo applications |

It can be assumed that all libraries use the latest stable release versions.

