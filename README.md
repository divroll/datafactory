<p align="center">
    <img src="https://datafactory.divroll.com/logo_big.png" alt="drawing" width="300"/>
</p>

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## What is DataFactory?

DataFactory is a thin abstraction library to Jetbrains Xodus database with a fluent API. It allows 
a _managed_ access to the underlying database through consistent Java API that works both
in an embedded or in a remote context through Java RMI. 

**Requirements:** JDK 7, Maven

- [Features](#features)
- [Installation](#installation)

Features
---

- It's a drop-in library for any Java project
- Does not require any server setup or administration
- Extendable managed database access through interfaces

Installation
---

```$xslt
$mvn clean install
```

#### Maven
```xml
<dependency>
    <groupId>com.divroll</groupId>
    <artifactId>datafactory</artifactId>
    <version>0-SNAPSHOT</version>
</dependency>
```

#### What's the motivation behind this library? 

Jetbrains Xodus is fast and low-footprint, an embedded database by design, accessing it remotely 
can only be achieved by creating custom endpoints through remote access methods. 

DataFactory provides a generic and fluent API for accessing the Xodus database without hand-coding 
these custom endpoints and just using Java RMI. 

Furthermore, the lack of ability to scale horizontally across multiple JVM's makes it hard to be used 
in a distributed manner. 

With DataFactory, a Xodus database instance running on a vertically-scaled server can be used in along with
horizontally scaling application servers.