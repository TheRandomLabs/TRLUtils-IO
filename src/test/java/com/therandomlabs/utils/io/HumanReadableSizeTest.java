package com.therandomlabs.utils.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class HumanReadableSizeTest {
	@Test
	public void correctDecimalSizesShouldBeReturned() {
		assertThat(HumanReadableSize.decimal(27)).isEqualTo("27 B");
		assertThat(HumanReadableSize.decimal(999)).isEqualTo("999 B");
		assertThat(HumanReadableSize.decimal(1000)).matches("1[.,]0 kB");
		assertThat(HumanReadableSize.decimal(1023)).matches("1[.,]0 kB");
		assertThat(HumanReadableSize.decimal(1024)).matches("1[.,]0 kB");
		assertThat(HumanReadableSize.decimal(1500)).matches("1[.,]5 kB");
		assertThat(HumanReadableSize.decimal(1000000)).matches("1[.,]0 MB");
		assertThat(HumanReadableSize.decimal(1500000)).matches("1[.,]5 MB");
		assertThat(HumanReadableSize.decimal(1000000000)).matches("1[.,]0 GB");
		assertThat(HumanReadableSize.decimal(1550000000)).matches("1[.,]6 GB");
		assertThat(HumanReadableSize.decimal(Long.MAX_VALUE)).matches("9[.,]2 EB");
	}

	@Test
	public void correctBinarySizesShouldBeReturned() {
		assertThat(HumanReadableSize.binary(27)).isEqualTo("27 B");
		assertThat(HumanReadableSize.binary(999)).isEqualTo("999 B");
		assertThat(HumanReadableSize.binary(1000)).matches("1000 B");
		assertThat(HumanReadableSize.binary(1023)).matches("1023 B");
		assertThat(HumanReadableSize.binary(1024)).matches("1[.,]0 KiB");
		assertThat(HumanReadableSize.binary(1500)).matches("1[.,]5 KiB");
		assertThat(HumanReadableSize.binary(1048576)).matches("1[.,]0 MiB");
		assertThat(HumanReadableSize.binary(1500000)).matches("1[.,]4 MiB");
		assertThat(HumanReadableSize.binary(1073741824)).matches("1[.,]0 GiB");
		assertThat(HumanReadableSize.binary(1550000000)).matches("1[.,]4 GiB");
		assertThat(HumanReadableSize.binary(Long.MAX_VALUE)).matches("8[.,]0 EiB");
	}
}
