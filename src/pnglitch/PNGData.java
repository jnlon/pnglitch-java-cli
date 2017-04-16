package pnglitch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

public class PNGData {
	
	//Some constants
	public final byte B_CONST_IEND[] = BITS.intsToBytes(new int[] {0, 0, 0, 0, 73, 69, 78, 68, 174, 66, 96, 130});
	public final byte B_CONST_PNGSIG[] = BITS.intsToBytes(new int[] { 137, 80, 78, 71, 13, 10, 26, 10 });
	public final byte B_CONST_IDAT_HDR[] = BITS.intsToBytes(new int[] {73, 68, 65, 84});
	
	//Contains everything important from the IHDR
	//chunk of an image. See PNGHeader class
	public PNGHeader HEADER;
	
	// A byte array where each byte describes either a channel, or, if it is the
	// first in a scanline, a filter method.  It is the purest representation of
	// the image, perfect to glitch!
	public byte[] B_PURE_IDAT;
	
	PNGData(File infile) throws IOException {

		DataInputStream pngr = new DataInputStream(new FileInputStream(infile));

		byte[] filesig = new byte[8];
		pngr.read(filesig, 0, 8);

		if (!Arrays.equals(filesig, B_CONST_PNGSIG)) {
			System.err.println(infile.getName() + " is not a valid PNG (signature incorrect)");
			pngr.close();
			throw new IOException();
		}

		// Length (4) + Header (4) + Data (13) + CRC (4)
		byte ihdrbuff[] = new byte[25];
		pngr.read(ihdrbuff, 0, 25);

		PNGHeader PNG_HEADER = new PNGHeader(ihdrbuff);

		if (!(PNG_HEADER.COLOUR_TYPE == 2 || PNG_HEADER.COLOUR_TYPE == 6)) {
			System.err.println("PNG '" + infile.getName() + "' is not Truecolour or Truecolour w/ alpha");
			pngr.close();
			throw new IOException();
		}

		ByteArrayOutputStream concatIdatStream = new ByteArrayOutputStream();

		// Read through every IDAT chunk, stuff all bytes into concatIdatStream
		while (pngr.available() > 0) {

			byte chunklenbuff[] = new byte[4];
			pngr.read(chunklenbuff, 0, 4);
			int chunklen = BITS._4bytesToInt(chunklenbuff);

			byte headerbuff[] = new byte[4];
			pngr.read(headerbuff, 0, 4);
			
			if (!Arrays.equals(B_CONST_IDAT_HDR, headerbuff)) {
				int toskip = chunklen + 4;
				while (toskip != 0)
					toskip -= pngr.skip(toskip);  //skip data and CRC
				continue;
			}
			
			byte rawIdat[] = new byte[chunklen];
			pngr.read(rawIdat, 0, chunklen);
			concatIdatStream.write(rawIdat);
			
			pngr.skip(4); // skip checksum
		}
		
		InflaterInputStream infIdatStream = new InflaterInputStream( 
				new ByteArrayInputStream(concatIdatStream.toByteArray()));
		
		ByteArrayOutputStream tmpDecompressing = new ByteArrayOutputStream();

		// Decompress everything from concatIdatStream
		while (infIdatStream.available() != 0) {
			byte b[] = new byte[4096];
			int read = 0;
			try {
				read = infIdatStream.read(b);
			} catch (EOFException e) {
				break;
			}
			if (read == -1)
				break;

			tmpDecompressing.write(b, 0, read);
		}
		pngr.close();

		B_PURE_IDAT = tmpDecompressing.toByteArray();
		HEADER = PNG_HEADER;
	}
}
