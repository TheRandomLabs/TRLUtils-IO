package com.therandomlabs.utils.io;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

final class AddZipEntryVisitor extends SimpleFileVisitor<Path> {
	private final ZipFile zipFile;
	private final Path sourceDirectory;
	private final CopyOption[] options;

	AddZipEntryVisitor(ZipFile zipFile, Path sourceDirectory, CopyOption[] options) {
		this.zipFile = zipFile;
		this.sourceDirectory = sourceDirectory;
		this.options = options;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
			throws IOException {
		Files.copy(file, zipFile.getEntry(sourceDirectory.relativize(file).toString()), options);
		return FileVisitResult.CONTINUE;
	}
}
