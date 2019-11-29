package com.therandomlabs.utils.io;

/**
 * Contains constants that are useful when performing I/O operations.
 */
public final class IOConstants {
	/**
	 * The Unix directory separator character.
	 */
	public static final char UNIX_DIRECTORY_SEPARATOR = '/';
	/**
	 * The Windows directory separator character.
	 */
	public static final char WINDOWS_DIRECTORY_SEPARATOR = '\\';

	/**
	 * The Unix line separator string.
	 */
	public static final String UNIX_LINE_SEPARATOR = "\n";
	/**
	 * The Windows line separator string.
	 */
	public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
	/**
	 * The system-dependent line separator string as returned by {@link System#lineSeparator()}.
	 */
	public static final String LINE_SEPARATOR = System.lineSeparator();

	private IOConstants() {}
}
