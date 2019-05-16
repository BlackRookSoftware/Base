# Black Rook Base

Copyright (c) 2019 Black Rook Software. All rights reserved.  
[https://github.com/BlackRookSoftware/Base](https://github.com/BlackRookSoftware/Base)

### Required Libraries

NONE

### Required Java Modules

[java.base](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/module-summary.html)  

### Introduction

This library serves as a base set of code that all Black Rook projects use.
The code and classes contained herein may be used outside of the library in pieces - the entirety
of this library does not need to be distributed together, nor do all of the contained methods
in each class need to be included - this is meant to be a set of files to alleviate coding headaches
that are otherwise not especially covered by Java's main libraries.

End users are encouraged to pick and choose, mix and match.


### Library

Contained in this release is a series of classes that should be used for
common functions. The javadocs contain basic outlines of each package's contents.

### Compiling with Ant

To compile this library with Apache Ant, type:

	ant compile

To make Maven-compatible JARs of this library (placed in the *build/jar* directory), type:

	ant jar

To make Javadocs (placed in the *build/docs* directory):

	ant javadoc

To compile main and test code and run tests (if any):

	ant test

To make Zip archives of everything (main src/resources, bin, javadocs, placed in the *build/zip* directory):

	ant zip

To compile, JAR, test, and Zip up everything:

	ant release

To clean up everything:

	ant clean
	
### Other

This program and the accompanying materials are made available under the 
terms of the MIT License which accompanies this distribution.

A copy of the MIT License should have been included in this release (LICENSE.txt).
If it was not, please contact us for a copy, or to notify us of a distribution
that has not included it. 
