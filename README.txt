README FILE

== Input ==

The program first prompts for a file or directory path. When given the path of a
directory, it will be searched recursively for files ending in ".png". When
given a file path, pnglitch will perform 5 kinds of glitches on that file alone.

Next it prompts for the "frequency" (proportion) of glitches for the "random insert"
glitch method. Defaults to .0005. See the "glitches" section for more details.

== PNG Overview ==

Specification: http://www.w3.org/TR/PNG/

PNG files are split up into "chunks". Each chunk contains (in the following
order): 

  1) A length value (4 bytes long describing an integer), 
  2) A name (4 bytes, eg. 'IDAT' or 'IHDR' in ASCII), 
  3) Associated data (may be zero length), 
  4) A 4 byte CRC checksum of sections 2 and 3

  In order to glitch PNG's, we are interested in the data section of IDAT
  chunks.  These data sections are commpressed with the zlib DEFLATE algorithm.
  Once this datastream has been decompressed, we are left with the "pure" image
  data.

  If the image is a Truecolour, each byte in this "pure" data will represent an
  R, G, or B value.  If the image is a Trucolour with alpha channel, each byte
  will represent an R, G, B, or A value.

  The only exception is that that every first byte of a PNG "scanline" (a row
  of pixels from left to right) contains a value from 0-4, which describes the
  "filter type" that applies to the whole line.  Filter types are meant to aid
  with compression, typically setting regions with similar colours to the same
  byte values. 

  See http://www.w3.org/TR/PNG/#9Filters for a description of filter types and
  their algorithms.


  == Code Overview ==

  The basic structure is of the program is as follows:

  -> The UI class generates a list of files from user input

  -> From each file a PNGData object is created
    -> This class unzips and concatenates the file's IDAT chunks into B_PURE_IDAT

  -> Each of the 6 glitch methods is performed simultaneously in its own thread (See Glitcher class)
    -> Each thread will glitch its own clone of B_PURE_IDAT from the PNGData object

  -> When every thread running for the current input file has .joined() (and
     every glitched PNG file has been written to disk), a new PNGData object
     is generated with the next input file, and the Glitcher() threads are started again

  == Glitches == 

  Filter types are relied on for 5 kinds of glitches.

  Good PNG encoders will make use of all 5 filter types to optimize the DEFLATE
  compression. 

  So, what happens when we change every scanline to use the same filter method?

  Typically very pretty results!

  Images that are outputed with suffix "-filt_[none|up|sub|average|paeth].png"
  simply have had each scanline's filter method changed to a single value (one
  of 0-4)

  (See the Glitcher.glitchFilter method)

  The last glitch type, suffixed "-random_insert.png" inserts random numbers
  over the channel data (but not the filter method). How many random spots are
  picked depends on the "frequency" variable, prompted for on startup. It
  describes the ratio of randomized to unrandomized spots. 

  A frequency value of 1 means that (almost) every pixel will adopt a new
  random value. A frequency value of 0 means no random numbers will be
  inserted.

  .0005 is the default 

  (See the Glitcher.glitchRandom method)



  == Gotchyas ==

  pnglitch's outputted images are unusual in that all of their data is
  contained in a single IDAT chunk. Most encoders will split up this data
  across many chunks (GIMP seems to use chunk lengths of 8192)

  Only Truecolour and Truecolour with alpha images are supported by pnglitch.
  When offered other kinds of images a warning will print on stderr saying that
  this image will not be glitched.


  == Thanks ==

  -Mr Brunetti, friends, and parents for letting me bug them about PNGs

  -PNG images from the "samples" directory are courtesy of
  http://people.sc.fsu.edu/~jburkardt/data/png/png.html

  -The following website, which inspired the project:
  https://ucnv.github.io/pnglitch/
