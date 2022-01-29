---
title: Introduction
---

---

## Contents

- [Overview](#overview)
- [Background](#background)
- [Approach](#approach)
- [Code Presentation](#code-presentation)
- [Technology Choices](#technology-choices)

---

## Overview

In this first section we set the scene for the project:

* What are we trying to achieve?

* And why?

* What are the goals and design philosophies of the project?

* Why is it Java-based?

* How do we bind to the native Vulkan libraries?

If the reader has no interest in the _why_ and wants to get straight to the nitty-gritty of the _how_ then this chapter can easily be skipped.

---

## Background

For several years as a personal project we had developed a Java-based OpenGL library and suite of demo applications.  With the advent of Vulkan we decided to archive this project and start afresh with a Vulkan implementation named __JOVE__.

This is obviously not a small amount of effort, our rationale for such an undertaking is:

* A general interest in 3D graphics and mathematics.

* The challenge of adapting a complex native library to an object orientated (OO) design.

* A personal project that can picked up or put aside as mood dictates.

* A test-bed for learning new development approaches, technologies and Java features.

We have attempted to structure this blog so that each chapter builds on the preceding sections to incrementally deliver the functionality of the JOVE library, roughly following the [Vulkan tutorial](https://vulkan-tutorial.com/).

Each chapter generally consists of:

* An introduction that covers the goals(s) for that chapter and the functionality to be developed.

* A walk-through of the design and development of new or refactored software components to deliver that functionality, including any challenges or problems that arose.

* One or more _integration_ steps where we use the new functionality to extend the demo applications.

* A retrospective of any identified improvements and enhancements that lead to refactoring of existing code.

---

## Approach

Although this is a personal project we _will_ strive to apply best practice to our design and development as we would if it were a professional contract.

In general our goals are:

* Maintainability - The prime directive: code should be designed and implemented such that it can be easily extended, refactored and fixed.

* Clarity - The purpose of any code component should be clear and coherent, i.e. we aim for simplicity where possible.

* Testability - All code is developed with testing in mind from the outset.

To attempt to achieve these goals our approach and principles are as follows:

* Implement the minimal functionality required to deliver each feature.

* Aggressive refactoring of existing components to avoid code duplication and complexity, or where better understanding of Vulkan invalidates previous design decisions or assumptions.

* Comprehensive argument and state validation to identify incorrect or illogical usage of the software components.

* High test coverage with unit-tests developed _test-first_ or _test-in-parallel_ with the source code.

* Detailed documentation throughout.

* Third-party libraries and technologies must be well-documented and widely supported.

Some of these might seem high-flown or even pointless for a personal project, however experience has taught us that following sound development principles avoids pain and bugs in the future (especially for a large project perhaps developed over several years).

On the other hand this _is_ a personal project and we allow ourselves some freedom in our decisions that might not be possible under the constraints of a real-world project - we can reinvent as many wheels as we choose if there is sufficient reason (or challenge) in doing so.

---

## Design

The general approach for the JOVE project is to design a toolkit of Vulkan functionality that can then be built upon to create higher level features.

The Vulkan API is (in our opinion) extremely well designed, especially considering the limitations of defining abstract data types in C.  The components of the JOVE library will correspond closely to the underlying API and will follow the same naming conventions.

Generally a Vulkan component is configured via a _descriptor_ which is passed to an API method to instantiate the native object.  Therefore JOVE will make extensive use of the _builder_ pattern and/or static factory methods to create domain objects (however constructors are often package-private for testability).

Obviously there is usually some _thrashing_ at the start of a new project employing new technologies, in particular there will be a major change of direction when binding to the native library, as we shall see in the next chapter.

Finally we collect some general design decisions:

* All classes are _immutable_ by default unless there is a compelling reason to provide mutator methods.  In any case the majority of the native Vulkan components are immutable by design, e.g. pipelines, semaphores, etc.  This also has the side benefits of simplifying the design and mitigating risk for multi-threaded code (and Vulkan is designed to support multi-threaded applications from the ground up).

* Unless explicitly stated otherwise __all__ mutable components are considered __not__ thread safe.

* Data transfer operations are implemented using NIO buffers since this is the most convenient 'primitive' supported by the JNA library.

---

## Code Presentation

Source code is presented as fragments interspersed with commentary, rather than (for example) links to the source files followed by a discussion.

We also follow these additional coding guidelines:

* Method arguments can be assumed to be non-null by default unless explicitly specified (and documented) as optional.  Additionally null pointer exceptions are not declared in the method documentation.

* The `var` keyword is used to avoid duplication where the type is complex or long-winded (but is also present in the statement).

* Local variables are `final` by default.

* Latest Java features are used where appropriate or convenient, e.g. lambdas rather than anonymous classes.

* If a coding guideline is broken then this should be explicitly documented in the code.

The following are silently omitted unless their inclusion better illustrates the code:

* In-code comments and JavaDoc

* Local variable `final` modifiers

* Validation

* Trivial constructors, getters and setters

* Trivial equals, hash-code and `toString` implementations

* Exception error messages

* Warnings suppression

* Method `@Override` annotations

* Unit-tests

* Package structure

Note that the presented code represents the state of the JOVE library at that stage of development, even if that code is eventually refactored, replaced, removed, etc.  i.e. the blog is not refactored.

---

## Technology Choices

Finally in this introductory chapter we cover the various supporting technologies, frameworks and libraries used in the JOVE project.

On the face of it Java may seem an odd choice for a project which requires interacting with a very complex native library (not exactly a strength of Java).  However it the language with which we have most experience (both personal and professional) and as previously mentioned one of the interesting challenges is how to develop an OO project dependant on a C/C++ native, which is the focus of the next chapter.

We develop using the latest stable JDK release supported by the IDE (Java 16 at the time of writing).  Although not particularly relevant to the blog our IDE of choice is Eclipse (mainly out of habit).

The JOVE library and associated demo applications are implemented as Maven projects backed by a [Git repository](https://github.com/stridecolossus/JOVE).

Besides Vulkan itself the JOVE project also uses the following supporting libraries:

| __dependency__ | __purpose__ |  __scope__ |
| Apache Commons & Collections  | General helpful utilities and supporting classes | all |
| Library                       | Argument validation (another personal project) | all |
| JUnit                         | Unit-testing framework | testing |
| Mockito                       | Mocking and stubbing | testing |
| JNA                           | Interaction with native libraries | JOVE |
| GLFW                          | Management of native windows and input-devices | JOVE |
| Spring Boot                   | Dependency injection | demos only |

Where _scope_ indicates whether the library is used to support JOVE, unit-testing or demo applications.

It can be assumed that all libraries use the latest stable release versions.

