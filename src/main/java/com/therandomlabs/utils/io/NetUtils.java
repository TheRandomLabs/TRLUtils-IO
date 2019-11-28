package com.therandomlabs.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetUtils {
	private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

	private NetUtils() {}

	public static URL encodeURLPath(URL url, Charset encoding) throws IOException {
		return encodeURLPath(url, encoding, false);
	}

	public static URL encodeURLPath(URL url, Charset encoding, boolean leavePlusesDecoded)
			throws IOException {
		Preconditions.checkNotNull(url, "url should not be null");
		Preconditions.checkNotNull(encoding, "encoding should not be null");

		final String path = url.getPath();
		final int fileNameIndex = path.lastIndexOf('/') + 1;

		String encodedFileName = URLEncoder.encode(path.substring(fileNameIndex), encoding.name());

		if (leavePlusesDecoded) {
			encodedFileName = encodedFileName.replace("%2B", "+");
		}

		final String encodedPath = path.substring(0, fileNameIndex) + encodedFileName;
		return new URL(url.getProtocol(), url.getHost(), url.getPort(), encodedPath);
	}

	public static String getFileNameQuickly(URL url, Charset encoding) {
		Preconditions.checkNotNull(url, "url should not be null");
		Preconditions.checkNotNull(encoding, "encoding should not be null");

		//Some websites, such as CurseForge, sometimes put tabs in their file names for some reason.
		final String path = url.getPath().replace('\t', ' ');

		if (path.isEmpty()) {
			return null;
		}

		final Path fileName = Paths.get(path).getFileName();

		if (fileName == null) {
			return null;
		}

		try {
			return URLDecoder.decode(fileName.toString(), encoding.name());
		} catch (UnsupportedEncodingException ex) {
			logger.warn("Unsupported encoding: {}", encoding, ex);
		}

		return fileName.toString();
	}

	public static String getFileName(HttpURLConnection connection, Charset encoding) {
		Preconditions.checkNotNull(connection, "connection should not be null");
		Preconditions.checkNotNull(encoding, "encoding should not be null");

		final String disposition = connection.getHeaderField("Content-Disposition");

		if (disposition == null) {
			return getFileNameQuickly(connection.getURL(), encoding);
		}

		//filename* is preferred over filename
		//https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition
		String fileName = getContentDispositionDirective(disposition, "filename*");

		if (fileName == null) {
			fileName = getContentDispositionDirective(disposition, "filename");
		}

		if (fileName != null) {
			try {
				fileName = URLDecoder.decode(fileName, encoding.name());
			} catch (UnsupportedEncodingException ex) {
				logger.warn("Unsupported encoding: {}", encoding, ex);
			}

			return fileName;
		}

		return getFileNameQuickly(connection.getURL(), encoding);
	}

	//Taken from https://stackoverflow.com/a/27226712
	public static String getContentDispositionDirective(String disposition, String directive) {
		Preconditions.checkNotNull(disposition, "disposition should not be null");
		Preconditions.checkNotNull(directive, "directive should not be null");
		Preconditions.checkArgument(!directive.isEmpty(), "directive should not be empty");
		final String regex = "(?i)^.*; " + Pattern.quote(directive) + "=\"?([^\"]+)\"?.*$";
		final Matcher matcher = Pattern.compile(regex).matcher(disposition);
		return matcher.matches() ? matcher.group(1) : null;
	}

	//Document that encoding is only used for the error stream
	public static InputStream getInputStream(HttpURLConnection connection, Charset encoding)
			throws IOException {
		Preconditions.checkNotNull(connection, "connection should not be null");
		Preconditions.checkNotNull(encoding, "encoding should not be null");

		try {
			return connection.getInputStream();
		} catch (IOException ex) {
			//We try to read the error stream and throw its content as a separate IOException.
			try (InputStream errorStream = connection.getErrorStream()){
				if (errorStream == null) {
					throw ex;
				}

				throw new IOException(
						"Failed to read from " + connection.getURL() + ": " +
								CharStreams.toString(new InputStreamReader(errorStream, encoding)),
						ex
				);
			} catch (IOException ex2) {
				//Since the error stream couldn't be read either, we throw the original exception.
				throw ex;
			}
		}
	}

	public static String getMacAddress() throws SocketException, UnknownHostException {
		return getMacAddress(':');
	}

	public static String getMacAddress(char separator)
			throws SocketException, UnknownHostException {
		return getMacAddress(String.valueOf(separator));
	}

	public static String getMacAddress(String separator)
			throws SocketException, UnknownHostException {
		final StringBuilder macAddress = new StringBuilder();

		final InetAddress localHost = InetAddress.getLocalHost();
		final NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);

		if (networkInterface == null) {
			return null;
		}

		for (byte b : networkInterface.getHardwareAddress()) {
			macAddress.append(String.format("%02X", b)).append(separator);
		}

		return macAddress.substring(0, macAddress.length() - separator.length());
	}
}
