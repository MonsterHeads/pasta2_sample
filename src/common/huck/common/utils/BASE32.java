package huck.common.utils;

public final class BASE32 {
	private static final String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
	private static final byte[] DECODE_TABLE = {
		26, 27, 28, 29, 30, 31, // 234567
		-1, -1, -1, -1, -1, -1, -1, -1, -1, // 89:;<=>?@
		 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, // ABCDEFGHIJKLM
		13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // NOPQRSTUVWXYZ
		-1, -1, -1, -1, -1, -1, // [\]^_`
		 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, // abcdefghijklm
		13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 // nopqrstuvwxyz
	};

	private static int getDigit(char ch) {
		int lookup = ch - '2';
		if (lookup < 0 || lookup >= DECODE_TABLE.length) {
			throw new IllegalArgumentException("invalid base32 string");
		}
		int digit = DECODE_TABLE[lookup];
		if (digit == -1) {
			throw new IllegalArgumentException("invalid base32 string");
		}
		return digit;
	}
	
	// 5   5   5   5   5   5   5   5
	// 5  3|2  5  1|4 4|1  5  2|3  5 
	public static byte[] decode(String s) throws IllegalArgumentException {
		int sLen = s.length();
		switch (sLen % 8) {
			case 1: // 5 bits in subblock: 0 useful bits but 5 discarded
			case 3: // 15 bits in subblock: 8 useful bits but 7 discarded
			case 6: // 30 bits in subblock: 24 useful bits but 6 discarded
				throw new IllegalArgumentException("invalid base32 string");
		}
		
		byte[] result = new byte[sLen*5/8];
		int offset = 0;
		
		int i = 0;
		int remain;
		while (i < sLen) {
			int d0 = getDigit(s.charAt(i++));
			int d1 = getDigit(s.charAt(i++));
			result[offset++] = (byte)((d0<<3) | (d1>>2));
			remain = d1 & 3;
			if (i >= sLen) {
				if ( 0 != remain) {
					throw new IllegalArgumentException("invalid base32 string");
				}
				break;
			}

			int d2 = getDigit(s.charAt(i++));
			int d3 = getDigit(s.charAt(i++));
			result[offset++] = (byte)((remain<<6) | (d2<<1) | (d3>>4));
			remain = d3 & 15;
			if (i >= sLen) {
				if (0 != remain) {
					throw new IllegalArgumentException("invalid base32 string");
				}
				break; // discard the remaining 4 bits
			}
			
			int d4 = getDigit(s.charAt(i++));
			result[offset++] = (byte)((remain<<4) | (d4>>1));
			remain = d4 & 1;
			if (i >= sLen) {
				if (0 != remain) {
					throw new IllegalArgumentException("invalid base32 string");
				}
				break; // discard the remaining 4 bits
			}
			
			int d5 = getDigit(s.charAt(i++));
			int d6 = getDigit(s.charAt(i++));
			result[offset++] = (byte)((remain<<7) | (d5<<2) | (d6>>3));
			remain = d6 & 7;
			if (i >= sLen) {
				if (0 != remain) {
					throw new IllegalArgumentException("invalid base32 string");
				}
				break; // discard the remaining 4 bits
			}
			
			int d7 = getDigit(s.charAt(i++));
			result[offset++] = (byte)((remain<<5) | d7);
		}
		return result;
	}

	public static String encode(final byte[] bytes) {
		StringBuffer base32 = new StringBuffer((bytes.length * 8 + 4) / 5);
		int currByte, digit, i = 0;
		while (i < bytes.length) {
			currByte = bytes[i++] & 255;
			base32.append(base32Chars.charAt(currByte >> 3));
			digit = (currByte & 7) << 2;
			if (i >= bytes.length) { // put the last 3 bits
				base32.append(base32Chars.charAt(digit));
				break;
			}
			currByte = bytes[i++] & 255;
			base32.append(base32Chars.charAt(digit | (currByte >> 6)));
			base32.append(base32Chars.charAt((currByte >> 1) & 31));
			digit = (currByte & 1) << 4;
			if (i >= bytes.length) { // put the last 1 bit
				base32.append(base32Chars.charAt(digit));
				break;
			}
			currByte = bytes[i++] & 255;
			base32.append(base32Chars.charAt(digit | (currByte >> 4)));
			digit = (currByte & 15) << 1;
			if (i >= bytes.length) { // put the last 4 bits
				base32.append(base32Chars.charAt(digit));
				break;
			}
			currByte = bytes[i++] & 255;
			base32.append(base32Chars.charAt(digit | (currByte >> 7)));
			base32.append(base32Chars.charAt((currByte >> 2) & 31));
			digit = (currByte & 3) << 3;
			if (i >= bytes.length) { // put the last 2 bits
				base32.append(base32Chars.charAt(digit));
				break;
			}
			currByte = bytes[i++] & 255;
			base32.append(base32Chars.charAt(digit | (currByte >> 5)));
			base32.append(base32Chars.charAt(currByte & 31));
			// // This point is reached for bytes.length multiple of 5
		}
		return base32.toString();
	}
	
	public static void main(String... args) throws Exception {
		String in = "1234567890";
		String enc = encode(in.getBytes());
		String dec = new String(decode(enc));
		
		System.out.println(in);
		System.out.println(enc);
		System.out.println(dec);
	}
}