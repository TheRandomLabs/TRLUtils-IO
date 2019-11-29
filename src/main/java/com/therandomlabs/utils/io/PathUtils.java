package com.therandomlabs.utils.io;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Contains utility methods for manipulating paths. No methods in this class access the filesystem.
 */
public final class PathUtils {
	private PathUtils() {}

	/**
	 * Returns whether the specified string is a valid path.
	 *
	 * @param path a string.
	 * @return {@code true} if the specified string is a valid path, or otherwise {@code false}.
	 */
	public static boolean isValid(String path) {
		if (path == null) {
			return false;
		}

		try {
			Paths.get(path);
			return true;
		} catch (InvalidPathException ignored) {}

		return false;
	}

	/**
	 * Returns the file name of the specified {@link Path} as a string.
	 * If the {@link Path} has zero elements, an empty string is returned.
	 *
	 * @param path a path.
	 * @return the file name of the specified {@link Path} as a string.
	 */
	public static String getFileName(Path path) {
		Preconditions.checkNotNull(path, "path should not be null");
		final Path name = path.getFileName();
		return name == null ? "" : name.toString();
	}

	/**
	 * Returns whether a {@link Path} is the ancestor of another {@link Path}.
	 *
	 * @param ancestor a potential ancestor {@link Path}.
	 * @param child a potential child {@link Path}.
	 * @return {@code true} if the specified potential ancestor {@link Path} is an ancestor of the
	 * specified potential child {@link Path}, or otherwise {@code false}.
	 */
	public static boolean isAncestor(Path ancestor, Path child) {
		Preconditions.checkNotNull(ancestor, "ancestor should not be null");
		Preconditions.checkNotNull(child, "child should not be null");

		while ((child = child.getParent()) != null) {
			if (child.equals(ancestor)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the closest common ancestor {@link Path} of two {@link Path}s.
	 *
	 * @param path1 a {@link Path}.
	 * @param path2 another {@link Path}.
	 * @return the closest common ancestor {@link Path} of two {@link Path}s.
	 */
	public static Path getCommonAncestor(Path path1, Path path2) {
		Preconditions.checkNotNull(path1, "path1 should not be null");
		Preconditions.checkNotNull(path2, "path2 should not be null");

		//Taken from https://stackoverflow.com/a/54596369

		if (path1.equals(path2)) {
			return path1;
		}

		path1 = path1.normalize();
		path2 = path2.normalize();

		final int smallestNameCount = Math.min(path1.getNameCount(), path2.getNameCount());

		for (int i = smallestNameCount; i > 0; i--) {
			final Path subpath1 = path1.subpath(0, i);

			if (subpath1.equals(path2.subpath(0, i))) {
				//Since Path#subpath strips away the initial "/", we prepend Path#getRoot.
				return Paths.get(Objects.toString(path1.getRoot(), "") + subpath1.toString());
			}
		}

		return path1.getRoot();
	}

	/**
	 * Returns the closest common ancestor {@link Path} of a collection of {@link Path}s.
	 *
	 * @param paths a collection of {@link Path}s.
	 * @return the closest common ancestor {@link Path} of the specified {@link Path}s.
	 */
	public static Path getCommonAncestor(Collection<Path> paths) {
		Preconditions.checkNotNull(paths, "paths should not be null");
		Preconditions.checkArgument(paths.size() > 1, "paths should contain at least two elements");

		Path commonAncestor = null;

		//We use ImmutableSet#copyOf to remove duplicates.
		for (Path path : ImmutableSet.copyOf(paths)) {
			if (commonAncestor == null) {
				commonAncestor = path;
			} else {
				commonAncestor = getCommonAncestor(commonAncestor, path);
			}
		}

		return commonAncestor;
	}

	/**
	 * Returns the string representation of the specified {@link Path} with Unix directory
	 * separators.
	 *
	 * @param path a {@link Path}.
	 * @return the string representation of the specified {@link Path} with Unix directory
	 * separators.
	 * @see IOConstants#UNIX_DIRECTORY_SEPARATOR
	 */
	public static String withUnixDirectorySeparators(Path path) {
		Preconditions.checkNotNull(path, "path should not be null");
		return withUnixDirectorySeparators(path.toString());
	}

	/**
	 * Returns the specified path with Unix directory separators.
	 *
	 * @param path a path.
	 * @return the specified path with Unix directory separators.
	 * @see IOConstants#UNIX_DIRECTORY_SEPARATOR
	 */
	public static String withUnixDirectorySeparators(String path) {
		Preconditions.checkNotNull(path, "path should not be null");
		return path.replace(
				IOConstants.WINDOWS_DIRECTORY_SEPARATOR, IOConstants.UNIX_DIRECTORY_SEPARATOR
		);
	}
}
