package com.therandomlabs.utils.io;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public final class PathUtils {
	private PathUtils() {}

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

	public static String getName(Path path) {
		Preconditions.checkNotNull(path, "path should not be null");
		final Path name = path.getFileName();
		return name == null ? "" : name.toString();
	}

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

	//Taken from https://stackoverflow.com/a/54596369
	public static Path getCommonAncestor(Path path1, Path path2) {
		Preconditions.checkNotNull(path1, "path1 should not be null");
		Preconditions.checkNotNull(path2, "path2 should not be null");

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
				return Paths.get(Objects.toString(path1.getRoot(), ""), subpath1.toString());
			}
		}

		return path1.getRoot();
	}

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

	public static String toStringWithUnixPathSeparators(Path path) {
		Preconditions.checkNotNull(path, "path should not be null");
		return ensureUnixPathSeparators(path.toString());
	}

	public static String ensureUnixPathSeparators(String path) {
		Preconditions.checkNotNull(path, "path should not be null");
		return path.replace(IOConstants.DIR_SEPARATOR_WINDOWS, IOConstants.DIR_SEPARATOR_UNIX);
	}
}
