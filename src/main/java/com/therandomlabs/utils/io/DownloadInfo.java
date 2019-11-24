package com.therandomlabs.utils.io;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DownloadInfo {
	private final HttpURLConnection connection;

	public DownloadInfo(HttpURLConnection connection) throws IOException {
		this.connection = connection;
	}

	public HttpURLConnection getConnection() {
		return connection;
	}

	public URL getURL() {
		return connection.getURL();
	}

	public String getContentType() {
		return connection.getContentType();
	}

	public String getContentDisposition() {
		return connection.getHeaderField("Content-Disposition");
	}

	public String getContentDispositionDirective(String directive) {
		return NetUtils.getContentDispositionDirective(getContentDisposition(), directive);
	}

	public long getSize() {
		return connection.getContentLengthLong();
	}

	public String getHumanReadableSISize() {
		return FileSizeUtils.getHumanReadableSISize(getSize());
	}

	public String getHumanReadableBinarySize() {
		return FileSizeUtils.getHumanReadableBinarySize(getSize());
	}

	public String getFileName(Charset encoding) {
		return NetUtils.getFileName(connection, encoding);
	}

	//Document that encoding is only used for the error stream
	public void download(Path location, Charset encoding) throws IOException {
		Files.copy(NetUtils.getInputStream(connection, encoding), location);
	}

	public void downloadToDirectory(Path directory, Charset encoding) throws IOException {
		download(directory.resolve(getFileName(encoding)), encoding);
	}
}
