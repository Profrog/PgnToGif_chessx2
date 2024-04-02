package io.github.profrog;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class PgnParse {

    public static Logger logger = Logger.getLogger(PgnParse.class.getName());

    public static int[][] chess_table;
    public static Image[] chess_img;
    public static Color boardline_lout;
    public static String pgn_data;
    public static Map<String,String> pgn_state;

    public static List<String> white_move;
    public static  List<String> black_move;


    public static String pgn_example = "\n" +
            "[Event \"Live Chess\"]\n" +
            "[Site \"Chess.com\"]\n" +
            "[Date \"2024.03.23\"]\n" +
            "[Round \"?\"]\n" +
            "[White \"MagicRoss\"]\n" +
            "[Black \"PFchessX2\"]\n" +
            "[Result \"0-1\"]\n" +
            "[ECO \"D02\"]\n" +
            "[WhiteElo \"1139\"]\n" +
            "[BlackElo \"1150\"]\n" +
            "[TimeControl \"120+1\"]\n" +
            "[EndTime \"19:04:44 PDT\"]\n" +
            "[Termination \"PFchessX2 won by checkmate\"]\n" +
            "\n" +
            "1. d4 d5 2. Nf3 Nc6 3. e3 Bf5 4. Bb5 a6 5. O-O e6 6. Bxc6+ bxc6 7. c3 Bd6 8. Re1\n" +
            "Nf6 9. h3 h6 10. Nh4 Bh7 11. g4 Ne4 12. Nd2 Qxh4 13. Nxe4 Bxe4 14. f3 Qg3+ 15.\n" +
            "Kf1 Bxf3 16. Qd2 Qxh3+ 17. Kg1 Qh1+ 18. Kf2 Qg2# 0-1\n";

    public static String pgn_example2 = "\n" +
            "[Event \"Casual 긴 대국 game\"]\n" +
            "[Site \"https://lichess.org/3FLP6aWA\"]\n" +
            "[Date \"2024.04.02\"]\n" +
            "[White \"lichess AI level 1\"]\n" +
            "[Black \"profrog\"]\n" +
            "[Result \"0-1\"]\n" +
            "[UTCDate \"2024.04.02\"]\n" +
            "[UTCTime \"14:59:54\"]\n" +
            "[WhiteElo \"?\"]\n" +
            "[BlackElo \"1500\"]\n" +
            "[Variant \"Standard\"]\n" +
            "[TimeControl \"-\"]\n" +
            "[ECO \"C44\"]\n" +
            "[Opening \"Ponziani Opening\"]\n" +
            "[Termination \"Normal\"]\n" +
            "[Annotator \"lichess.org\"]\n" +
            "\n" +
            "1. e4 e5 2. Nf3 Nc6 3. c3 { C44 Ponziani Opening } Qf6 " +
            "4. d4 exd4 5. cxd4 Bb4+ 6. Ke2 Nh6 7. Nc3 Ng4 8. a3 Nxd4+ 9. Kd2 Bxc3+ 10. Kd3 Nxf2+ " +
            "11. Kc4 Nxd1 12. Rb1 b5+ 13. Kd5 Bb7+ 14. Kc5 d6# { Black wins by checkmate. } 0-1\n";




    //void parser_init(Image[] chess_img0, Color boardline_lout0, String pgn_data0)
    public static void parser_init(Image[] chess_img0, Color boardline_lout0, String pgn_data0)
    {
        chess_table = new int[8][8];
        chess_img = new Image[chess_img0.length];
        pgn_state = new HashMap<String,String>();

        for(int idx = 0; idx < chess_img0.length; ++idx)
        {
            chess_img[idx] = chess_img0[idx];
        }

        boardline_lout = boardline_lout0;
        pgn_data = pgn_data0;

        //chess_image[0] = white_pawn_image.png..jpg..bmp..
        //chess_image[1] = white_bishop_image.png..jpg..bmp..
        //chess_image[2] = white_rook_image.png..jpg..bmp..
        //chess_image[3] = white_knights_image.png..jpg..bmp..
        //chess_image[4] = white_queen_image.png..jpg..bmp..
        //chess_image[5] = white_king_image.png..jpg..bmp..

        //chess_image[5] = black_pawn_image.png..jpg..bmp..
        //chess_image[6] = black_bishop_image.png..jpg..bmp..
        //chess_image[7] = black_rook_image.png..jpg..bmp..
        //chess_image[8] = black_knights_image.png..jpg..bmp..
        //chess_image[9] = black_queen_image.png..jpg..bmp..
        //chess_image[10] = black_king_image.png..jpg..bmp..
        //chess_image[11] = board_image.png..jpg..bmp..
    }

    public static void only_parser(String pgn_data0)
    {
        try {
            chess_table = new int[8][8];
            pgn_state = new HashMap<String, String>();
            white_move = new ArrayList<>();
            black_move = new ArrayList<>();

            String[] raw_data1 = pgn_data0.split("]");

            for (int idx = 0; idx < raw_data1.length - 1; ++idx) {

                String[] raw_data2 = raw_data1[idx].split("\\[");

                if(raw_data2.length < 2)
                {
                    continue;
                }

                String[] raw_data3 = raw_data2[1].split("\"");

                if(raw_data3.length < 2)
                {
                    continue;
                }

                raw_data3[0] = raw_data3[0].trim();
                raw_data3[1] = raw_data3[1].trim();
                pgn_state.put(raw_data3[0],raw_data3[1]);
            }


            String move_file ="";
            int load_check = 1;

            for(int idx = 0; idx < raw_data1[raw_data1.length -1].length(); ++idx)
            {
                if(raw_data1[raw_data1.length -1].charAt(idx) == '{')
                {
                    load_check = 0;
                }

                if(raw_data1[raw_data1.length -1].charAt(idx) == '\n')
                {
                    continue;
                }

                else if(load_check == 1)
                {
                    move_file += raw_data1[raw_data1.length -1].charAt(idx);
                }

                if(raw_data1[raw_data1.length -1].charAt(idx) == '}')
                {
                    load_check = 1;
                    ++idx;
                }
            }

            String[] move_rdata = move_file.split("\\.");

            for(int idx = 1 ; idx < move_rdata.length; ++idx)
            {
                move_rdata[idx] = move_rdata[idx].trim();

                String[] move_rdata_state = move_rdata[idx].split(" ");
                white_move.add(move_rdata_state[0].trim());
                black_move.add(move_rdata_state[1].trim());
            }
        }

        catch (Exception e)
        {
            System.out.println("only parser error " + e.getMessage() );
        }
    }


    public static void main(String[] args){
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        only_parser(pgn_example2);
        //System.out.printf("Hello and welcome!");
    }

    public static String getvalue()
    {
        return "chessx2";
    }
}