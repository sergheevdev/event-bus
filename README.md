# :bus: Event Bus

[![Apache 2.0 License](https://img.shields.io/badge/License-Apache%202.0-brightgreen.svg)](https://github.com/sergheevdev/event-bus/blob/main/LICENSE)

## Introduction

**Event Bus** is a simple, lightweight and thread-safe, event bus that comprises an entire 
event-driven architecture in a library suitable for all kind of size projects.

This library provides a set of unified interfaces and an implementation for this architecture,
allowing you to easily define and manage events, listeners and handlers, in simple or even 
complex environments.

### Features
- **Thread-safety** (for concurrent operations when already serving, i.e., runtime loading/unloading of listeners and events).
- **Custom annotations** (to ease the management events/listeners, and to create a very simple DSL for comprehensibility).
- **High flexibility** and very extensible library (the client can override or even create their own implementation from spec).
- **Non-Dependant on Frameworks** (you can use it on a small project without having a whole framework as dependency).

### TODO
1. Reach 100% code coverage (with JUnit, Mockito and JaCoCo).
2. Add Spring Boot integration (event-listener provisioning, custom builder, etc.).

### Story

This project started as a need for an **adapter** for my end-of-degree project. My project was 
about having a **dynamic algorithmic cluster computation framework**, which would allow me to 
switch, or even integrate, extend or remove, server behavior at runtime, to make this possible
I needed a base event-driven architecture.

When I was twelve I used to code Minecraft plugins, so I had some (black-box) experience with
event-driven architecture (I knew how to use annotations but not the inner workings of them).

I reviewed the Bukkit (Minecraft Server Core) code about the events, but found it messy, so
I decided to start an adventure from scratch, I knew I needed to model three concepts: events,
listeners and handlers, and after several refactorings and evolutions this code is the result.

## Getting started

0. Make sure you have Java version 8 installed. 
1. Package the library core with [maven](https://maven.apache.org/) into a JAR file.
2. Upload it to your favourite artifact repository (online, self-hosted, or even easier, install to your local m2 cache).
3. Add the artifact as a dependency to your project.
4. Enjoy your new event bus library!

## Usage

All the documentation and tutorials about the usage of this event bus is available
at the [wiki page of this repository](https://github.com/sergheevdev/event-bus/wiki).

## License

[Apache 2.0](LICENSE) &copy; Serghei Sergheev
