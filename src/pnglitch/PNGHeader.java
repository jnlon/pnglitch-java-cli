package pnglitch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class PNGHeader {
	
	/* These are found in the IHDR chunk */
	public int WIDTH = 0; // The number of pixels in a scanline
	public int HEIGHT = 0;
	public int BITS_PER_SAMPLE = 0;
	public int COLOUR_TYPE = 0;
	public int COMPRESSION_METHOD = 0; // Number of bytes in a pixel
	public int FILTER_METHOD = 0; // Number of bytes in a pixel
	public int INTERLACE_METHOD = 0; // Number of bytes in a pixel
	public byte[] IHDR_CRC = new byte[4]; //The checksum
	public byte[] B_IHDR; //The data portion

	/*Not found in IHDR chunk, calculated*/
	public int SAMPLES_PER_PIXEL = 0;  // Number of bytes per pixel
	
	
	public void printInfos() {
		
		//Index values beyond these are unspecified by the spec
		String[] compression_descr = { "Inflate/Deflate" };
		String[] filter_descr = { "None" };
		String[] interlace_descr = { "No interlace", "Adam7 interlace" };
		
			System.out.println("## IHDR info ##");
			System.out.println("# Image width: " + WIDTH);
			System.out.println("# Image height: " + HEIGHT);
			System.out.println("# Bit depth: " + BITS_PER_SAMPLE);
			System.out.println("# Colour type: " + COLOUR_TYPE);
			try {
				System.out.format("# Compression method: %s (%d)%n", compression_descr[COMPRESSION_METHOD], COMPRESSION_METHOD);
				System.out.format("# Global filter method: %s (%d)%n", filter_descr[FILTER_METHOD], FILTER_METHOD);
				System.out.format("# Interlace method: %s (%d)%n", interlace_descr[INTERLACE_METHOD], INTERLACE_METHOD);
			} catch(IndexOutOfBoundsException e) {
				System.out.println("# *** Unknown values in IHDR, THIS IS A NON-CONFORMING PNG! ***"); 
			}
			System.out.format("# IHDR checksum: %s%n", BITS.ByteArrayToStringToInt(IHDR_CRC));
			System.out.println("");
	}
	
	public PNGHeader(byte[] ihdrbuff) throws IOException {
		
		B_IHDR = ihdrbuff;

		ByteArrayInputStream tmpr = new ByteArrayInputStream(ihdrbuff);
		tmpr.read(new byte[4], 0, 4); //IHDR Length, should be {0,0,0,13}

		int headercheck[] = { 73, 72, 68, 82 }; //IHDR
		int headername[] = { tmpr.read(), tmpr.read(), tmpr.read(), tmpr.read() }; 

		if (!Arrays.equals(headercheck, headername)) {
			System.err.println("Header chunk is not valid");
			throw new IOException();
		}

		byte widthbytes[] = new byte[4];
		byte heightbytes[] = new byte[4];

		tmpr.read(widthbytes, 0, 4);
		WIDTH = BITS._4bytesToInt(widthbytes);
		
		tmpr.read(heightbytes, 0, 4);
		HEIGHT = BITS._4bytesToInt(heightbytes);
		
		BITS_PER_SAMPLE = tmpr.read();
		COLOUR_TYPE = tmpr.read();
		COMPRESSION_METHOD = tmpr.read();
		FILTER_METHOD = tmpr.read();
		INTERLACE_METHOD = tmpr.read();
		tmpr.read(IHDR_CRC, 0, 4);
		
		if (COLOUR_TYPE == 2) //Each byte is an R, G, or B value
			SAMPLES_PER_PIXEL = 3;
		else if (COLOUR_TYPE == 6) //Each byte is R, G, B, or A value
			SAMPLES_PER_PIXEL = 4;
		}
	}
