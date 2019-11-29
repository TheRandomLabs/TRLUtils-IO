package com.therandomlabs.utils.io;

/**
 * Contains utility methods for converting sizes in bytes to human-readable formats.
 */
public final class HumanReadableSize {
	private HumanReadableSize() {}

	/**
	 * Returns the specified size in a human-readable format in decimal (SI) units.
	 *
	 * @param bytes a size in bytes.
	 * @return the specified size in a human-readable format in decimal units.
	 * For example, for a size of {@code 1000000}, the string {@code "1 MB"} is returned.
	 */
	public static String decimal(long bytes) {
		return get(bytes, true);
	}

	/**
	 * Returns the specified size in a human-readable format in binary units.
	 *
	 * @param bytes a size in bytes.
	 * @return the specified size in a human-readable format in binary units.
	 * For example, for a size of {@code 1048576} ({@code 1024^2}),
	 * the string {@code "1 MiB"} is returned.
	 */
	public static String binary(long bytes) {
		return get(bytes, false);
	}

	//Taken from https://stackoverflow.com/a/3758880
	private static String get(long bytes, boolean decimal) {
		final String sign = bytes < 0 ? "-" : "";
		final int unit = decimal ? 1000 : 1024;
		bytes = Math.abs(bytes);

		if (bytes < unit) {
			return sign + bytes + " B";
		}

		final int exponent = (int) (Math.log(bytes) / Math.log(unit));
		final String symbol =
				(decimal ? "kMGTPE" : "KMGTPE").charAt(exponent - 1) + (decimal ? "" : "i");
		return String.format("%s%.1f %sB", sign, bytes / Math.pow(unit, exponent), symbol);
	}
}
