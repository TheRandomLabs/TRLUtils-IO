package com.therandomlabs.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConnectionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

	private final Map<String, String> requestProperties = new HashMap<>();
	private Charset encoding = StandardCharsets.UTF_8;
	private int connectTimeout = 5000;
	private int readTimeout = 5000;

	public ConnectionHandler() {
		setUserAgent(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
						"(KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36"
		);
		setAcceptLanguage("en-US,en;q=0.8");
		setReferer("google.com");
		setUpgradeInsecureRequests(true);
	}

	public String getUserAgent() {
		return getRequestProperty("User-Agent");
	}

	public String setUserAgent(String userAgent) {
		return setRequestProperty("User-Agent", userAgent);
	}

	public String getAcceptLanguage() {
		return getRequestProperty("Accept-Language");
	}

	public String setAcceptLanguage(String acceptLanguage) {
		return setRequestProperty("Accept-Language", acceptLanguage);
	}

	public String getReferer() {
		return getRequestProperty("Referer");
	}

	public String setReferer(String referer) {
		return setRequestProperty("Referer", referer);
	}

	public boolean upgradeInsecureRequests() {
		return "1".equals(getRequestProperty("Upgrade-Insecure-Requests"));
	}

	public boolean setUpgradeInsecureRequests(boolean upgradeInsecureRequests) {
		return "1".equals(setRequestProperty(
				"Upgrade-Insecure-Requests", upgradeInsecureRequests ? "1" : "0"
		));
	}

	public String getRequestProperty(String key) {
		Preconditions.checkNotNull(key, "key should not be null");
		return requestProperties.get(key);
	}

	public Map<String, String> getRequestProperties() {
		return requestProperties;
	}

	public String setRequestProperty(String key, String value) {
		Preconditions.checkNotNull(key, "key should not be null");
		return value == null ? requestProperties.remove(key) : requestProperties.put(key, value);
	}

	public Charset getEncoding() {
		return encoding;
	}

	public void setEncoding(Charset encoding) {
		Preconditions.checkNotNull(encoding, "encoding should not be null");
		this.encoding = encoding;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int timeout) {
		Preconditions.checkArgument(timeout >= 0, "timeout should not be negative");
		connectTimeout = timeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int timeout) {
		Preconditions.checkArgument(timeout >= 0, "timeout should not be negative");
		readTimeout = timeout;
	}

	public HttpURLConnection connect(URL url) throws IOException {
		Preconditions.checkNotNull(url, "url should not be null");
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(connectTimeout);
		connection.setReadTimeout(readTimeout);
		requestProperties.forEach(connection::addRequestProperty);
		return connection;
	}

	public InputStream getInputStream(URL url) throws IOException {
		Preconditions.checkNotNull(url, "url should not be null");
		return NetUtils.getInputStream(connect(url), encoding);
	}

	public String read(URL url) throws IOException {
		Preconditions.checkNotNull(url, "url should not be null");

		try (
				InputStreamReader reader = new InputStreamReader(getInputStream(url), encoding)
		) {
			return CharStreams.toString(reader);
		}
	}

	public URL redirectURL(URL url) throws IOException {
		return redirectURL(url, 100);
	}

	public URL redirectURL(URL url, int redirections) throws IOException {
		return redirectURL(url, redirections, false);
	}

	public URL redirectURL(URL url, int redirections, boolean leavePlusesDecoded)
			throws IOException {
		Preconditions.checkNotNull(url, "url should not be null");
		Preconditions.checkArgument(redirections > 0, "redirections should be positive");

		HttpURLConnection connection;
		String previousLocation = null;
		String cookies = null;

		for (int i = 0; i < redirections; i++) {
			connection = connect(url);
			connection.setInstanceFollowRedirects(false);

			//Preserve cookies over redirections
			if (cookies != null) {
				connection.setRequestProperty("Set-Cookie", cookies);
			}

			cookies = connection.getHeaderField("Set-Cookie");
			String location = connection.getHeaderField("Location");

			if (leavePlusesDecoded && location != null) {
				location = location.replace("%2B", "+");
			}

			if (location == null || location.equals(previousLocation)) {
				connection.disconnect();
				break;
			}

			previousLocation = location;
			final URL previousURL = url;
			url = getURLFromLocation(connection, location, leavePlusesDecoded);
			connection.disconnect();

			if (url == null) {
				return previousURL;
			}
		}

		return url;
	}

	public void download(URL url, OutputStream outputStream) throws IOException {
		Preconditions.checkNotNull(url, "url should not be null");
		Preconditions.checkNotNull(outputStream, "outputStream should not be null");
		ByteStreams.copy(getInputStream(url), outputStream);
	}

	public void download(URL url, Path path) throws IOException {
		Preconditions.checkNotNull(url, "url should not be null");
		Preconditions.checkNotNull(path, "path should not be null");
		Files.copy(getInputStream(url), path);
	}

	public DownloadInfo getDownloadInfo(URL url) throws IOException {
		Preconditions.checkNotNull(url, "url should not be null");
		return new DownloadInfo(connect(url));
	}

	private URL getURLFromLocation(
			HttpURLConnection connection, String location, boolean leavePlusesDecoded
	) throws IOException {
		final URL url = connection.getURL();

		try {
			final URL locationURL = new URL(location);
			locationURL.toURI();
			return locationURL;
		} catch (MalformedURLException ex) {
			//If the URL is malformed, it's possible that the location is a relative path.
			try {
				return new URL(url.getProtocol(), url.getHost(), url.getPort(), location);
			} catch (MalformedURLException ex2) {
				logger.warn("Invalid location {} for URL: {}", location, url);
				return null;
			}
		} catch (URISyntaxException ex) {
			//Some websites, such as CurseForge, do not encode their URL paths correctly,
			//so we try encoding it and returning it as the redirected URL if it is valid.
			final URL encodedURL = NetUtils.encodeURLPath(url, encoding, leavePlusesDecoded);
			return URLUtils.isValid(encodedURL) ? encodedURL : null;
		}
	}
}
