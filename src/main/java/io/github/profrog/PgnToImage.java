package io.github.profrog;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PgnToImage {

    public static int[][] example_data = {{10, 8, 9, 11, 12, 9, 8, 10},
            {7, 7, 7, 7, 7, 7, 7, 7},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {4, 2, 3, 5, 6, 3, 2, 4}};

    public static String board_dir = "";
    public static int piece_count = PgnParse.board_size;
    public static List<BufferedImage> image_raw;
    public static List<BufferedImage> output_image;

    public static int piece_width = 720;
    public static int piece_height = 720;
    public static int board_width;
    public static int board_height;

    public static String output_dir;


    public static int piece_size;

    public static String imageInit(List<int[][]> check_table_mem, String folder) throws IOException {
        int index = 0;

        connectLinkToImage(folder);
        output_image = new ArrayList<>();
        output_dir = folder + "/output/";
        File directory = new File(output_dir);

        if (directory.exists()) {
            directory.delete();
        }

        directory.mkdirs();
        for (int idx = 0; idx < check_table_mem.size(); ++idx) {
            //showTableValue(check_table_mem.get(idx));
            BufferedImage cur_img = makeImage(check_table_mem.get(idx));
            output_image.add(cur_img);
            File outputFile = new File(output_dir + String.format( "%02d",++index) + ".png");
            ImageIO.write(cur_img, "png", outputFile);
        }

        return folder + "/output";
    }

    public static boolean connectLinkToImage(String folder) {
        image_raw = new ArrayList<>();

        File directory = new File(folder);
        File[] imageFiles = directory.listFiles((dir, name) -> {
            String nameLower = name.toLowerCase();
            return nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") || nameLower.endsWith(".png") || nameLower.endsWith(".bmp");
        });

        if (imageFiles != null && imageFiles.length > 0) {
            // 파일 이름순으로 정렬
            Arrays.sort(imageFiles, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return file1.getName().compareTo(file2.getName());
                }
            });
        }

        for (int i = 0; i < imageFiles.length; i++) {
            try {
                System.out.println(i + " " + imageFiles[i].getName() + "\n");
                if (i == 0) {
                    image_raw.add(resizeImage(ImageIO.read(imageFiles[i]), piece_width, piece_height));
                    board_width = image_raw.get(0).getWidth();
                    board_height = image_raw.get(0).getHeight();
                    piece_width = board_width / piece_count;
                    piece_height = board_height / piece_count;
                } else {
                    image_raw.add(resizeImage(ImageIO.read(imageFiles[i]), piece_width, piece_height));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public static BufferedImage makeImage(int[][] cur_table) {
        BufferedImage returnImage = new BufferedImage(board_width, board_height, image_raw.get(0).getType());
        Graphics2D g2d = returnImage.createGraphics();
        g2d.drawImage(image_raw.get(0), 0, 0, null);
        g2d.dispose();

        for (int row0 = 0; row0 < piece_count; ++row0) {
            for (int col0 = 0; col0 < piece_count; ++col0) {
                int piece_data = cur_table[row0][col0];
                if (piece_data > 0) {
                    //System.out.println(image_raw.get(piece_data).getHeight() + "\n");
                    g2d = returnImage.createGraphics();
                    g2d.drawImage(image_raw.get(piece_data), col0 * piece_height, row0 * piece_width, null);
                    g2d.dispose();
                }
            }
        }

        return returnImage;
    }

    public static BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Get the Graphics2D object
        Graphics2D g2d = resizedImage.createGraphics();
        // Draw the original image to the new image
        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();
        return resizedImage;
    }

    public static boolean saveImage(int index, String folder) throws IOException {
        BufferedImage returnImage = new BufferedImage(image_raw.get(index).getWidth(), image_raw.get(index).getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = returnImage.createGraphics();
        g2d.drawImage(image_raw.get(index), 0, 0, null);
        g2d.dispose();

        File outputFile = new File(folder + "/output/" + "255.png");
        ImageIO.write(returnImage, "png", outputFile);
        return true;
    }

    public static void showTableValue(int[][] cur_table) {

        for (int row0 = 0; row0 < piece_count; ++row0) {
            for (int col0 = 0; col0 < piece_count; ++col0) {
                System.out.print(cur_table[row0][col0]);
            }
            System.out.println("");
        }
        System.out.println("");
    }

}
