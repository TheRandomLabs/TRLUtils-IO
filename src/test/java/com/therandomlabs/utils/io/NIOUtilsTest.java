/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2020 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.therandomlabs.utils.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class NIOUtilsTest {
	@Test
	public void listShouldReturnAllDirectoryElements(@TempDir Path tempDirectory)
			throws IOException {
		final List<Path> files = new ArrayList<>(5);

		for (int i = 0; i < 5; i++) {
			files.add(Files.createFile(tempDirectory.resolve("file" + i)));
		}

		assertThat(NIOUtils.list(tempDirectory)).containsAll(files);
	}

	@Test
	public void emptyTreeShouldBeEmpty(@TempDir Path tempDirectory) throws IOException {
		Files.createDirectories(tempDirectory.resolve("a").resolve("b"));
		Files.createDirectories(tempDirectory.resolve("a").resolve("b").resolve("c"));
		assertThat(NIOUtils.isTreeEmpty(tempDirectory)).isTrue();
		Files.createFile(tempDirectory.resolve("a").resolve("b").resolve("c.txt"));
		assertThat(NIOUtils.isTreeEmpty(tempDirectory)).isFalse();
	}

	@Test
	public void parentDirectoryShouldBeCreated(@TempDir Path tempDirectory) throws IOException {
		final Path parent = tempDirectory.resolve("a").resolve("b");
		NIOUtils.ensureParentExists(parent.resolve("c.txt"));
		assertThat(parent).isDirectory();
	}

	@Test
	public void directoryStructureShouldBePreservedWhenCopying(@TempDir Path tempDirectory)
			throws IOException {
		final List<Path> files = Arrays.asList(
				tempDirectory.resolve("a").resolve("b").resolve("c").resolve("d.txt"),
				tempDirectory.resolve("a").resolve("b").resolve("c.txt")
		);

		for (Path file : files) {
			NIOUtils.ensureParentExists(file);
			Files.createFile(file);
		}

		final Path targetDirectory = Files.createDirectory(tempDirectory.resolve("target"));
		NIOUtils.copyPreservingDirectoryStructure(files, targetDirectory);
		assertThat(targetDirectory.resolve("c").resolve("d.txt")).isRegularFile();
		assertThat(targetDirectory.resolve("c.txt")).isRegularFile();
	}

	@Test
	public void directoryShouldBeCopiedRecursively(@TempDir Path tempDirectory) throws IOException {
		final Path sourceDirectory = Files.createDirectory(tempDirectory.resolve("source"));
		final List<Path> files = Arrays.asList(
				sourceDirectory.resolve("a.txt"),
				sourceDirectory.resolve("a").resolve("b.txt"),
				sourceDirectory.resolve("a").resolve("b").resolve("c.txt")
		);

		for (Path file : files) {
			NIOUtils.ensureParentExists(file);
			Files.createFile(file);
		}

		final Path targetDirectory = tempDirectory.resolve("target");

		NIOUtils.copyDirectory(sourceDirectory, targetDirectory);
		assertThat(targetDirectory.resolve("a.txt")).isRegularFile();
		assertThat(targetDirectory.resolve("a").resolve("b.txt")).isRegularFile();
		assertThat(targetDirectory.resolve("a").resolve("b").resolve("c.txt")).isRegularFile();
	}

	@Test
	public void directoryShouldBeDeletedRecursively(@TempDir Path tempDirectory)
			throws IOException {
		final Path directory = tempDirectory.resolve("a");
		final Path file = directory.resolve("b").resolve("c").resolve("d.txt");
		NIOUtils.ensureParentExists(file);
		Files.createFile(file);
		NIOUtils.deleteDirectory(directory);
		assertThat(directory).doesNotExist();
	}

	@Test
	public void onlyFilesShouldBeDeleted(@TempDir Path tempDirectory) throws IOException {
		final Path directory = tempDirectory.resolve("a");
		final Path file = directory.resolve("b").resolve("c").resolve("d.txt");
		NIOUtils.ensureParentExists(file);
		Files.createFile(file);
		NIOUtils.deleteInDirectory(directory, Files::isRegularFile);
		assertThat(file.getParent()).isDirectory();
		assertThat(file).doesNotExist();
	}

	@Test
	public void directoryShouldBeDeletedIfExists(@TempDir Path tempDirectory) throws IOException {
		final Path directory1 = tempDirectory.resolve("a");
		Files.createDirectories(directory1.resolve("b"));
		final Path directory2 = tempDirectory.resolve("c");
		assertThat(NIOUtils.deleteDirectoryIfExists(directory1)).isTrue();
		assertThat(directory1).doesNotExist();
		assertThat(NIOUtils.deleteDirectoryIfExists(directory2)).isFalse();
	}

	@Test
	public void globShouldBeMatched(@TempDir Path tempDirectory) throws IOException {
		final List<Path> files = Arrays.asList(
				tempDirectory.resolve("aa").resolve("bb.txt"),
				tempDirectory.resolve("aa").resolve("bc.txt"),
				tempDirectory.resolve("ab").resolve("bb.txt"),
				tempDirectory.resolve("ab").resolve("ab.txt"),
				tempDirectory.resolve("bb").resolve("bb.txt")
		);

		for (Path file : files) {
			NIOUtils.ensureParentExists(file);
			Files.createFile(file);
		}

		assertThat(NIOUtils.matchGlob(tempDirectory, "a*/b*.txt")).hasSize(3);
	}
}
