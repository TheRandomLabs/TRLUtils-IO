package com.therandomlabs.utils.io;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.common.base.Preconditions;

public final class URLUtils {
	private URLUtils() {}

	public static boolean isValid(String url) {
		if (url == null) {
			return false;
		}

		try {
			return isValid(new URL(url));
		} catch (MalformedURLException ex) {
			return false;
		}
	}

	public static boolean isValid(URL url) {
		if (url == null) {
			return false;
		}

		try {
			url.toURI();
		} catch (URISyntaxException ex) {
			return false;
		}

		return true;
	}

	public static URL of(String urlString) {
		Preconditions.checkNotNull(urlString, "url should not be null");

		try {
			final URL url = new URL(urlString);
			url.toURI();
			return url;
		} catch (MalformedURLException | URISyntaxException ignored) {}

		return null;
	}
}
