package cn.join.android.util;

public class Base64Util
{
    private static final char last2byte = (char) Integer.parseInt("00000011", 2);
    private static final char last4byte = (char) Integer.parseInt("00001111", 2);
    private static final char last6byte = (char) Integer.parseInt("00111111", 2);
    private static final char lead6byte = (char) Integer.parseInt("11111100", 2);
    private static final char lead4byte = (char) Integer.parseInt("11110000", 2);
    private static final char lead2byte = (char) Integer.parseInt("11000000", 2);
    private static final char[] encodeTable = new char[]
    { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
    private static byte[] base64DecodeChars = new byte[] { -1, -1, -1, -1, -1,
    	   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    	   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    	   -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59,
    	   60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
    	   10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1,
    	   -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
    	   38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1,
    	   -1, -1 };
    /**
     * Base64 encoding.
     * 
     * @param from
     *            The src data.
     * @return
     */
    public static String encode(byte[] from)
    {
        StringBuffer to = new StringBuffer((int) (from.length * 1.34) + 3);
        int num = 0;
        char currentByte = 0;
        for (int i = 0; i < from.length; i++)
        {
            num = num % 8;
            while (num < 8)
            {
                switch (num)
                {
                case 0:
                    currentByte = (char) (from[i] & lead6byte);
                    currentByte = (char) (currentByte >>> 2);
                    break;
                case 2:
                    currentByte = (char) (from[i] & last6byte);
                    break;
                case 4:
                    currentByte = (char) (from[i] & last4byte);
                    currentByte = (char) (currentByte << 2);
                    if ((i + 1) < from.length)
                    {
                        currentByte |= (from[i + 1] & lead2byte) >>> 6;
                    }
                    break;
                case 6:
                    currentByte = (char) (from[i] & last2byte);
                    currentByte = (char) (currentByte << 4);
                    if ((i + 1) < from.length)
                    {
                        currentByte |= (from[i + 1] & lead4byte) >>> 4;
                    }
                    break;
                }
                to.append(encodeTable[currentByte]);
                num += 6;
            }
        }
        if (to.length() % 4 != 0)
        {
            for (int i = 4 - to.length() % 4; i > 0; i--)
            {
                to.append("=");
            }
        }
        return to.toString();
    }
    
    
}
