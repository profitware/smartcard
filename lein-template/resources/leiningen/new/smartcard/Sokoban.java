// Authors: pkilla, neko_koneko, val1d

package {{sanitized}};

import javacard.framework.*;

public class Sokoban extends Applet {

	final static byte[] levelOne = new byte[] {
		0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23,
		0x23, 0x23, 0x23, 0x23, 0x23, 0x20, 0x20, 0x20, 0x23, 0x23, 0x23, 0x23,
		0x23, 0x23, 0x23, 0x20, 0x4f, 0x20, 0x23, 0x20, 0x23, 0x23, 0x23, 0x23,
		0x23, 0x23, 0x23, 0x20, 0x23, 0x20, 0x20, 0x2e, 0x20, 0x23, 0x23, 0x23,
		0x23, 0x23, 0x23, 0x20, 0x20, 0x20, 0x48, 0x23, 0x20, 0x23, 0x23, 0x23,
		0x23, 0x23, 0x23, 0x23, 0x4f, 0x23, 0x2e, 0x20, 0x20, 0x23, 0x23, 0x23,
		0x23, 0x23, 0x23, 0x23, 0x20, 0x20, 0x20, 0x23, 0x23, 0x23, 0x23, 0x23,
		0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23,
		0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23
        };
	final static byte[] levelTwo = new byte[] {
		0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23,
		0x23, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x23, 0x20, 0x20, 0x23,
		0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x20, 0x20, 0x20, 0x20, 0x20, 0x23,
		0x23, 0x20, 0x20, 0x20, 0x20, 0x23, 0x20, 0x20, 0x23, 0x20, 0x20, 0x23,
		0x23, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x23, 0x2e, 0x23, 0x23,
		0x23, 0x23, 0x4f, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x2e, 0x23, 0x23,
		0x23, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x4f, 0x20, 0x48, 0x20, 0x23,
		0x23, 0x20, 0x20, 0x20, 0x20, 0x23, 0x23, 0x23, 0x20, 0x20, 0x20, 0x23,
		0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23
        };

	// code of CLA byte in the command APDU header
	final static byte APPLET_CLA =(byte)0x10;

	// codes of INS byte in the command APDU header
	final static byte MOVE = (byte) 0x20;
	final static byte GET_STATE = (byte) 0x30;
	final static byte RESET = (byte) 0x40;

	byte solved = 0;
	final static byte[] flag = new byte[] {
		0x4f, 0x46, 0x46, 0x5a, 0x4f, 0x4e, 0x45, 0x7b, 0x70, 0x75, 0x35, 0x68, 0x5f,
		0x62, 0x30, 0x78, 0x33, 0x35, 0x5f, 0x31, 0x6e, 0x5f, 0x37, 0x68, 0x33, 0x5f,
		0x37, 0x33, 0x6d, 0x70, 0x30, 0x7d
        };

	// signal sent string is wrong
	final static short SW_WRONG_PIN_LEN = 0x6420;
	final static short SW_WRONG_PIN = 0x6421;

	private byte[] first = new byte[108];
	private byte[] second = new byte [108];

	short playerX;
	short playerY;
	boolean first_is_done = false;
	boolean second_is_done = false;

	short XSIZE = 12;
	short YSIZE = 9;

	final static byte[] currentState = new byte[108];

	protected Sokoban(byte[] bArray, short bOffset, byte bLength) {
		if (bLength > 0) {
			byte iLen = bArray[bOffset]; // aid length
			bOffset = (short) (bOffset + iLen + 1);
			byte cLen = bArray[bOffset]; // info length
			bOffset = (short) (bOffset + 3);
			byte aLen = bArray[bOffset]; // applet data length
		}

		init(levelOne);

		register();
	}

	public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
		new Sokoban(bArray, bOffset, bLength);
	}

	public void process(APDU apdu) {
		if (selectingApplet()) {
			return;
		}
		byte[] buffer = apdu.getBuffer();

		switch (buffer[ISO7816.OFFSET_INS]) {

			case MOVE:
				move(apdu);
				return;
			case GET_STATE:
				getGameState(apdu);
				return;
			case RESET:
				reset(apdu);
				return;

			default:
				// We do not support any other INS values
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
		return;
	}

	public void init(byte[] level)
	{
		for (short y = 0; y < YSIZE; y++)
		{
			for (short x = 0; x < XSIZE; x++)
			{
				byte c = level[(short)(y * XSIZE + x)];

				if ( c == 72 || c == 79) { // 'H' or 'O'
					second[(short)(y * XSIZE + x)] = c;
					first[(short)(y * XSIZE + x)] = ' ';
				} else {
					first[(short)(y * XSIZE + x)] = c;
					second[(short)(y * XSIZE + x)] = ' ';
				}
				if (c == 72) {
					playerX = x;
					playerY = y;
				}
			}
		}
		second[(short)(playerY * XSIZE + playerX)] = ' ';
	}

	public void reset(APDU apdu) {
		if (second_is_done == true) {
			init(levelOne);
			first_is_done = false;
			second_is_done = false;
		}
		if (first_is_done) {
			init(levelTwo);
		} else {
			init(levelOne);
		}
	}

	public void move(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		byte numBytes = buffer[ISO7816.OFFSET_LC];
		byte byteRead = (byte)(apdu.setIncomingAndReceive());
		if ((numBytes != 1) || (byteRead > 1)) {
			ISOException.throwIt(ISO7816.SW_WRONG_DATA);
		}
		byte d = buffer[(short)(ISO7816.OFFSET_CDATA)];

		short dx = (short) (d == 0x61 ? -1 : d == 0x64 ? 1 : 0); // L R
		short dy = (short) (d == 0x77 ? -1 : d == 0x73 ? 1 : 0); // U D

		if (second[(short)((playerY + dy) * XSIZE + playerX + dx)] == 79 // 'O'
				&& first[(short)((playerY + 2 * dy) * XSIZE + playerX + 2 * dx)] != 35 // '#'
				&& second[(short)((playerY + 2 * dy) * XSIZE + playerX + 2 * dx)] ==' ') {
			second[(short)((playerY + dy) * XSIZE + playerX + dx)] = ' ';
			second[(short)((playerY + 2 * dy) * XSIZE + playerX + 2 * dx)] = 79; // 'O'
		}
		if (second[(short)((playerY + dy) * XSIZE + playerX + dx)] != 79 // 'O'
				&& first[(short)((playerY + dy) * XSIZE + playerX + dx)] != 35 ) { //'#'
			playerX += dx;
			playerY += dy;
		}
		isCompleted();

	}

	public boolean isCompleted() {
		for (short y = 0; y < YSIZE; y++) {
			for (short x = 0; x < XSIZE; x++) {
				if (second[(short)(y * XSIZE + x)] == 79
						&& first[(short)(y * XSIZE + x)] != 46) { // 'O' and '.'
					return false;
				}
			}
		}
		if (first_is_done) {
			second_is_done = true;
			return true;
		} else {
			init(levelTwo);
			first_is_done = true;
			return false;
		}
	}

	public void getGameState(APDU apdu) {
		short i = 0;
		byte[] currentState = apdu.getBuffer();
		for (short y = 0; y < YSIZE; y++)
		{
			for (short x = 0; x < XSIZE; x++)
			{
				currentState[i] = playerX == x && playerY == y ? 72
					: second[(short)(y * XSIZE + x)] != ' ' ? second[(short)(y * XSIZE + x)]
					: first[(short)(y * XSIZE + x)];
				i++;
			}
		}
		if (second_is_done){
			byte[] buffer = apdu.getBuffer();
			for (short k=0; k<32; k++) {
				buffer[k] = flag[k];
			}
			apdu.setOutgoingAndSend((short) 0, (short) 32);
		}
		else apdu.setOutgoingAndSend((short) 0, (short) 108);
	}

}
