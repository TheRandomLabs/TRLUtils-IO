package com.therandomlabs.utils.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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

		try {
			NIOUtils.copyDirectory(sourceDirectory, targetDirectory);
		} catch (NoSuchFileException ex) {
			ex.printStackTrace();
		}

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
