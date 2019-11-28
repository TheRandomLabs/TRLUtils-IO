package com.therandomlabs.utils.io;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.base.Preconditions;

public final class DownloadInfo {
	private final HttpURLConnection connection;

	public DownloadInfo(HttpURLConnection connection) {
		Preconditions.checkNotNull(connection, "connection should not be null");
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
		Preconditions.checkNotNull(directive, "directive should not be null");
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
		Preconditions.checkNotNull(encoding, "encoding should not be null");
		return NetUtils.getFileName(connection, encoding);
	}

	//Encoding is only used for the error stream
	public void download(Path path, Charset encoding) throws IOException {
		Preconditions.checkNotNull(path, "path should not be null");
		Preconditions.checkNotNull(encoding, "encoding should not be null");
		Files.copy(NetUtils.getInputStream(connection, encoding), path);
	}

	public void downloadToDirectory(Path directory, Charset encoding) throws IOException {
		Preconditions.checkNotNull(directory, "directory should not be null");
		Preconditions.checkArgument(
				Files.isDirectory(directory), "directory should be a directory"
		);
		Preconditions.checkNotNull(encoding, "encoding should not be null");
		download(directory.resolve(getFileName(encoding)), encoding);
	}
}
