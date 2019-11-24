package com.therandomlabs.utils.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;

public final class NIOUtils {
	private NIOUtils() {}

	public static List<Path> list(Path directory) throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(directory), "directory should be a directory"
		);

		try (final Stream<Path> list = Files.list(directory)) {
			return list.collect(Collectors.toList());
		}
	}

	public static boolean isTreeEmpty(Path directory) throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(directory), "directory should be a directory"
		);

		final List<Path> children = list(directory);

		for (int i = 0; i < children.size(); i++) {
			final Path child = children.get(i);

			if (!Files.isDirectory(child)) {
				return false;
			}

			children.addAll(list(child));
		}

		return true;
	}

	public static void touch(Path path) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");

		if (!Files.exists(path)) {
			Files.createFile(path);
		}

		final long millis = System.currentTimeMillis();

		Files.setLastModifiedTime(path, FileTime.fromMillis(millis));

		if (Files.getLastModifiedTime(path).toMillis() != millis) {
			throw new IOException("Could not set last modification time: " + path);
		}
	}

	public static boolean canTouch(Path path) {
		Preconditions.checkNotNull(path, "path should not be null");

		try {
			final FileTime originalTime = Files.getLastModifiedTime(path);
			touch(path);
			Files.setLastModifiedTime(path, originalTime);
			return true;
		} catch (IOException ignored) {}

		return false;
	}

	public static void ensureParentExists(Path path) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");

		final Path parent = path.getParent();

		if (parent != null) {
			Files.createDirectories(parent);
		}
	}

	public static String readFile(Path path, Charset charset) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");
		Preconditions.checkArgument(Files.isRegularFile(path), "path should be a file");
		Preconditions.checkNotNull(charset, "charset should not be null");
		return new String(Files.readAllBytes(path), charset);
	}

	public static Path write(Path path, String content, Charset encoding) throws IOException {
		return write(path, content, encoding, true);
	}

	public static Path write(Path path, String content, Charset encoding, boolean forceEndNewline)
			throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");
		Preconditions.checkNotNull(content, "content should not be null");
		Preconditions.checkNotNull(encoding, "encoding should not be null");

		if (forceEndNewline && !content.endsWith("\n")) {
			content += IOUtils.LINE_SEPARATOR;
		}

		return Files.write(path, content.getBytes(encoding));
	}

	public static void copyDirectory(
			Path sourceDirectory, Path targetDirectory, CopyOption... options
	) throws IOException {
		Preconditions.checkNotNull(sourceDirectory, "sourceDirectory should not be null");
		Preconditions.checkNotNull(targetDirectory, "targetDirectory should not be null");
		Files.walkFileTree(sourceDirectory, new CopyFileVisitor(targetDirectory, options));
	}

	public static void deleteDirectory(Path directory) throws IOException {
		deleteDirectory(directory, pathToTest -> true);
	}

	public static void deleteDirectory(Path directory, Predicate<Path> filter) throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(directory), "directory should be a directory"
		);
		Preconditions.checkNotNull(filter, "filter should not be null");
		Files.walkFileTree(directory, new DeleteFileVisitor(filter));
	}

	public static boolean deleteDirectoryIfExists(Path directory) throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");

		if (!Files.exists(directory)) {
			return false;
		}

		deleteDirectory(directory);
		return true;
	}

	public static List<Path> matchGlob(Path directory, String glob) throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(directory), "directory should be a directory"
		);
		Preconditions.checkNotNull(glob, "glob should not be null");

		glob = PathUtils.ensureUnixPathSeparators(glob);

		final String[] elements = glob.split(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX));
		final List<Path> parentDirectories;

		if (elements.length > 1) {
			parentDirectories = getParentDirectoriesFromGlobElements(directory, elements);
		} else {
			parentDirectories = Collections.singletonList(directory);
		}

		final List<Path> paths = new ArrayList<>();
		final String childGlob = elements[elements.length - 1];

		for (Path parentDirectory : parentDirectories) {
			try (
					final DirectoryStream<Path> stream =
							Files.newDirectoryStream(parentDirectory, childGlob)
			) {
				stream.forEach(path -> paths.add(path.toAbsolutePath().normalize()));
			}
		}

		return paths;
	}

	private static List<Path> getParentDirectoriesFromGlobElements(
			Path directory, String[] elements
	) throws IOException {
		final List<Path> parentDirectories = Lists.newArrayList(directory);

		for (int i = 0; i < elements.length - 1; i++) {
			final String element = elements[i];
			final List<Path> childDirectories = new ArrayList<>();

			for (Path parentDirectory : parentDirectories) {
				try (
						final DirectoryStream<Path> stream =
								Files.newDirectoryStream(parentDirectory, element)
				) {
					Iterables.filter(stream, Files::isDirectory).forEach(childDirectories::add);
				}
			}

			if (childDirectories.isEmpty()) {
				return Collections.emptyList();
			}

			parentDirectories.clear();
			parentDirectories.addAll(childDirectories);
		}

		return parentDirectories;
	}
}
