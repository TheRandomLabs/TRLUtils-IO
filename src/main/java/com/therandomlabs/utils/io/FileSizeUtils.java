package com.therandomlabs.utils.io;

public final class FileSizeUtils {
	private FileSizeUtils() {}

	public static String getHumanReadableSISize(long bytes) {
		return getHumanReadableSize(bytes, true);
	}

	public static String getHumanReadableBinarySize(long bytes) {
		return getHumanReadableSize(bytes, false);
	}

	//Taken from https://stackoverflow.com/a/3758880
	private static String getHumanReadableSize(long bytes, boolean si) {
		final String sign = bytes < 0 ? "-" : "";
		final int unit = si ? 1000 : 1024;
		bytes = Math.abs(bytes);

		if (bytes < unit) {
			return sign + bytes + " B";
		}

		final int exponent = (int) (Math.log(bytes) / Math.log(unit));
		final String symbol = (si ? "kMGTPE" : "KMGTPE").charAt(exponent - 1) + (si ? "" : "i");
		return String.format("%s%.1f %sB", sign, bytes / Math.pow(unit, exponent), symbol);
	}
}
