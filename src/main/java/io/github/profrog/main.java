package io.github.profrog;

import io.github.profrog.PgnParse;

import java.awt.*;
import java.util.HashMap;

public class main {

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
            "1. d4 d5 2. Nf3 Nc6 3. e3 Bf5 4. Bb5 a6 5. O-O e6 6. Bxc6+ bxc6 7. c3 Bd6 8. Re1 " +
            "Nf6 9. h3 h6 10. Nh4 Bh7 11. g4 Ne4 12. Nd2 Qxh4 13. Nxe4 Bxe4 14. f3 Qg3+ 15. " +
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


    public static void main(String[] args){
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        //PgnParse.showTableValue(0);
        PgnParse.onlyParser(pgn_example,0,1);
    }

    /*
    public static void parserInit(Image[] chess_img0, Color boardline_lout0, String pgn_data0, int wanttosee_table)
    {
        cur_chess_table = new int[8][8];
        chess_img = new Image[chess_img0.length];
        pgn_state = new HashMap<String,String>();

        for(int idx = 0; idx < chess_img0.length; ++idx)
        {
            chess_img[idx] = chess_img0[idx];
        }

        boardline_lout = boardline_lout0;
        pgn_data = pgn_data0;

        //chess_image[0] = board_image.png..jpg..bmp..
        //chess_image[1] = white_pawn_image.png..jpg..bmp..
        //chess_image[2] = white_knight_image.png..jpg..bmp..
        //chess_image[3] = white_bishop_image.png..jpg..bmp..
        //chess_image[4] = white_rook_image.png..jpg..bmp..
        //chess_image[5] = white_queen_image.png..jpg..bmp..
        //chess_image[6] = white_king_image.png..jpg..bmp..

        //chess_image[7] = black_pawn_image.png..jpg..bmp..
        //chess_image[8] = black_knight_image.png..jpg..bmp..
        //chess_image[9] = black_bishop_image.png..jpg..bmp..
        //chess_image[10] = black_rook_image.png..jpg..bmp..
        //chess_image[11] = black_queen_image.png..jpg..bmp..
        //chess_image[12] = black_king_image.png..jpg..bmp..
    }

    */

}
