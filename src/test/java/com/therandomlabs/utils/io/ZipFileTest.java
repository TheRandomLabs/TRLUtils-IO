package com.therandomlabs.utils.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ZipFileTest {
	@Test
	public void zipFileShouldBeCreated(@TempDir Path tempDirectory) throws IOException {
		final Path path = tempDirectory.resolve("test.zip");

		Files.createFile(path);

		try (ZipFile zipFile = ZipFile.createNew(path)) {
			assertThat(zipFile).isNotNull();
			assertThat(path).isRegularFile();

			//Check for the zip file header.
			final byte[] bytes = Files.readAllBytes(path);
			assertThat(bytes[0]).isEqualTo((byte) 0x50);
			assertThat(bytes[1]).isEqualTo((byte) 0x4b);
			assertThat(bytes[2]).isEqualTo((byte) 0x5);
			assertThat(bytes[3]).isEqualTo((byte) 0x6);
		}
	}

	@Test
	public void validRootShouldBeReturned(@TempDir Path tempDirectory) throws IOException {
		try (ZipFile zipFile = new ZipFile(tempDirectory.resolve("test.zip"))) {
			//The parent of the entry "test" should be the root.
			assertThat(zipFile.getEntry("test").getParent().equals(zipFile.getRoot())).isTrue();
		}
	}
}
