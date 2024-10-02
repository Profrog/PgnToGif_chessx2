package io.github.profrog;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;


/**
 *  PgnToImage help to make image set data from board data from PgnParse
 *  simply, from int[][] to images(.png)
 *  @author mingyu Kim(ache159@naver.com)
 */
public class PgnToImage {


    /**
     *  board size mean that chess board's volume, in originally, chess board has 8 spaces, and then it would have 8
     */
    public static int board_size = PgnParse.board_size;

    /**
     *  original_images mean that image set image source about board and pieces,
     *  board's numbering 00(.png,,, .jpg,,, .jpeg,,, .bmp)
     *  white pawn 01, white night 02, white bishop 03, white rook 04, white queen 05, white king 06
     *  black pawn 07, black night 08, black bishop 09, black rook 10, black queen 11, black king 12
     */
    public static List<BufferedImage> original_images;

    /**
     *  output_images mean that image set board scene of combination of board and pieces
     *  Numbered according to move order
     */
    public static List<BufferedImage> output_images;

    /**
     *  piece_width, piece_height,board_width,board_height is size of piece and board image
     */
    public static int board_width = 400;
    public static int board_height = 400;
    public static int piece_width;
    public static int piece_height;

    /**
     *  output dir is saving directory for output_images, output_folder is folder name which save ouput_iamges
     */
    public static String output_dir;
    public static String output_folder = "/output";

    /**
     *  numbering_format is data for filter numbering name for output_images, example "%05d" mean that 5-digit number management
     */
    public static String numbering_format = "%05d";

    /**
     *  extension_type is image extension type for saving, likely png, jpg, jpeg..etc
     */
    public static String extension_type = "png";


    /**
     * it is method for controlling gif data form BufferedImages in gif_chessx2
     * @param check_table_mem - data whcih save pgn data to table, and it's output for PgnParse
     * @param original_dir - directory for original images
     * example1 : PgnToImage.imageInit(PgnParse.parserInit(pgn_example,0,0), "/home/mingyu/Pictures/chess");
     */
    public static String imageInit(List<int[][]> check_table_mem, String original_dir, String skin_dir) throws IOException {
        int index = 0;
        connectLinkToImage(original_dir,skin_dir);
        output_images = new ArrayList<>();
        output_dir = original_dir +  output_folder + "/";
        File directory = new File(output_dir);

        if (directory.exists()) {
            deleteFolder(directory);
        }
        directory.mkdirs();

        for (int idx = 0; idx < check_table_mem.size(); ++idx) {
            BufferedImage cur_img = makeImage(check_table_mem.get(idx));
            output_images.add(cur_img);
            saveImage(cur_img, String.format(numbering_format,++index),0.2f);
            System.out.println("make output image " + String.format(numbering_format,index) + "." + extension_type);
        }

        System.out.println("finished PgnToImage Process");
        return original_dir + output_folder;
    }

    /**
     * it is method for deleting output folder and resetting
     * @param folder - directory for deleting
     * example1 :  deleteFolder("/home/mingyu/Pictures/chess");
     */
    public static boolean deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {  // 폴더가 비어있지 않은 경우
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);  // 재귀적으로 하위 폴더 삭제
                } else {
                    file.delete();  // 파일 삭제
                }
            }
        }
        return folder.delete();  // 최종적으로 폴더 삭제
    }


    /**
     * it is method for getting input image about board and piece
     * @param original_dir - directory for original images
     * example1 :  connectLinkToImage("/home/mingyu/Pictures/chess");
     */
    public static boolean connectLinkToImage(String original_dir,String skin_dir) {
        original_images = new ArrayList<>();
        File[] imageFiles;

        if(skin_dir == null) {
            File directory = new File(original_dir);
            imageFiles = directory.listFiles((dir, name) -> {
                String nameLower = name.toLowerCase();
                return nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") || nameLower.endsWith(".png") || nameLower.endsWith(".bmp");
            });

            if (imageFiles != null && imageFiles.length > 0) {
                //sorting by file name
                Arrays.sort(imageFiles, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {
                        return file1.getName().compareTo(file2.getName());
                    }
                });
            }
        }

        else {
            String[] paths = skin_dir.replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .split(",");

            imageFiles = new File[paths.length];
            for(int idx = 0; idx < paths.length; ++idx)
            {
                imageFiles[idx] = new File(paths[idx]);
            }
        }
        for (int idx = 0; idx < imageFiles.length; idx++) {
            try {
                if (idx == 0) {
                    original_images.add(resizeImage(ImageIO.read(imageFiles[idx]), board_width, board_height));
                    board_width = original_images.get(0).getWidth();
                    board_height = original_images.get(0).getHeight();
                    piece_width = board_width / board_size;
                    piece_height = board_height / board_size;
                } else {
                    original_images.add(resizeImage(ImageIO.read(imageFiles[idx]), piece_width, piece_height));
                }
                System.out.println("connect input image " + idx + " " + imageFiles[idx].getName());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * it is method for making image from current move data
     * @param cur_table - chess table about current move
     * example1 :  makeImage(PgnParse.cur_chess_table)
     */
    public static BufferedImage makeImage(int[][] cur_table) {
        BufferedImage returnImage = new BufferedImage(board_width, board_height, original_images.get(0).getType());
        Graphics2D g2d = returnImage.createGraphics();
        g2d.drawImage(original_images.get(0), 0, 0, null);
        g2d.dispose();

        for (int row0 = 0; row0 < board_size; ++row0) {
            for (int col0 = 0; col0 < board_size; ++col0) {
                int piece_data = cur_table[row0][col0];
                if (piece_data > 0) {
                    g2d = returnImage.createGraphics();
                    g2d.drawImage(original_images.get(piece_data), col0 * piece_height, row0 * piece_width, null);
                    g2d.dispose();
                }
            }
        }

        return returnImage;
    }

    /**
     * it is method for resizing image following user intent
     * @param original - original image for modifying
     * @param width - width size for modifying
     * @param height - height size for modifying
     * example1 : resizeImage(ImageIO.read(imageFiles[idx])
     */
    public static BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // Get the Graphics2D object
        Graphics2D g2d = resizedImage.createGraphics();
        // Draw the original image to the new image
        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();
        return resizedImage;
    }

    /**
     * it is method for saving image following user intent
     * @param original0 - original image for saving
     * @param name - name for saving
     * example1 : resizeImage(ImageIO.read(imageFiles[idx])
     */
    public static boolean saveImage(BufferedImage original0, String name,float quality) throws IOException {
        File outputFile = new File(output_dir + name + "." + extension_type);
        if(extension_type == "jpg") {
            BufferedImage original = new BufferedImage(original0.getWidth(), original0.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = original.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, original.getWidth(), original.getHeight());
            // PNG 이미지 그리기
            g.drawImage(original0, 0, 0, null);
            g.dispose(); // 그래픽스 객체 해제

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next(); // JPEG 형식의 첫 번째 writer를 선택
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
                writer.setOutput(ios);

                // 압축 파라미터 설정
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(quality); // 설정한 품질로 압축
                }
                // 이미지 저장
                writer.write(null, new javax.imageio.IIOImage(original, null, null), param);
            } finally {
                writer.dispose(); // writer 자원 해제
            }
        }

        else { //png 케이스
            ImageIO.write(original0, extension_type, outputFile);
        }
        return true;
    }

    /**
     * it is method for debugging current chess board data
     * @param cur_table - chess table about current move
     * example1 :  showTableValue(PgnParse.cur_chess_table)
     */
    public static void showTableValue(int[][] cur_table) {

        for (int row0 = 0; row0 < board_size; ++row0) {
            for (int col0 = 0; col0 < board_size; ++col0) {
                System.out.print(cur_table[row0][col0]);
            }
            System.out.println("");
        }
        System.out.println("");
    }

}
