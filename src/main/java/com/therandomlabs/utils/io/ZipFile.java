package com.therandomlabs.utils.io;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * A very basic representation of a zip file.
 * <p>
 * Operations can be performed on zip file by acquiring zip entry {@link Path}s using
 * {@link #getEntry(String)}, then operating on those {@link Path}s as normal.
 * <p>
 * For example, to extract a zip file to a directory, the following code can be used:
 * <pre>
 * {@code
 * final ZipFile zipFile = new ZipFile(Paths.get("test.zip"));
 * NIOUtils.copyDirectory(zipFile.getRoot(), Paths.get("extracted"));
 * }
 * </pre>
 * To add a collection of files as entries while preserving directory structure,
 * the following code can be used:
 * <pre>
 * {@code
 * final ZipFile zipFile = new ZipFile(Paths.get("test.zip"));
 * final List<Path> paths = Arrays.asList(Paths.get("a/b.txt"), Paths.get("c/d.txt"));
 * NIOUtils.copyPreservingDirectoryStructure(paths, zipFile.getRoot());
 * }
 * </pre>
 */
public class ZipFile implements AutoCloseable {
	private static final ImmutableMap<String, String> CREATE = ImmutableMap.of("create", "true");
	private static final ImmutableMap<String, String> DO_NOT_CREATE =
			ImmutableMap.of("create", "false");

	private final FileSystem fileSystem;
	private final Path path;

	/**
	 * Creates a new {@link ZipFile} instance that represents a zip file at the specified
	 * {@link Path}.
	 * If a file does not exist at the specified {@link Path}, a zip file is created.
	 * If a file does exist at the specified {@link Path} but is not a valid zip file,
	 * an {@link IOException} is thrown.
	 *
	 * @param path a {@link Path}.
	 * @throws IOException if there is an invalid zip file at the specified {@link Path},
	 * or if an I/O error occurs.
	 */
	public ZipFile(Path path) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");
		this.path = path;
		final URI uri = URI.create("jar:" + path.toUri());
		fileSystem = FileSystems.newFileSystem(uri, Files.exists(path) ? DO_NOT_CREATE : CREATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		fileSystem.close();
	}

	/**
	 * Returns the {@link Path} to this {@link ZipFile}.
	 *
	 * @return the {@link Path} to this {@link ZipFile}.
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * Returns this {@link ZipFile}'s {@link FileSystem}.
	 *
	 * @return this {@link ZipFile}'s {@link FileSystem}.
	 */
	public FileSystem getFileSystem() {
		return fileSystem;
	}

	/**
	 * Returns a {@link Path} that represents an entry within this {@link ZipFile} that matches
	 * the specified entry path.
	 *
	 * @param entryPath an entry path.
	 * @return a {@link Path} that represents an entry within this {@link ZipFile} that matches
	 * the specified entry path.
	 * @throws java.nio.file.InvalidPathException if the entry path is invalid.
	 */
	public Path getEntry(String entryPath) {
		Preconditions.checkNotNull(entryPath, "entryPath should not be null");
		return fileSystem.getPath(entryPath).toAbsolutePath().normalize();
	}

	/**
	 * Returns a {@link Path} that represents this {@link ZipFile}'s root.
	 * This is equivalent to calling {@link #getEntry(String)} with {@code "/"} as the entry path.
	 *
	 * @return a {@link Path} that represents this {@link ZipFile}'s root.
	 */
	public Path getRoot() {
		return getEntry("/");
	}

	/**
	 * Creates a {@link ZipFile} instance that represents a zip file at the specified {@link Path}.
	 * If a file already exists at the specified {@link Path}, it is deleted first.
	 *
	 * @param path a {@link Path}.
	 * @return the created {@link ZipFile}.
	 * @throws IOException if an I/O error occurs.
	 * @see ZipFile#ZipFile(Path)
	 */
	public static ZipFile createNew(Path path) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");
		Files.deleteIfExists(path);
		return new ZipFile(path);
	}
}
