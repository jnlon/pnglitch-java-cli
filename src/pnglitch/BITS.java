package pnglitch;

public class BITS {
	
	//Mostly bithacking helper methods
	
	public static byte[] intsToBytes(int[] ints) {
		byte[] b = new byte[ints.length];
		for (int i = 0; i < ints.length; i++) {
			b[i] = (byte) ints[i];
		}
		return b;
	}
	
	public static byte[] intTo4Bytes(int i) {
		
		int eraser = 0xFF;
		byte[] b = new byte[4];
		b[0] = (byte) ((i >> 24) & eraser);
		b[1] = (byte) ((i >> 16) & eraser);
		b[2] = (byte) ((i >> 8) & eraser);
		b[3] = (byte) (i & eraser);

		return b;
	}

	public static String bytesToString(byte[] nums) {
		String str = "";
		for (int i = 0; i < nums.length; i++)
			str += (char) nums[i];
		return str;
	}
	
	
	// signed bytes are the devil
	public static String ByteArrayToStringToInt(byte b[]) {

		String str = "[";

		for (int i = 0; i < b.length; i++) {
			str += (int) b[i] & 0xFF;
			if (i == b.length - 1)
				break;
			str += ", ";
		}

		str += "]";
		return str;
	}
	
    public static int _4bytesToInt(byte bb[]) {
		int b1 = bb[0] & 0x0000FF;
		int b2 = bb[1] & 0x0000FF;
		int b3 = bb[2] & 0x0000FF;
		int b4 = bb[3] & 0x0000FF;

		return (int)((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);
    }

}
