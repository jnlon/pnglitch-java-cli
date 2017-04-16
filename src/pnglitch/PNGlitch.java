package pnglitch;

import java.io.File;
import java.io.IOException;

/*
 * See README.txt in project directory
 */

public class PNGlitch {
	
	
	public static final String OUTPUT_DIRECTORY = "pnglitch_output";

	public static void main(String[] args) throws IOException {

		//Get options, show ASCII art
		UI ui = new UI();

		File outDirRoot = new File(OUTPUT_DIRECTORY);
		outDirRoot.mkdir();

		for (File pngfile : ui.fileList) {

			String basename = "???";
			try {
				basename = pngfile.getName().toLowerCase().split("\\.")[0];
			}
			catch (IndexOutOfBoundsException e) {
				basename = pngfile.getName();
			}

			String outdirname = OUTPUT_DIRECTORY + File.separator + basename + "_glitch";
			File outdir = new File(outdirname);

			outdir.mkdir();
			outdirname += File.separator + basename;
			
			PNGData pngdata;
			try {
				pngdata = new PNGData(pngfile);
			}
			catch (IOException e) {
				System.err.println("Cannot glitch " + pngfile.getName());
				System.err.println("");
				outdir.delete();
				continue;
			}
			
			System.out.format("# Glitching %s (size %.2fM)%n", pngfile.getName(), ((double)pngfile.length())/1024.0/1024.0);
			System.out.println("");
			//pngdata.HEADER.printInfos();
			
			Thread[] threads = new Thread[6];
			
			threads[0] = new Thread((new Glitcher(pngdata, outdirname, 0, -1))); //None
			threads[1] = new Thread((new Glitcher(pngdata, outdirname, 1, -1))); //Sub
			threads[2] = new Thread((new Glitcher(pngdata, outdirname, 2, -1))); //Up
			threads[3] = new Thread((new Glitcher(pngdata, outdirname, 3, -1))); //Average
			threads[4] = new Thread((new Glitcher(pngdata, outdirname, 4, -1))); //Paeth
			threads[5] = new Thread((new Glitcher(pngdata, outdirname, -1, ui.frequency))); //Random!
			
			//Start every thread
			for (Thread t : threads)
				t.start();
			
			//Wait for all threads to finish
			for (Thread t : threads) {
				try {
					//System.out.println("TID: " + t.getId() + " Alive: " + t.isAlive());
					t.join(1000000);
				} catch (InterruptedException e) {
					System.err.println("Joining thread interrupted, moving to next file");
					break;
				}
			}

			System.out.println("");

		}
		
		System.out.println("\n#");
		System.out.println("# Done!");
		System.out.println("# Glitched images can be found at " + outDirRoot.getAbsolutePath());
		System.out.println("#\n");
		
		System.out.println("# Thanks for pnglitching!");
	}
}
