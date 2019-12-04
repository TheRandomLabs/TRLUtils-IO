package com.therandomlabs.utils.io;

/**
 * Contains utility methods for converting sizes in bytes to human-readable formats.
 * The code in this class has been taken and adapted from
 * <a href="https://programming.guide/java/formatting-byte-size-to-human-readable-format.html">
 * here
 * </a>.
 */
public final class HumanReadableSize {
	private static final String FORMAT_STRING = "%s%.1f %s";
	private static final String[] DECIMAL_SYMBOLS = {
			"B", "kB", "MB", "GB", "TB", "PB", "EB"
	};
	private static final String[] BINARY_SYMBOLS = {
			"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"
	};

	private HumanReadableSize() {}

	/**
	 * Returns the specified size in a human-readable format in decimal (SI) units.
	 *
	 * @param bytes a size in bytes.
	 * @return the specified size in a human-readable format in decimal units.
	 * For example, for a size of {@code 1000000}, the string {@code "1.0 MB"} is returned,
	 * or {@code "1,0 MB"} depending on the locale.
	 */
	public static String decimal(long bytes) {
		final String sign = bytes < 0 ? "-" : "";

		//Math.abs(Long.MIN_VALUE) is Long.MIN_VALUE.
		long abs = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);

		if (abs < 1000L) {
			return bytes + " " + DECIMAL_SYMBOLS[0];
		}

		for (int i = 1; i < DECIMAL_SYMBOLS.length - 1; i++) {
			if (abs < 999_950L) {
				return String.format(FORMAT_STRING, sign, abs / 1000.0, DECIMAL_SYMBOLS[i]);
			}

			abs /= 1000;
		}

		return String.format(
				FORMAT_STRING, sign, abs / 1000.0, DECIMAL_SYMBOLS[DECIMAL_SYMBOLS.length - 1]
		);
	}

	/**
	 * Returns the specified size in a human-readable format in binary units.
	 *
	 * @param bytes a size in bytes.
	 * @return the specified size in a human-readable format in binary units.
	 * For example, for a size of {@code 1048576} ({@code 1024^2}),
	 * the string {@code "1.0 MiB"} is returned, or {@code "1,0 MiB"} depending on the locale.
	 */
	public static String binary(long bytes) {
		final String sign = bytes < 0 ? "-" : "";

		//Math.abs(Long.MIN_VALUE) is Long.MIN_VALUE.
		long abs = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);

		if (abs < 1024L) {
			return bytes + " " + BINARY_SYMBOLS[0];
		}

		for (int i = 1; i < BINARY_SYMBOLS.length - 1; i++) {
			final double size = Math.pow(1024.0, i);

			if (abs < size * 1024 - 52) {
				return String.format(FORMAT_STRING, sign, abs / size, BINARY_SYMBOLS[i]);
			}
		}

		return String.format(
				FORMAT_STRING, sign, abs / Math.pow(1024.0, BINARY_SYMBOLS.length - 1.0),
				BINARY_SYMBOLS[BINARY_SYMBOLS.length - 1]
		);
	}
}
