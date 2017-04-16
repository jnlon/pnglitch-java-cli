package pnglitch;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Scanner;

public class UI {

    public LinkedList<File> fileList;
    public double frequency;

    final private Scanner input;

    private LinkedList<File> walkPngDir(File f, LinkedList<File> lst) {

        for (File child : f.listFiles()) {
            String childname = child.getName().toLowerCase();
            if (child.isFile() && child.canRead() && childname.endsWith(".png") && !lst.contains(child)) {
                lst.add(child);
            } else if (child.isDirectory() && child.canRead() && !Files.isSymbolicLink(child.toPath())) {
                walkPngDir(child, lst);
            }
        }
        
        return lst;
    }

    private LinkedList<File> getPngPaths(File f) {
        LinkedList<File> lst = new LinkedList<File>();

        if (f.isDirectory()) {
            System.out.println("\n# Searching " + f.getAbsolutePath() + " for PNGs...");
            lst = walkPngDir(f, lst);
            if (lst.size() == 0) {
                System.out.println("\n# No files found, try a different path!");
                f = getInputFile();
                return getPngPaths(f);
            }
            return walkPngDir(f, lst);
        } else if (f.isFile()) {
            //System.out.println("\n# "+ f.getAbsolutePath() + " is a file");
            lst.add(f);
        }

        return lst;
    }

    private File getInputFile() {

        String cwd = "'" + System.getProperty("user.dir") + "'";
        System.out.println("# Tips: ");
        System.out.println("#   -Relative and full paths are supported");
        System.out.println("#   -On that note, the current working directory is " + cwd);
        System.out.println("#   -Directory paths will be searched recursively for PNG files");
        System.out.println("#   -Images without a transparency channel produce the best results");
        System.out.println("#   -If you don't have any PNGs lying around, type 'samples' to try some examples!");
        System.out.println("");
        while (true) {
            System.out.print("Enter a file or directory path: ");
            String inputPath = input.nextLine();
            File f = new File(inputPath);
            if (f.exists() && (f.isFile() || f.isDirectory()))
                return f;
            
            System.out.println("\n# File/Directory '" + inputPath + "' does not exist!\n");
        }
    }

    private double getInputFrequency() {

        System.out.println("# Tips: ");
        System.out.println("#   -Valid range is between 0 and 1 (inclusive)");
        System.out.println("#   -The default frequency is .0005");
        System.out.println("#   -If frequency is 0, the image will not be glitched");
        System.out.println("#   -If frequency is 1, every pixel will be overwritten by a random value!");
        System.out.println("#   -This value only affects images glitched with 'random insertion'");
        System.out.println("");

        while (true) {

            System.out.print("Enter a frequency value (leave blank for default): ");
            double fl = .0005;

            try {
                String line = input.nextLine();
                if (line.isEmpty())
                    return fl;

                fl = Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("\n# Number format error, try again!\n");
                continue;
            }

            if (fl < 0 || fl > 1) {
                System.out.println("\n# Frequency should be between 0 and 1!\n");
                continue;
            }

            return fl;
        }
    }

    UI() {
        input = new Scanner(System.in);
        System.out.println("###########################");
        System.out.println(""
                + "                      .__  .__  __         .__     \n"
                + "______   ____    ____ |  | |__|/  |_  ____ |  |__  \n"
                + "\\____ \\ /    \\  / ___\\|  | |  \\   __\\/ ___\\|  |  \\ \n"
                + "|  |_> >   |  \\/ /_/  >  |_|  ||  | \\  \\___|   Y  \\\n"
                + "|   __/|___|  /\\___  /|____/__||__|  \\___  >___|  /\n"
                + "|__|        \\//_____/                    \\/     \\/ \n");

        System.out.println("###########################\n");
        System.out.println("# Welcome to PNGlitch");
        System.out.println("#");
        System.out.println("# Step 1: Choose what files to glitch");
        System.out.println("# ");
        File inputFile = getInputFile();
        fileList = getPngPaths(inputFile);
        System.out.println("# Found " + fileList.size() + " PNG files");
        System.out.println("\n#");
        System.out.println("# Step 2 [optional]: Specify frequency (proportion) of random pixel insertion ");
        System.out.println("#");
        frequency = getInputFrequency();
        System.out.println("");
    }
}
