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

import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class PathUtilsTest {
	@Test
	public void validPathsShouldBeValid() {
		assertThat(PathUtils.isValid("/test/path")).isTrue();
		assertThat(PathUtils.isValid("C:\\test/path")).isTrue();
	}

	@Test
	public void correctFileNamesShouldBeReturned() {
		assertThat(PathUtils.getFileName(Paths.get("/test/file.txt"))).isEqualTo("file.txt");
		assertThat(PathUtils.getFileName(Paths.get(""))).isEmpty();
	}

	@Test
	public void ancestorPathShouldBeAncestor() {
		assertThat(PathUtils.isAncestor(Paths.get("/test"), Paths.get("/test/path/directory"))).
				isTrue();
	}

	@Test
	public void unrelatedPathShouldNotBeAncestor() {
		assertThat(PathUtils.isAncestor(Paths.get("/test"), Paths.get("/path"))).isFalse();
	}

	@Test
	public void closestCommonAncestorShouldBeReturned() {
		assertThat(PathUtils.getCommonAncestor(
				Paths.get("/a/b/c/d/file.txt"),
				Paths.get("/a/b/c/file.txt")
		)).isEqualTo(Paths.get("/a/b/c"));

		assertThat(PathUtils.getCommonAncestor(Arrays.asList(
				Paths.get("/a/b/c/d/e/file.txt"),
				Paths.get("/a/b/c/file.txt"),
				Paths.get("/a/b/file.txt")
		))).isEqualTo(Paths.get("/a/b"));
	}

	@Test
	public void pathShouldBeReturnedWithUnixDirectorySeparators() {
		assertThat(PathUtils.withUnixDirectorySeparators(Paths.get("a\\b"))).isEqualTo("a/b");
		assertThat(PathUtils.withUnixDirectorySeparators("C:\\test")).isEqualTo("C:/test");
	}
}
