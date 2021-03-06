package org.red5.codecs.asao;

/*
 * Copyright (c) 2007 a840bda5870ba11f19698ff6eb9581dfb0f95fa5,
 *                    539459aeb7d425140b62a3ec7dbf6dc8e408a306, and
 *                    520e17cd55896441042b14df2566a6eb610ed444
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

//------------------------------------------------------------------------
//
//
//
//------------------------------------------------------------------------
final class StateTable {

	private final byte[] a;

	private final int b;

	private int c;

	private int d;

	public StateTable(byte[] abyte0, int i) {

		c = 0;
		d = 0;
		a = abyte0;
		b = i;
	}

	public int state(int i) {

		int k = 8 - d;
		int l = (a[b + c] & 0xff) >> d;
		if (i >= k) {
			c++;
			if (i > k) {
				l |= a[b + c] << k;
			}
		}
		d = d + i & 7;
		return l & (1 << i) - 1;
	}

	public void state(int i, int k) {

		if (d == 0) {
			a[b + c] = (byte) i;
		} else {
			a[b + c] |= (byte) (i << d);
		}
		d += k;
		if (d < 8) {
			return;
		}
		c++;
		d -= 8;
		if (d > 0) {
			a[b + c] = (byte) (i >> k - d);
		}
	}

}
