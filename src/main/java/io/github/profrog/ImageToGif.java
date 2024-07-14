package io.github.profrog;
import com.sun.source.tree.NewArrayTree;

import javax.imageio.ImageIO;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class ImageToGif {

    /**
     * No copyright asserted on the source code of this class. May be used for any
     * purpose, however, refer to the Unisys LZW patent for restrictions on use of
     * the associated LZWEncoder class. Please forward any corrections to
     * kweiner@fmsware.com.
     *
     * 2nd edit, Mingyu Kim(ache159@naver.com)
     *
     * @author Kevin Weiner, FM Software
     * @version 1.03 November 2003
     *
     */

    /**
     *  output_gif if gif file for distribution
     */
    public static OutputStream output_gif;

    /**
     *  cur_image is current image as frame for gif output
     */
    public static BufferedImage cur_image; // current frame

    /**
     * img_width is current image's width and img_height is currents image height
     */
    public static int img_width; // image's width size
    public static int img_height; //image's height size


    /**
     * input_images is input data for making gif output
     */
    public static List<BufferedImage> input_images;


    /**
     * repeat is parameter of repetitions for images
     * -1 mean that no repeat
     */
    public static int repeat = -1;

    /**
     * delay is parameter delay for images
     * unit is milliseconds
     */
    public static int delay = 0;

    /**
     * setSize is parameter for checking frame size
     * if false, get size from first frame
     */
    public static boolean sizeSet = false;

    /**
     * sample is parameter for quality of sampling
     * default sample interval for quantizer
     */
    public static int sample = 10;

    /**
     * pixels are parameter for BGR data of frame
     * BGR byte array from frame
     */
    public static byte[] pixels;

    /**
     * indexedPixels are parameter for pixel color data for BGR data of frame
     * converted frame indexed to palette
     */
    public static byte[] indexedPixels;

    /**
     * colorTab are parameter for palette data for pixel color data
     * RGB palette
     */
    public static byte[] colorTab;

    /**
     * userEntry are parameter for active color data set from colorTab
     * active palette entries
     */
    public static boolean[] usedEntry = new boolean[256];

    /**
     * firstFrame is parameter for check event about first frame
     */
    public static boolean firstFrame = true;

    /**
     * it is init method for controlling gif data form BufferedImages in gif_chessx2
     * @param output_dir - location for saving gif data added gif's name
     * @param input_dir - buffer image set which make gif file
     * @param delay - millisecond of delay frame by frame
     * example1 : ImageToGif.gifInit("/home/mingyu/Pictures/Wallpapers/test.gif", test_set, 1000);
     */
    public static boolean gifInit(String output_dir, String input_dir, int delay) throws IOException {
        setDelay(delay);
        output_gif = new BufferedOutputStream(new FileOutputStream(output_dir));
        writeString("GIF89a");
        connectLinkToImage(input_dir);

        int idx = 0;
        for(BufferedImage image : input_images)
        {
            addFrame(image);

        }
        finish();
        System.out.println("finished ImageToGif Process");
        return true;
    }

    /**
     * it is method for getting input image about board and piece
     * @param original_dir - directory for original images
     * example1 : connectLinkToImage("/home/mingyu/Pictures/chess/output");
     */
    public static List<BufferedImage> connectLinkToImage(String original_dir)
    {
        File directory = new File(original_dir);
        File[] imageFiles = directory.listFiles((dir, name) -> {
            String nameLower = name.toLowerCase();
            return nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") || nameLower.endsWith(".png") || nameLower.endsWith(".bmp");
        });

        if (imageFiles != null && imageFiles.length > 0) {
            // Sort by file name
            Arrays.sort(imageFiles, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return file1.getName().compareTo(file2.getName());
                }
            });
        }

        input_images = new ArrayList<>();
        for (int idx = 0; idx < imageFiles.length; idx++) {
            try {
                input_images.add(ImageIO.read(imageFiles[idx]));
                System.out.println("connect input image " + idx + " " + imageFiles[idx].getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return input_images;
    }

    /**
     * it is method for writing string to gif file
     * @param str - string content which write to file
     * example1 : writeString("GIF89a");
     */
    public static void writeString(String str) throws IOException {
        for (int i = 0; i < str.length(); i++) {
            output_gif.write((byte) str.charAt(i)); //write data on outputstream for gif file
        }
    }

    /**
     * Adds next GIF frame. The frame is not written immediately, but is actually
     * deferred until the next frame is received so that timing data can be
     * inserted. Invoking <code>finish()</code> flushes all frames. If
     * <code>setSize</code> was not invoked, the size of the first image is used
     * for all subsequent frames.
     *
     * @param im - BufferedImage containing frame to write.
     * example1 : addFrame(image);
     */
    public static boolean addFrame(BufferedImage im) {
        if (im == null) {
            return false;
        }
        boolean ok = true;
        try {
            if (!sizeSet) {
                // use first frame's size
                setSize(im.getWidth(), im.getHeight());
            }
            cur_image = im;
            getImagePixels(); // convert to correct format if necessary
            analyzePixels(); // build color table & map pixels
            if (firstFrame) {
                writeLSD(); // logical screen descriptior
                writePalette(); // global color table
                if (repeat >= 0) {
                    // use NS app extension to indicate reps
                    writeNetscapeExt();
                }
            }
            writeGraphicCtrlExt(); // write graphic control extension
            writeImageDesc(); // image descriptor
            if (!firstFrame) {
                writePalette(); // local color table
            }
            writePixels(); // encode and write pixel data
            firstFrame = false;
        } catch (IOException e) {
            ok = false;
        }

        return ok;
    }

    /**
     * it is method for finishing process for gif file
     */
    public static boolean finish() {

        boolean ok = true;
        try {
            output_gif.write(0x3b); // gif trailer
            output_gif.flush();
            output_gif.close();

        } catch (IOException e) {
            ok = false;
        }

        // reset for subsequent use
        //transIndex = 0;
        output_gif = null;
        cur_image = null;
        pixels = null;
        indexedPixels = null;
        colorTab = null;
        firstFrame = true;
        return ok;
    }

    /**
     * Sets the delay time between each frame, or changes it for subsequent frames
     * (applies to last frame added).
     * @param ms - int delay time in milliseconds
     */
    public static void setDelay(int ms) {
        if(ms >= 0) {
            delay = Math.round(ms / 10.0f);
        }
    }


    /**
     * Sets the number of times the set of GIF frames should be played. Default is
     * 1; 0 means play indefinitely. Must be invoked before the first image is
     * added.
     * @param iter - int number of iterations.
     */
    public static void setRepeat(int iter) {
        if (iter >= 0) {
            repeat = iter;
        }
    }

    /**
     * Sets frame rate in frames per second. Equivalent to
     * <code>setDelay(1000/fps)</code>.
     * @param fps - float frame rate (frames per second)
     */
    public static void setFrameRate(float fps) {
        if (fps != 0f) {
            delay = Math.round(100f / fps);
        }
    }

    /**
     * Sets quality of color quantization (conversion of images to the maximum 256
     * colors allowed by the GIF specification). Lower values (minimum = 1)
     * produce better colors, but slow processing significantly. 10 is the
     * default, and produces good color mapping at reasonable speeds. Values
     * greater than 20 do not yield significant improvements in speed.
     *
     * @param quality -int greater than 0.
     */
    public static void setQuality(int quality) {
        if (quality < 1)
            quality = 1;
        sample = quality;
    }



    /**
     * Sets the GIF frame size. The default size is the size of the first frame
     * added if this method is not invoked.
     * @param w - int frame width.
     * @param h -int frame width.
     */
    public static void setSize(int w, int h) {
        if (!firstFrame)
            return;
        img_width = w;
        img_height = h;
        if (img_width < 1)
            img_width = 320;
        if (img_height < 1)
            img_height = 240;
        sizeSet = true;
    }

    /**
     * Analyzes image colors and creates color map.
     */
    public static void analyzePixels() {
        int len = pixels.length;
        int nPix = len / 3;
        indexedPixels = new byte[nPix];
        NeuQuant nq = new NeuQuant(pixels, len, sample);
        // initialize quantizer
        colorTab = nq.process(); // create reduced palette
        // convert map from BGR to RGB
        for (int i = 0; i < colorTab.length; i += 3) {
            byte temp = colorTab[i];
            colorTab[i] = colorTab[i + 2];
            colorTab[i + 2] = temp;
            usedEntry[i / 3] = false;
        }
        // map image pixels to new palette
        int k = 0;
        for (int i = 0; i < nPix; i++) {
            int index = nq.map(pixels[k++] & 0xff, pixels[k++] & 0xff, pixels[k++] & 0xff);
            usedEntry[index] = true;
            indexedPixels[i] = (byte) index;
        }
        pixels = null;
    }

    /**
     * Extracts image pixels into byte array "pixels"
     */
    public static void getImagePixels() {
        int w = cur_image.getWidth();
        int h = cur_image.getHeight();
        int type = cur_image.getType();
        if ((w != img_width) || (h != img_height) || (type != BufferedImage.TYPE_3BYTE_BGR)) {
            // create new image with right size/format
            BufferedImage temp = new BufferedImage(img_width, img_height, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = temp.createGraphics();
            g.drawImage(cur_image, 0, 0, null);
            cur_image = temp;
        }
        pixels = ((DataBufferByte) cur_image.getRaster().getDataBuffer()).getData();
    }

    /**
     * Writes Graphic Control Extension
     */
    public static void writeGraphicCtrlExt() throws IOException {
        output_gif.write(0x21); // extension introducer
        output_gif.write(0xf9); // GCE label
        output_gif.write(4); // data block size
        int transp, disp;
        if (true) { //transparent == null
            transp = 0;
            disp = 0; // dispose = no action
        } else {
            transp = 1;
            disp = 2; // force clear if using transparent color
        }
        if (true) { //dispose >= 0
            disp = 100 & 7; // user override, dispose & 7
        }
        disp <<= 2;

        // packed fields
        output_gif.write(0 | // 1:3 reserved
                disp | // 4:6 disposal
                0 | // 7 user input - 0 = none
                transp); // 8 transparency flag

        writeShort(delay); // delay x 1/100 sec
        output_gif.write(0); // transparent color index
        output_gif.write(0); // block terminator
    }

    /**
     * Writes Image Descriptor
     */
    public static void writeImageDesc() throws IOException {
        output_gif.write(0x2c); // image separator
        writeShort(0); // image position x,y = 0,0
        writeShort(0);
        writeShort(img_width); // image size
        writeShort(img_height);
        // packed fields
        if (firstFrame) {
            // no LCT - GCT is used for first (or only) frame
            output_gif.write(0);
        } else {
            // specify normal LCT
            output_gif.write(0x80 | // 1 local color table 1=yes
                    0 | // 2 interlace - 0=no
                    0 | // 3 sorted - 0=no
                    0 | // 4-5 reserved
                    7); // 6-8 size of color table palSize
        }
    }

    /**
     * Writes Logical Screen Descriptor
     */
    public static void writeLSD() throws IOException {
        // logical screen size
        writeShort(img_width);
        writeShort(img_height);
        // packed fields
        output_gif.write((0x80 | // 1 : global color table flag = 1 (gct used)
                0x70 | // 2-4 : color resolution = 7
                0x00 | // 5 : gct sort flag = 0
                7)); // 6-8 : gct size,palSize

        output_gif.write(0); // background color index
        output_gif.write(0); // pixel aspect ratio - assume 1:1
    }

    /**
     * Writes Netscape application extension to define repeat count.
     */
    public static void writeNetscapeExt() throws IOException {
        output_gif.write(0x21); // extension introducer
        output_gif.write(0xff); // app extension label
        output_gif.write(11); // block size
        writeString("NETSCAPE" + "2.0"); // app id + auth code
        output_gif.write(3); // sub-block size
        output_gif.write(1); // loop sub-block id
        writeShort(repeat); // loop count (extra iterations, 0=repeat forever)
        output_gif.write(0); // block terminator
    }

    /**
     * Writes color table
     */
    public static void writePalette() throws IOException {
        output_gif.write(colorTab, 0, colorTab.length);
        int n = (3 * 256) - colorTab.length;
        for (int i = 0; i < n; i++) {
            output_gif.write(0);
        }
    }

    /**
     * Encodes and writes pixel data
     */
    public static void writePixels() throws IOException {
        LZWEncoder encoder = new LZWEncoder(img_width, img_height, indexedPixels, 8); //color_depth
        encoder.encode(output_gif);
    }

    /**
     * Write 16-bit value to output stream, LSB first
     */
    public static void writeShort(int value) throws IOException {
        output_gif.write(value & 0xff);
        output_gif.write((value >> 8) & 0xff);
    }
}


class NeuQuant {
    /*
     * NeuQuant Neural-Net Quantization Algorithm
     * ------------------------------------------
     *
     * Copyright (c) 1994 Anthony Dekker
     *
     * NEUQUANT Neural-Net quantization algorithm by Anthony Dekker, 1994. See
     * "Kohonen neural networks for optimal colour quantization" in "Network:
     * Computation in Neural Systems" Vol. 5 (1994) pp 351-367. for a discussion of
     * the algorithm.
     *
     * Any party obtaining a copy of these files from the author, directly or
     * indirectly, is granted, free of charge, a full and unrestricted irrevocable,
     * world-wide, paid up, royalty-free, nonexclusive right and license to deal in
     * this software and documentation files (the "Software"), including without
     * limitation the rights to use, copy, modify, merge, publish, distribute,
     * sublicense, and/or sell copies of the Software, and to permit persons who
     * receive copies from any such party to do so, with the only requirement being
     * that this copyright notice remain intact.
     */
    public static final int netsize = 256; /* number of colours used */

    /* four primes near 500 - assume no image has a length so large */
    /* that it is divisible by all four primes */
    public static final int prime1 = 499;

    public static final int prime2 = 491;

    public static final int prime3 = 487;

    public static final int prime4 = 503;

    public static final int minpicturebytes = (3 * prime4);

    /* minimum size for input image */

    /*
     * Program Skeleton ---------------- [select samplefac in range 1..30] [read
     * image from input file] pic = (unsigned char*) malloc(3*width*height);
     * initnet(pic,3*width*height,samplefac); learn(); unbiasnet(); [write output
     * image header, using writecolourmap(f)] inxbuild(); write output image using
     * inxsearch(b,g,r)
     */

    /*
     * Network Definitions -------------------
     */

    public static final int maxnetpos = (netsize - 1);

    public static final int netbiasshift = 4; /* bias for colour values */

    public static final int ncycles = 100; /* no. of learning cycles */

    /* defs for freq and bias */
    public static final int intbiasshift = 16; /* bias for fractions */

    public static final int intbias = (((int) 1) << intbiasshift);

    public static final int gammashift = 10; /* gamma = 1024 */

    public static final int gamma = (((int) 1) << gammashift);

    public static final int betashift = 10;

    public static final int beta = (intbias >> betashift); /* beta = 1/1024 */

    public static final int betagamma = (intbias << (gammashift - betashift));

    /* defs for decreasing radius factor */
    public static final int initrad = (netsize >> 3); /*
     * for 256 cols, radius
     * starts
     */

    public static final int radiusbiasshift = 6; /* at 32.0 biased by 6 bits */

    public static final int radiusbias = (((int) 1) << radiusbiasshift);

    public static final int initradius = (initrad * radiusbias); /*
     * and
     * decreases
     * by a
     */

    public static final int radiusdec = 30; /* factor of 1/30 each cycle */

    /* defs for decreasing alpha factor */
    public static final int alphabiasshift = 10; /* alpha starts at 1.0 */

    public static final int initalpha = (((int) 1) << alphabiasshift);

    public static int alphadec; /* biased by 10 bits */

    /* radbias and alpharadbias used for radpower calculation */
    public static final int radbiasshift = 8;

    public static final int radbias = (((int) 1) << radbiasshift);

    public static final int alpharadbshift = (alphabiasshift + radbiasshift);

    public static final int alpharadbias = (((int) 1) << alpharadbshift);

    /*
     * Types and Global Variables --------------------------
     */

    public static byte[] thepicture; /* the input image itself */

    public static int lengthcount; /* lengthcount = H*W*3 */

    public static int samplefac; /* sampling factor 1..30 */

    // typedef int pixel[4]; /* BGRc */
    public static int[][] network; /* the network itself - [netsize][4] */

    public static int[] netindex = new int[256];

    /* for network lookup - really 256 */

    public static int[] bias = new int[netsize];

    /* bias and freq arrays for learning */
    public static int[] freq = new int[netsize];

    public static int[] radpower = new int[initrad];

    /* radpower for precomputation */

    /*
     * Initialise network in range (0,0,0) to (255,255,255) and set parameters
     * -----------------------------------------------------------------------
     */
    public NeuQuant(byte[] thepic, int len, int sample) {

        int i;
        int[] p;

        thepicture = thepic;
        lengthcount = len;
        samplefac = sample;

        network = new int[netsize][];
        for (i = 0; i < netsize; i++) {
            network[i] = new int[4];
            p = network[i];
            p[0] = p[1] = p[2] = (i << (netbiasshift + 8)) / netsize;
            freq[i] = intbias / netsize; /* 1/netsize */
            bias[i] = 0;
        }
    }

    public static byte[] colorMap() {
        byte[] map = new byte[3 * netsize];
        int[] index = new int[netsize];
        for (int i = 0; i < netsize; i++)
            index[network[i][3]] = i;
        int k = 0;
        for (int i = 0; i < netsize; i++) {
            int j = index[i];
            map[k++] = (byte) (network[j][0]);
            map[k++] = (byte) (network[j][1]);
            map[k++] = (byte) (network[j][2]);
        }
        return map;
    }

    /*
     * Insertion sort of network and building of netindex[0..255] (to do after
     * unbias)
     * -------------------------------------------------------------------------------
     */
    public static void inxbuild() {

        int i, j, smallpos, smallval;
        int[] p;
        int[] q;
        int previouscol, startpos;

        previouscol = 0;
        startpos = 0;
        for (i = 0; i < netsize; i++) {
            p = network[i];
            smallpos = i;
            smallval = p[1]; /* index on g */
            /* find smallest in i..netsize-1 */
            for (j = i + 1; j < netsize; j++) {
                q = network[j];
                if (q[1] < smallval) { /* index on g */
                    smallpos = j;
                    smallval = q[1]; /* index on g */
                }
            }
            q = network[smallpos];
            /* swap p (i) and q (smallpos) entries */
            if (i != smallpos) {
                j = q[0];
                q[0] = p[0];
                p[0] = j;
                j = q[1];
                q[1] = p[1];
                p[1] = j;
                j = q[2];
                q[2] = p[2];
                p[2] = j;
                j = q[3];
                q[3] = p[3];
                p[3] = j;
            }
            /* smallval entry is now in position i */
            if (smallval != previouscol) {
                netindex[previouscol] = (startpos + i) >> 1;
                for (j = previouscol + 1; j < smallval; j++)
                    netindex[j] = i;
                previouscol = smallval;
                startpos = i;
            }
        }
        netindex[previouscol] = (startpos + maxnetpos) >> 1;
        for (j = previouscol + 1; j < 256; j++)
            netindex[j] = maxnetpos; /* really 256 */
    }

    /*
     * Main Learning Loop ------------------
     */
    public static void learn() {

        int i, j, b, g, r;
        int radius, rad, alpha, step, delta, samplepixels;
        byte[] p;
        int pix, lim;

        if (lengthcount < minpicturebytes)
            samplefac = 1;
        alphadec = 30 + ((samplefac - 1) / 3);
        p = thepicture;
        pix = 0;
        lim = lengthcount;
        samplepixels = lengthcount / (3 * samplefac);
        delta = samplepixels / ncycles;
        alpha = initalpha;
        radius = initradius;

        rad = radius >> radiusbiasshift;
        if (rad <= 1)
            rad = 0;
        for (i = 0; i < rad; i++)
            radpower[i] = alpha * (((rad * rad - i * i) * radbias) / (rad * rad));

        // fprintf(stderr,"beginning 1D learning: initial radius=%d\n", rad);

        if (lengthcount < minpicturebytes)
            step = 3;
        else if ((lengthcount % prime1) != 0)
            step = 3 * prime1;
        else {
            if ((lengthcount % prime2) != 0)
                step = 3 * prime2;
            else {
                if ((lengthcount % prime3) != 0)
                    step = 3 * prime3;
                else
                    step = 3 * prime4;
            }
        }

        i = 0;
        while (i < samplepixels) {
            b = (p[pix + 0] & 0xff) << netbiasshift;
            g = (p[pix + 1] & 0xff) << netbiasshift;
            r = (p[pix + 2] & 0xff) << netbiasshift;
            j = contest(b, g, r);

            altersingle(alpha, j, b, g, r);
            if (rad != 0)
                alterneigh(rad, j, b, g, r); /* alter neighbours */

            pix += step;
            if (pix >= lim)
                pix -= lengthcount;

            i++;
            if (delta == 0)
                delta = 1;
            if (i % delta == 0) {
                alpha -= alpha / alphadec;
                radius -= radius / radiusdec;
                rad = radius >> radiusbiasshift;
                if (rad <= 1)
                    rad = 0;
                for (j = 0; j < rad; j++)
                    radpower[j] = alpha * (((rad * rad - j * j) * radbias) / (rad * rad));
            }
        }
        // fprintf(stderr,"finished 1D learning: final alpha=%f
        // !\n",((float)alpha)/initalpha);
    }

    /*
     * Search for BGR values 0..255 (after net is unbiased) and return colour
     * index
     * ----------------------------------------------------------------------------
     */
    public static int map(int b, int g, int r) {

        int i, j, dist, a, bestd;
        int[] p;
        int best;

        bestd = 1000; /* biggest possible dist is 256*3 */
        best = -1;
        i = netindex[g]; /* index on g */
        j = i - 1; /* start at netindex[g] and work outwards */

        while ((i < netsize) || (j >= 0)) {
            if (i < netsize) {
                p = network[i];
                dist = p[1] - g; /* inx key */
                if (dist >= bestd)
                    i = netsize; /* stop iter */
                else {
                    i++;
                    if (dist < 0)
                        dist = -dist;
                    a = p[0] - b;
                    if (a < 0)
                        a = -a;
                    dist += a;
                    if (dist < bestd) {
                        a = p[2] - r;
                        if (a < 0)
                            a = -a;
                        dist += a;
                        if (dist < bestd) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
            if (j >= 0) {
                p = network[j];
                dist = g - p[1]; /* inx key - reverse dif */
                if (dist >= bestd)
                    j = -1; /* stop iter */
                else {
                    j--;
                    if (dist < 0)
                        dist = -dist;
                    a = p[0] - b;
                    if (a < 0)
                        a = -a;
                    dist += a;
                    if (dist < bestd) {
                        a = p[2] - r;
                        if (a < 0)
                            a = -a;
                        dist += a;
                        if (dist < bestd) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
        }
        return (best);
    }

    public static byte[] process() {
        learn();
        unbiasnet();
        inxbuild();
        return colorMap();
    }

    /*
     * Unbias network to give byte values 0..255 and record position i to prepare
     * for sort
     * -----------------------------------------------------------------------------------
     */
    public static void unbiasnet() {

        int i, j;

        for (i = 0; i < netsize; i++) {
            network[i][0] >>= netbiasshift;
            network[i][1] >>= netbiasshift;
            network[i][2] >>= netbiasshift;
            network[i][3] = i; /* record colour no */
        }
    }

    /*
     * Move adjacent neurons by precomputed alpha*(1-((i-j)^2/[r]^2)) in
     * radpower[|i-j|]
     * ---------------------------------------------------------------------------------
     */
    public static void alterneigh(int rad, int i, int b, int g, int r) {

        int j, k, lo, hi, a, m;
        int[] p;

        lo = i - rad;
        if (lo < -1)
            lo = -1;
        hi = i + rad;
        if (hi > netsize)
            hi = netsize;

        j = i + 1;
        k = i - 1;
        m = 1;
        while ((j < hi) || (k > lo)) {
            a = radpower[m++];
            if (j < hi) {
                p = network[j++];
                try {
                    p[0] -= (a * (p[0] - b)) / alpharadbias;
                    p[1] -= (a * (p[1] - g)) / alpharadbias;
                    p[2] -= (a * (p[2] - r)) / alpharadbias;
                } catch (Exception e) {
                } // prevents 1.3 miscompilation
            }
            if (k > lo) {
                p = network[k--];
                try {
                    p[0] -= (a * (p[0] - b)) / alpharadbias;
                    p[1] -= (a * (p[1] - g)) / alpharadbias;
                    p[2] -= (a * (p[2] - r)) / alpharadbias;
                } catch (Exception e) {
                }
            }
        }
    }

    /*
     * Move neuron i towards biased (b,g,r) by factor alpha
     * ----------------------------------------------------
     */
    public static void altersingle(int alpha, int i, int b, int g, int r) {

        /* alter hit neuron */
        int[] n = network[i];
        n[0] -= (alpha * (n[0] - b)) / initalpha;
        n[1] -= (alpha * (n[1] - g)) / initalpha;
        n[2] -= (alpha * (n[2] - r)) / initalpha;
    }

    /*
     * Search for biased BGR values ----------------------------
     */
    public static int contest(int b, int g, int r) {

        /* finds closest neuron (min dist) and updates freq */
        /* finds best neuron (min dist-bias) and returns position */
        /* for frequently chosen neurons, freq[i] is high and bias[i] is negative */
        /* bias[i] = gamma*((1/netsize)-freq[i]) */

        int i, dist, a, biasdist, betafreq;
        int bestpos, bestbiaspos, bestd, bestbiasd;
        int[] n;

        bestd = ~(((int) 1) << 31);
        bestbiasd = bestd;
        bestpos = -1;
        bestbiaspos = bestpos;

        for (i = 0; i < netsize; i++) {
            n = network[i];
            dist = n[0] - b;
            if (dist < 0)
                dist = -dist;
            a = n[1] - g;
            if (a < 0)
                a = -a;
            dist += a;
            a = n[2] - r;
            if (a < 0)
                a = -a;
            dist += a;
            if (dist < bestd) {
                bestd = dist;
                bestpos = i;
            }
            biasdist = dist - ((bias[i]) >> (intbiasshift - netbiasshift));
            if (biasdist < bestbiasd) {
                bestbiasd = biasdist;
                bestbiaspos = i;
            }
            betafreq = (freq[i] >> betashift);
            freq[i] -= betafreq;
            bias[i] += (betafreq << gammashift);
        }
        freq[bestpos] += beta;
        bias[bestpos] -= betagamma;
        return (bestbiaspos);
    }
}

// ==============================================================================
// Adapted from Jef Poskanzer's Java port by way of J. M. G. Elliott.
// K Weiner 12/00

class LZWEncoder {
    /*
     * Copyright (C) 2015 Square, Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    private static final int EOF = -1;

    private int imgW, imgH;

    private byte[] pixAry;

    private int initCodeSize;

    private int remaining;

    private int curPixel;

    // GIFCOMPR.C - GIF Image compression routines
    //
    // Lempel-Ziv compression based on 'compress'. GIF modifications by
    // David Rowley (mgardi@watdcsu.waterloo.edu)

    // General DEFINEs

    static final int BITS = 12;

    static final int HSIZE = 5003; // 80% occupancy

    // GIF Image compression - modified 'compress'
    //
    // Based on: compress.c - File compression ala IEEE Computer, June 1984.
    //
    // By Authors: Spencer W. Thomas (decvax!harpo!utah-cs!utah-gr!thomas)
    // Jim McKie (decvax!mcvax!jim)
    // Steve Davies (decvax!vax135!petsd!peora!srd)
    // Ken Turkowski (decvax!decwrl!turtlevax!ken)
    // James A. Woods (decvax!ihnp4!ames!jaw)
    // Joe Orost (decvax!vax135!petsd!joe)

    int n_bits; // number of bits/code

    int maxbits = BITS; // user settable max # bits/code

    int maxcode; // maximum code, given n_bits

    int maxmaxcode = 1 << BITS; // should NEVER generate this code

    int[] htab = new int[HSIZE];

    int[] codetab = new int[HSIZE];

    int hsize = HSIZE; // for dynamic table sizing

    int free_ent = 0; // first unused entry

    // block compression parameters -- after all codes are used up,
    // and compression rate changes, start over.
    boolean clear_flg = false;

    // Algorithm: use open addressing double hashing (no chaining) on the
    // prefix code / next character combination. We do a variant of Knuth's
    // algorithm D (vol. 3, sec. 6.4) along with G. Knott's relatively-prime
    // secondary probe. Here, the modular division first probe is gives way
    // to a faster exclusive-or manipulation. Also do block compression with
    // an adaptive reset, whereby the code table is cleared when the compression
    // ratio decreases, but after the table fills. The variable-length output
    // codes are re-sized at this point, and a special CLEAR code is generated
    // for the decompressor. Late addition: construct the table according to
    // file size for noticeable speed improvement on small files. Please direct
    // questions about this implementation to ames!jaw.

    int g_init_bits;

    int ClearCode;

    int EOFCode;

    // output
    //
    // Output the given code.
    // Inputs:
    // code: A n_bits-bit integer. If == -1, then EOF. This assumes
    // that n_bits =< wordsize - 1.
    // Outputs:
    // Outputs code to the file.
    // Assumptions:
    // Chars are 8 bits long.
    // Algorithm:
    // Maintain a BITS character long buffer (so that 8 codes will
    // fit in it exactly). Use the VAX insv instruction to insert each
    // code in turn. When the buffer fills up empty it and start over.

    int cur_accum = 0;

    int cur_bits = 0;

    int masks[] = { 0x0000, 0x0001, 0x0003, 0x0007, 0x000F, 0x001F, 0x003F, 0x007F, 0x00FF, 0x01FF,
            0x03FF, 0x07FF, 0x0FFF, 0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF };

    // Number of characters so far in this 'packet'
    int a_count;

    // Define the storage for the packet accumulator
    byte[] accum = new byte[256];

    // ----------------------------------------------------------------------------
    LZWEncoder(int width, int height, byte[] pixels, int color_depth) {
        imgW = width;
        imgH = height;
        pixAry = pixels;
        initCodeSize = Math.max(2, color_depth);
    }

    // Add a character to the end of the current packet, and if it is 254
    // characters, flush the packet to disk.
    void char_out(byte c, OutputStream outs) throws IOException {
        accum[a_count++] = c;
        if (a_count >= 254)
            flush_char(outs);
    }

    // Clear out the hash table

    // table clear for block compress
    void cl_block(OutputStream outs) throws IOException {
        cl_hash(hsize);
        free_ent = ClearCode + 2;
        clear_flg = true;
        output(ClearCode, outs);
    }

    // reset code table
    void cl_hash(int hsize) {
        for (int i = 0; i < hsize; ++i)
            htab[i] = -1;
    }

    void compress(int init_bits, OutputStream outs) throws IOException {
        int fcode;
        int i /* = 0 */;
        int c;
        int ent;
        int disp;
        int hsize_reg;
        int hshift;

        // Set up the globals: g_init_bits - initial number of bits
        g_init_bits = init_bits;

        // Set up the necessary values
        clear_flg = false;
        n_bits = g_init_bits;
        maxcode = MAXCODE(n_bits);

        ClearCode = 1 << (init_bits - 1);
        EOFCode = ClearCode + 1;
        free_ent = ClearCode + 2;

        a_count = 0; // clear packet

        ent = nextPixel();

        hshift = 0;
        for (fcode = hsize; fcode < 65536; fcode *= 2)
            ++hshift;
        hshift = 8 - hshift; // set hash code range bound

        hsize_reg = hsize;
        cl_hash(hsize_reg); // clear hash table

        output(ClearCode, outs);

        outer_loop: while ((c = nextPixel()) != EOF) {
            fcode = (c << maxbits) + ent;
            i = (c << hshift) ^ ent; // xor hashing

            if (htab[i] == fcode) {
                ent = codetab[i];
                continue;
            } else if (htab[i] >= 0) // non-empty slot
            {
                disp = hsize_reg - i; // secondary hash (after G. Knott)
                if (i == 0)
                    disp = 1;
                do {
                    if ((i -= disp) < 0)
                        i += hsize_reg;

                    if (htab[i] == fcode) {
                        ent = codetab[i];
                        continue outer_loop;
                    }
                } while (htab[i] >= 0);
            }
            output(ent, outs);
            ent = c;
            if (free_ent < maxmaxcode) {
                codetab[i] = free_ent++; // code -> hashtable
                htab[i] = fcode;
            } else
                cl_block(outs);
        }
        // Put out the final code.
        output(ent, outs);
        output(EOFCode, outs);
    }

    // ----------------------------------------------------------------------------
    void encode(OutputStream os) throws IOException {
        os.write(initCodeSize); // write "initial code size" byte

        remaining = imgW * imgH; // reset navigation variables
        curPixel = 0;

        compress(initCodeSize + 1, os); // compress and write the pixel data

        os.write(0); // write block terminator
    }

    // Flush the packet to disk, and reset the accumulator
    void flush_char(OutputStream outs) throws IOException {
        if (a_count > 0) {
            outs.write(a_count);
            outs.write(accum, 0, a_count);
            a_count = 0;
        }
    }

    final int MAXCODE(int n_bits) {
        return (1 << n_bits) - 1;
    }

    // ----------------------------------------------------------------------------
    // Return the next pixel from the image
    // ----------------------------------------------------------------------------
    private int nextPixel() {
        if (remaining == 0)
            return EOF;

        --remaining;

        byte pix = pixAry[curPixel++];

        return pix & 0xff;
    }

    void output(int code, OutputStream outs) throws IOException {
        cur_accum &= masks[cur_bits];

        if (cur_bits > 0)
            cur_accum |= (code << cur_bits);
        else
            cur_accum = code;

        cur_bits += n_bits;

        while (cur_bits >= 8) {
            char_out((byte) (cur_accum & 0xff), outs);
            cur_accum >>= 8;
            cur_bits -= 8;
        }

        // If the next entry is going to be too big for the code size,
        // then increase it, if possible.
        if (free_ent > maxcode || clear_flg) {
            if (clear_flg) {
                maxcode = MAXCODE(n_bits = g_init_bits);
                clear_flg = false;
            } else {
                ++n_bits;
                if (n_bits == maxbits)
                    maxcode = maxmaxcode;
                else
                    maxcode = MAXCODE(n_bits);
            }
        }

        if (code == EOFCode) {
            // At EOF, write the rest of the buffer.
            while (cur_bits > 0) {
                char_out((byte) (cur_accum & 0xff), outs);
                cur_accum >>= 8;
                cur_bits -= 8;
            }

            flush_char(outs);
        }
    }
}
