package com.therandomlabs.utils.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

final class DeleteFileVisitor extends SimpleFileVisitor<Path> {
	private final Path baseDirectory;
	private final Predicate<Path> filter;

	DeleteFileVisitor(Path baseDirectory, Predicate<Path> filter) {
		this.baseDirectory = baseDirectory;
		this.filter = filter;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
			throws IOException {
		if (filter.test(file)) {
			Files.delete(file);
		}

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path directory, IOException ex)
			throws IOException {
		if (ex != null) {
			return FileVisitResult.TERMINATE;
		}

		if (!directory.equals(baseDirectory) && filter.test(directory)) {
			Files.walkFileTree(directory, new DeleteFileVisitor(directory, path -> true));
			Files.delete(directory);
		}

		return FileVisitResult.CONTINUE;
	}
}
