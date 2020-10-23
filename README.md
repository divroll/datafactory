<p align="center">
    <img src="https://datafactory.divroll.com/logo_big.png" alt="drawing" width="300"/>
</p>

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## What is DataFactory?

DataFactory is a library for [Jetbrains Xodus](https://github.com/JetBrains/xodus) database that provides 
compute abstraction layer through **Actions** and **Conditions** with a fluent API. It allows 
a _managed_ access to the underlying database through consistent Java API that works both
in an embedded or in a remote context through Java RMI. 

**Requirements:** JDK 8, Maven

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

#### Usage

```java
EntityStore entityStore 
    = isClientMode ? DataFactoryClient.getInstance().getEntityStore() 
          : DataFactory.getInstance().getEntityStore();
```

#### Basic Operation

The basic operation for the `EntityStore` are:

- **Save** which is used both for saving a single entity or multiple entities
- **Get** which is used for getting a single entity or multiple entities 
- **Delete** which is used for deleting a single or multiple entities

All operation, single or multiple entities operates in a transaction. 
Thus, it follows the concept of _all-or-nothing_. 

##### Save Operation

```java
DataFactoryEntity dataFactoryEntity = new DataFactoryEntityBuilder()
    .environment(PROD_ENVIRONMENT)
    .entityType("Room")
    .putPropertyMap("address", "Room 123, 456 Street, 789 Avenue")
    .build();

dataFactoryEntity = entityStore.saveEntity(dataFactoryEntity).get();
```

#### Actions

Actions execute as part of the save operation. There as built-in actions which 
are listed [here,](https://sourcegraph.com/github.com/divroll/datafactory@master/-/tree/src/main/java/com/divroll/datafactory/actions) 
but custom actions can also be used. An example implementation can be found [here](https://sourcegraph.com/github.com/divroll/datafactory@master/-/blob/src/test/java/com/divroll/datafactory/actions/IncrementLikesAction.java)

```java
entityStore.saveEntity(new DataFactoryEntityBuilder()
    .environment(PROD_ENVIRONMENT)
    .entityId(dataFactoryEntity.entityId())
    .addActions(new IncrementLikesAction(100))
    .build());
```

#### Conditions

Conditions provides the facility to save (or most usually, to update) an entity based on specific conditions.
Throwing exception when condition is not satisfied. There are built-in conditions that can be used 
off-the-shelf which can be found [here](https://sourcegraph.com/github.com/divroll/datafactory@master/-/tree/src/main/java/com/divroll/datafactory/conditions) 
however it also possible to create custom conditions as such:

```java
dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
    .environment(PROD_ENVIRONMENT)
    .entityId(dataFactoryEntity.entityId())
    .addConditions(new HasBeenLikedThisWeekCondition())
    .addActions(new IncrementLikesAction(100))
    .build()).get();
```

The above example shows that the save operation will only succeed if the condition 
`HasBeenLikedThisWeekCondition`, and will execute the action accordingly if satisfied.
Both the condition and the action here are self-explanatory but you can check the 
details [here.](https://sourcegraph.com/github.com/divroll/datafactory@master/-/blob/src/test/java/com/divroll/datafactory/conditions/CustomConditionTest.java)
 

#### What's the motivation behind this library? 

Jetbrains Xodus is fast and low-footprint, an embedded database by design, accessing it remotely 
can only be achieved by creating custom endpoints through remote access methods. 

DataFactory provides a generic and fluent API for accessing the Xodus database without hand-coding 
these custom endpoints and just using Java RMI. 

Furthermore, the lack of ability to scale horizontally across multiple JVM's makes it hard to be used 
in a distributed manner. 

With DataFactory, a Xodus database instance running on a vertically-scaled server can be used in along with
horizontally scaling application servers.