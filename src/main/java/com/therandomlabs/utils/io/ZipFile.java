package com.therandomlabs.utils.io;

import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class ZipFile implements AutoCloseable {
	private static final Map<String, String> CREATE = ImmutableMap.of("create", "true");
	private static final Map<String, String> DO_NOT_CREATE = ImmutableMap.of("create", "false");

	private final FileSystem fileSystem;
	private final Path path;

	public ZipFile(Path path) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");
		this.path = path;
		final URI uri = URI.create("jar:" + path.toUri());
		fileSystem = FileSystems.newFileSystem(uri, Files.exists(path) ? DO_NOT_CREATE : CREATE);
	}

	@Override
	public void close() throws IOException {
		fileSystem.close();
	}

	public Path getPath() {
		return path;
	}

	public FileSystem getFileSystem() {
		return fileSystem;
	}

	public Path getEntryAsPath(String pathInZip) {
		Preconditions.checkNotNull(pathInZip, "pathInZip should not be null");
		return fileSystem.getPath(pathInZip);
	}

	public Path getRootEntryAsPath() {
		return getEntryAsPath("/");
	}

	public String getComment() throws IOException {
		try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(path.toFile())) {
			return zipFile.getComment();
		}
	}

	public void addEntry(Path path) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");

		final Path fileName = path.getFileName();

		if (fileName == null) {
			throw new IllegalArgumentException("Invalid path: " + path);
		}

		addEntry(path, fileName.toString());
	}

	public void addEntry(Path path, String pathInZip) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");
		Preconditions.checkNotNull(pathInZip, "pathInZip should not be null");
		final Path target = getEntryAsPath(pathInZip);
		NIOUtils.ensureParentExists(target);
		Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
	}

	//Paths should be non-null
	public void addEntries(Collection<Path> paths, boolean preserveDirectoryStructure)
			throws IOException {
		Preconditions.checkNotNull(paths, "paths should not be null");

		if (paths.isEmpty()) {
			return;
		}

		if (paths.size() == 1) {
			addEntry(paths.iterator().next());
			return;
		}

		if (!preserveDirectoryStructure) {
			addEntries(paths, file -> file.getFileName().toString());
			return;
		}

		final Path root = PathUtils.getCommonAncestor(
				paths.stream().
						map(path -> path.toAbsolutePath().normalize()).
						collect(Collectors.toList())
		);
		addEntries(paths, file -> root.relativize(file).toString());
	}

	public void addEntries(Collection<Path> paths, Function<Path, String> pathToPathInZip)
			throws IOException {
		Preconditions.checkNotNull(paths, "paths should not be null");
		Preconditions.checkNotNull(pathToPathInZip, "pathToPathInZip should not be null");

		for (Path path : paths) {
			addEntry(path, pathToPathInZip.apply(path));
		}
	}

	public void addAll(Path sourceDirectory) throws IOException {
		Preconditions.checkNotNull(sourceDirectory, "sourceDirectory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(sourceDirectory), "sourceDirectory should be a directory"
		);
		Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
					throws IOException {
				addEntry(file, sourceDirectory.relativize(file).toString());
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public void extract(String pathInZip, Path location, CopyOption... options) throws IOException {
		Preconditions.checkNotNull(pathInZip, "pathInZip should not be null");
		Preconditions.checkNotNull(location, "location should not be null");
		Preconditions.checkNotNull(options, "options should not be null");

		final Path path = getEntryAsPath(pathInZip);

		if (Files.isDirectory(path)) {
			NIOUtils.copyDirectory(path, location, options);
		} else {
			Files.copy(path, location, options);
		}
	}

	public void extractAll(Path location) throws IOException {
		Preconditions.checkNotNull(location, "location should not be null");
		extract("/", location);
	}

	public void walk(String pathInZip, FileVisitor<? super Path> visitor) throws IOException {
		Preconditions.checkNotNull(pathInZip, "pathInZip should not be null");
		Preconditions.checkNotNull(visitor, "visitor should not be null");
		Files.walkFileTree(getEntryAsPath(pathInZip), visitor);
	}

	public static ZipFile createNew(Path path) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");
		Files.deleteIfExists(path);
		return new ZipFile(path);
	}
}
