# TRLUtils-IO

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build](https://jitci.com/gh/TheRandomLabs/TRLUtils-IO/svg)](https://jitci.com/gh/TheRandomLabs/TRLUtils-IO)

A collection of utilities that are useful when performing I/O operations.

All public-facing code is documented with Javadoc and (mostly) tested with JUnit.

## Features

* `HumanReadableSize` can be used to convert sizes in bytes to one of the following human-readable
formats:
	* Decimal (SI): kB, MB, GB, TB, GB, PB, EB
	* Binary: KiB, MiB, GiB, TiB, GiB, PiB, EiB
* `IOConstants` contains the following constants:
* `IOConstants#UNIX_DIRECTORY_SEPARATOR` (`'/'`)
	* `IOConstants#WINDOWS_DIRECTORY_SEPARATOR` (`'\\'`)
	* `IOConstants#UNIX_LINE_SEPARATOR` (`"\n"`)
	* `IOConstants#WINDOWS_LINE_SEPARATOR` (`"\r\n"`)
	* `IOConstants#LINE_SEPARATOR` (equivalent to `System#lineSeparator()`)
* `NIOUtils` contains a collection of utility methods for filesystem manipulation using Java NIO,
including `NIOUtils#list(Path)`, `NIOUtils#copyDirectory(Path, Path)` and
`NIOUtils#matchGlob(Path, String)`.
* `PathUtils` contains a collection of utility methods for manipulating paths,
including `PathUtils#getFileName(Path)`, `PathUtils#getCommonAncestor(Path, Path)` and
`PathUtils#withUnixDirectorySeparators(Path)`. No methods in this class access the filesystem.
* `ZipFile` is a very basic representation of a zip file. The main method in this class is the
`ZipFile#getEntry(String)` method, which returns a `Path` which represents a zip file entry.
`Path`s returned by this method can be operated on normally like any other `Path`.

## Using with Gradle

TRLUtils-IO can be found on [Jitpack](https://jitpack.io/):

```groovy
repositories {
	mavenCentral()

	maven {
		url "https://jitpack.io"
	}
}

dependencies {
	implementation "com.github.TheRandomLabs:TRLUtils-IO:v1.0.1"
}
```
