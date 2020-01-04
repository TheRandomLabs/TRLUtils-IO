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
