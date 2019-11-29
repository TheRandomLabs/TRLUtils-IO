package com.therandomlabs.utils.io;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

/**
 * Contains utility methods for filesystem manipulation with Java NIO.
 */
public final class NIOUtils {
	private NIOUtils() {}

	/**
	 * Returns a list of elements within the specified directory.
	 *
	 * @param directory a {@link Path} to a directory.
	 * @return a mutable {@link List} of {@link Path}s to elements within the specified directory.
	 * @throws IOException if an I/O error occurs when opening the directory.
	 */
	public static List<Path> list(Path directory) throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(directory), "directory should be a directory"
		);

		try (Stream<Path> list = Files.list(directory)) {
			return list.collect(Collectors.toList());
		}
	}

	/**
	 * Returns whether the tree of the specified directory is empty, i.e. does not contain
	 * any files and only contains directories.
	 *
	 * @param directory a {@link Path} to a directory.
	 * @return {@code true} if the tree of the specified directory is empty,
	 * or otherwise {@code false}.
	 * @throws IOException if an I/O error occurs when opening a directory.
	 */
	public static boolean isTreeEmpty(Path directory) throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(directory), "directory should be a directory"
		);

		List<Path> children = list(directory);

		while (!children.isEmpty()) {
			final List<Path> nextChildren = new ArrayList<>();

			for (Path child : children) {
				if (!Files.isDirectory(child)) {
					return false;
				}

				nextChildren.addAll(list(child));
			}

			children = nextChildren;
		}

		return true;
	}

	/**
	 * Creates any nonexistent parent directories of the specified {@link Path} if necessary.
	 *
	 * @param path a {@link Path}.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void ensureParentExists(Path path) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");

		final Path parent = path.getParent();

		if (parent != null) {
			Files.createDirectories(parent);
		}
	}

	/**
	 * Copies the specified files to the specified target directory while preserving directory
	 * structure.
	 * This is done by finding the common ancestor of the {@link Path}s using
	 * {@link PathUtils#getCommonAncestor(Collection)}.
	 *
	 * @param files a collection of {@link Path}s.
	 * @param targetDirectory a {@link Path} to a target directory.
	 * @param options {@link CopyOption}s that specify how the files should be copied.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copyPreservingDirectoryStructure(
			Collection<Path> files, Path targetDirectory, CopyOption... options
	) throws IOException {
		Preconditions.checkNotNull(files, "files should not be null");
		Preconditions.checkNotNull(targetDirectory, "targetDirectory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(targetDirectory), "targetDirectory should be a directory"
		);
		Preconditions.checkNotNull(options, "options should not be null");

		if (files.isEmpty()) {
			return;
		}

		if (files.size() == 1) {
			final Path file = files.iterator().next();
			Files.copy(file, targetDirectory.resolve(file.getFileName()), options);
			return;
		}

		final List<Path> normalized = files.stream().
				map(file -> file.toAbsolutePath().normalize()).
				collect(Collectors.toList());
		final Path commonAncestor = PathUtils.getCommonAncestor(normalized);

		for (Path file : normalized) {
			final Path target = targetDirectory.resolve(commonAncestor.relativize(file));
			ensureParentExists(target);
			Files.copy(file, target, options);
		}
	}

	/**
	 * Recursively copies the specified source directory to the specified target location.
	 *
	 * @param sourceDirectory a {@link Path} to the directory to copy.
	 * @param targetDirectory a {@link Path} to the target location.
	 * @param options {@link CopyOption}s that specify how files should be copied.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copyDirectory(
			Path sourceDirectory, Path targetDirectory, CopyOption... options
	) throws IOException {
		Preconditions.checkNotNull(sourceDirectory, "sourceDirectory should not be null");
		Preconditions.checkNotNull(targetDirectory, "targetDirectory should not be null");
		Files.walkFileTree(sourceDirectory, new CopyFileVisitor(targetDirectory, options));
	}

	/**
	 * Recursively deletes the specified directory.
	 *
	 * @param directory a {@link Path} to a directory.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void deleteDirectory(Path directory) throws IOException {
		if (!list(directory).isEmpty()) {
			deleteInDirectory(directory, path -> true);
		}

		Files.delete(directory);
	}

	/**
	 * Recursively deletes all files and directories in the specified directory that match the
	 * specified filter.
	 *
	 * @param directory a {@link Path} to a directory.
	 * @param filter a {@link Predicate} that determines which files and directories should be
	 * deleted.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void deleteInDirectory(Path directory, Predicate<Path> filter)
			throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(directory), "directory should be a directory"
		);
		Preconditions.checkNotNull(filter, "filter should not be null");
		Files.walkFileTree(directory, new DeleteFileVisitor(directory, filter));
	}

	/**
	 * Recursively deletes the specified directory if it exists.
	 *
	 * @param directory a {@link Path} to a directory.
	 * @return {@code true} if the directory existed, or otherwise {@code false}.
	 * @throws IOException if an I/O error occurs.
	 */
	public static boolean deleteDirectoryIfExists(Path directory) throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");

		if (!Files.exists(directory)) {
			return false;
		}

		deleteDirectory(directory);
		return true;
	}

	/**
	 * Returns a list of {@link Path}s that match the specified glob relative to the
	 * specified directory.
	 *
	 * @param directory a {@link Path} to a directory.
	 * @param glob a glob. The Unix path separator ({@code /}) should be used instead of the
	 * Windows path separator ({@code \}) as the backslash is used as an escape character.
	 * @return a list of {@link Path}s that match the specified glob relative to the
	 * specified directory.
	 * @throws IOException if an I/O error occurs.
	 * @see FileSystem#getPathMatcher(String)
	 */
	@SuppressWarnings("PMD.CloseResource")
	public static List<Path> matchGlob(Path directory, String glob) throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(directory), "directory should be a directory"
		);
		Preconditions.checkNotNull(glob, "glob should not be null");

		directory = directory.toAbsolutePath().normalize();

		final FileSystem fileSystem = directory.getFileSystem();
		final PathMatcher matcher = fileSystem.getPathMatcher(
				"glob:" + PathUtils.withUnixDirectorySeparators(directory) + "/" + glob
		);

		try (Stream<Path> stream = Files.walk(directory)) {
			return stream.filter(matcher::matches).collect(Collectors.toList());
		}
	}
}
