package pnglitch;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.DeflaterInputStream;

public class Glitcher implements Runnable {
	
	//Passed in via constructor
    private PNGData data;	
    private String basename;
    private int filter;
    private double freq;
    
    //constants	
	private final String FMETHOD_SHORT[] = { 
			"[none]", 
			"[sub]", 
			"[up]", 
			"[average]", 
			"[paeth]" 
		};
	
	
	Glitcher(PNGData _data, String _basename, int _filter, double _freq) {
		this.data = _data;
		this.basename = _basename;
		this.filter = _filter;
		this.freq = _freq;
	}
	
	public static byte[] glitchRandom(PNGHeader ihdr, byte[] data, double frequency) {
		Random rand = new Random();
		int glitches = (int) (((double) data.length) * frequency);

		// The plus one includes the filter byte
		int bytes_per_line = (ihdr.WIDTH * ihdr.SAMPLES_PER_PIXEL) + 1;

		for (int i = 0; i < glitches; i++) {
			int spot = rand.nextInt(data.length);

			// Protects filter byte from being overwritten
			if ((spot % bytes_per_line) == 0)
				continue;

			data[spot] = (byte)rand.nextInt(255);
		}

		return data;
	}

	public static byte[] glitchFilter(PNGHeader ihdr, byte[] data, int fmethod) {

		int bytes_per_scanline = ihdr.WIDTH * ihdr.SAMPLES_PER_PIXEL +1;
		
		int b = 0;
		while (b < data.length) {
			data[b] = (byte) fmethod;
			b += bytes_per_scanline;
		}

		return data;
	}

	
	public void run()  {
		
		if (filter == -1)
			System.out.println("# Glitching with random insertion at frequency " + freq);
		else
			System.out.println("# Glitching with filter method " + FMETHOD_SHORT[filter]);

		switch (filter) {
			case 0: basename += "-filt_none.png"; break;
			case 1: basename += "-filt_sub.png"; break;
			case 2: basename += "-filt_up.png"; break;
			case 3: basename += "-filt_average.png"; break;
			case 4: basename += "-filt_paeth.png"; break;
			default: basename += "-random_insert.png"; break;
		}

		File outfile = new File(basename);

		
		byte[] glitchedIdat = new byte[data.B_PURE_IDAT.length];

		// Glitch it! .clone() ensures we are glitching a copy
		if (filter == -1)
			glitchedIdat = glitchRandom(data.HEADER, data.B_PURE_IDAT.clone(), freq);
		else
			glitchedIdat = glitchFilter(data.HEADER, data.B_PURE_IDAT.clone(), filter);

		// Now recompress it! PNG demands zipstreams inside each IDAT
		ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
		DeflaterInputStream zipStream = new DeflaterInputStream(new ByteArrayInputStream(glitchedIdat));
		
		//Decompress the IDAT we concatenated
		try {
			while (true) {

				byte buff[] = new byte[4096];
				int read = zipStream.read(buff);

				if (read == -1) {
					zipStream.close();
					break;
				}
				tmpStream.write(buff, 0, read);
			}
		}
		catch(IOException e) {
			System.err.println("Error while decompressing");
			e.getMessage();
		}
		
		
		try {
			BufferedOutputStream pngw = new BufferedOutputStream(new FileOutputStream(outfile));
			
			//Writer the PNG signature and header from inputfile
			pngw.write(data.B_CONST_PNGSIG);
			pngw.write(data.HEADER.B_IHDR);
			pngw.flush();

			// Recalculate the CRC of our glitched and compressed IDAT
			CRC32 crc32 = new CRC32();
			byte zippedBuff[] = tmpStream.toByteArray();
			crc32.update(data.B_CONST_IDAT_HDR);
			crc32.update(zippedBuff);
			tmpStream.close();

			// Write the one big IDAT
			pngw.write(BITS.intTo4Bytes(zippedBuff.length));
			pngw.write(data.B_CONST_IDAT_HDR);
			pngw.write(zippedBuff, 0, zippedBuff.length);

			// write the CRC
			pngw.write(BITS.intTo4Bytes((int) (crc32.getValue())));

			// Write hardcoded IEND chunk (should never change)
			pngw.write(data.B_CONST_IEND);

			pngw.flush();
			pngw.close();
		}
		catch(IOException e) {
			System.err.println("Error while writing file");
			e.getMessage();
		}
	}
}
