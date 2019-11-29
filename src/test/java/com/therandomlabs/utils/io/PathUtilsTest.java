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
	public void invalidPathsShouldBeInvalid() {
		assertThat(PathUtils.isValid("/test/:")).isFalse();
		assertThat(PathUtils.isValid("?\\test/path")).isFalse();
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
	public void pathIsReturnedWithUnixDirectorySeparators() {
		assertThat(PathUtils.withUnixDirectorySeparators(Paths.get("a\\b"))).isEqualTo("a/b");
		assertThat(PathUtils.withUnixDirectorySeparators("C:\\test")).isEqualTo("C:/test");
	}
}
