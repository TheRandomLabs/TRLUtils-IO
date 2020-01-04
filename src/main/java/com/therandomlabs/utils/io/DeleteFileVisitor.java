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
