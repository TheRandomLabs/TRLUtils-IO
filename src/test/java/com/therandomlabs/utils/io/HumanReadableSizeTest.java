/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2020 TheRandomLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
