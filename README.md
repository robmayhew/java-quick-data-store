# Java Quick Data Store


# Example


# What is it?

QDS is a simple persistent storage system for simple java apps. Inspired by NSUserDefaults in objective-c. Java Quick Datastore provides a simple key value persistent map for java.

QDS is extremely easy to use with as little as one line to save or load persistent data. 

QDS is not a database and should not be used as one. It is only meant for simple use cases like a demo application, or some other experimental code. Given that there is a simple upgrade path when you are ready to use a real database backend.

QDS will store

+ String 
+ boolean
+ int
+ long
+ double
+ Simple Java objects containing the above
+ and java.util.List of the above.

Examples:

Quick and dirty, QDS will pick a filename based on the package the first save or load is called from and place the file in the System.getProperty(‘user.home’) folder.

```java
QDS.save("Testing", "A String");
String s = (String)QDS.load("Testing");
```

You can specify where to store data by calling

```java
QDS.usePath("pathname"); 
```
before save or load to specify exactly where you want the persistent file placed.


To replace the guts of QDS use the useInstance method provinding your own impletation of the QuickDataStoreInterface

```java
QDS.useInstance(QuickDataStoreInterface qds)
```
