<p align="center">
    <img src="https://gravity.divroll.com/logo_big.png" alt="drawing" width="300"/>
</p>

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## What is Gravity?

Gravity is an abstraction library with fluent API to Jetbrains Xodus database for remote accessing
the Xodus database through Java RMI. 

**Requirements:** JDK 7, Maven

- [Features](#features)
- [Installation](#installation)

Features
---

- It's a drop-in library for any Java project
- Does not require any server setup or administration

Installation
---

```$xslt
$mvn clean install
```

and add to your project dependency: 

```$xslt
<dependency>
    <groupId>com.divroll</groupId>
    <artifactId>gravity</artifactId>
    <version>0-SNAPSHOT</version>
</dependency>
```

#### What's the motivation behind this library? 

Jetbrains Xodus is fast and low-footprint, an embedded database by design, accessing it remotely 
can only be achieved by creating custom endpoints through remote access methods. 

Gravity provides a generic and fluent API for accessing the Xodus database without hand-coding 
these custom endpoints and just using Java RMI. 

Furthermore, the lack of ability to scale horizontally across multiple JVM's makes it hard to be used 
in a distributed manner. 

With Gravity, a Xodus database instance running on a vertically-scaled server can be used in along with
horizontally scaling application servers.