package com.therandomlabs.utils.io;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

final class CopyFileVisitor extends SimpleFileVisitor<Path> {
	private final Path targetDirectory;
	private final CopyOption[] options;
	private Path sourceDirectory;

	CopyFileVisitor(Path targetDirectory, CopyOption[] options) {
		this.targetDirectory = targetDirectory;
		this.options = options;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes)
			throws IOException {
		if (sourceDirectory == null) {
			sourceDirectory = directory;
		} else {
			Files.createDirectories(targetDirectory.resolve(sourceDirectory.relativize(directory)));
		}

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
		Files.copy(file, targetDirectory.resolve(sourceDirectory.relativize(file)), options);
		return FileVisitResult.CONTINUE;
	}
}
