# EtherealPerms

[![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Wiki](https://img.shields.io/badge/wiki-docs-orange.svg)](https://ethereallabs.it/etherealperms-wiki)
[![Discord](https://img.shields.io/badge/discord-join-7289DA.svg?logo=discord)](https://ethereallabs.it/discord)

**EtherealPerms** is a permissions plugin designed for Hytale. It allows server admins to control feature access via a sophisticated system of groups and nodes, featuring a native in-game visual editor.

The latest downloads, documentation and other useful links can be found on our website at [ethereallabs.it](https://ethereallabs.it). Visit our **[Wiki](https://ethereallabs.it/wiki)** for detailed setup guides.

It is:

* **Native** - Built from the ground up for the Hytale Server API using Kotlin.
* **Visual** - Manage permissions effortlessly using the built-in **Hytale UI Editor** (`/perms editor`).
* **Fast** - Optimized with asynchronous I/O and caching to ensure zero main-thread lag.
* **Flexible** - Supports multiple storage backends including **MongoDB**, **MySQL**, and **Local JSON**.
* **Extensible** - Provides a clean, strictly typed API for developers.

## Building

EtherealPerms uses Gradle to handle dependencies and building.

### Requirements
* Java 21 JDK or newer
* Git

### Compiling from source
```sh
git clone https://github.com/Darkeox34/EtherealPerms.git
cd EtherealPerms/
./gradlew shadowJar
```

## Features
- **In-Game Editor:** Full CRUD operations for Users and Groups via a custom Hytale UI.
- **Inheritance:** Robust parent/child group inheritance with weight systems.
- **Chat Formatting:** Built-in chat prefix/suffix handling with priority support.
- **Contexts:** Support for temporary or context-based permissions (WIP).

## Contributing
#### Pull Requests
We welcome contributions! If you'd like to improve the plugin, please create a Pull Request.
See [`CONTRIBUTING.md`](CONTRIBUTING.md) for more details.

## License
EtherealPerms is licensed under the permissive MIT license. Please see [`LICENSE`](LICENSE) for more info.
