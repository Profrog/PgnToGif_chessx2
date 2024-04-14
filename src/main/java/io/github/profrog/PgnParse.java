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

    public static int[][] cur_chess_table;
    public static  List<int[][]> check_table_mem;

    public static Image[] chess_img;
    public static Color boardline_lout;
    public static String pgn_data;
    public static Map<String,String> pgn_state;

    public static Map<String,Integer> piece_data;

    public static List<String> white_move;
    public static  List<String> black_move;

    public static final int board_size = 8; //in normally, chess board size is 8x8
    public static int left_p = 0; //left column basis index
    public static int right_p = board_size-1; //right column basis index
    public static int top_p = 0; //top row basis index
    public static int bottom_p = board_size -1; //bottom row basis index

    public static List<int[][]> move_condition;


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
    public static void parserInit(Image[] chess_img0, Color boardline_lout0, String pgn_data0,int wanttosee_table)
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

    public static void onlyParser(String pgn_data0, int black_bottom_opt, int want_seeing_table_opt)
    {
        try {
            cur_chess_table = new int[][]{ //definition first state of board, white piece 1~6, black piece 1~6
                    {10,8,9,11,12,9,8,10},
                    {7,7,7,7,7,7,7,7},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {1,1,1,1,1,1,1,1},
                    {4,2,3,5,6,3,2,4},
            };
            check_table_mem.add(cur_chess_table);

            pgn_state = new HashMap<String, String>();
            white_move = new ArrayList<>();
            black_move = new ArrayList<>();
            piece_data = new HashMap<>();


            piece_data.put("P",1); //piece meta data is added
            piece_data.put("N",2);
            piece_data.put("B",3);
            piece_data.put("R",4);
            piece_data.put("Q",5);
            piece_data.put("K",6);

            piece_data.put("8",8);
            piece_data.put("7",7);
            piece_data.put("6",6);
            piece_data.put("5",5);
            piece_data.put("4",4);
            piece_data.put("3",3);
            piece_data.put("2",2);
            piece_data.put("1",1);

            piece_data.put("h",8);
            piece_data.put("g",7);
            piece_data.put("f",6);
            piece_data.put("e",5);
            piece_data.put("d",4);
            piece_data.put("c",3);
            piece_data.put("b",2);
            piece_data.put("a",1);


            String[] raw_data1 = pgn_data0.split("]"); //extract piece data from pgn format
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


            StringBuilder move_file = new StringBuilder();
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
                    move_file.append(raw_data1[raw_data1.length -1].charAt(idx));
                }

                if(raw_data1[raw_data1.length -1].charAt(idx) == '}') //lichess format change to gif_chessx2 format
                {
                    load_check = 1;
                    ++idx;
                }
            }

            String[] move_rdata = move_file.toString().split("\\.");

            for(int idx = 1 ; idx < move_rdata.length; ++idx)
            {
                move_rdata[idx] = move_rdata[idx].trim();

                String[] move_rdata_state = move_rdata[idx].split(" ");
                white_move.add(move_rdata_state[0].trim());
                black_move.add(move_rdata_state[1].trim());
            }

            for(int idx = 0; idx< white_move.size(); ++idx) {

                //white_piece case
                check_table_mem.add(pgnAlgorithm(white_move.get(idx),0,black_bottom_opt)); //1,3,5... white piece moving control

                if(want_seeing_table_opt > 0)
                {
                    showTableValue(check_table_mem.size()-1);
                }

                //black_piece case
                if(idx < black_move.size())
                {
                    check_table_mem.add(pgnAlgorithm(black_move.get(idx),1,black_bottom_opt)); //2,4,6.. black piece moving control
                }

                if(want_seeing_table_opt > 0)
                {
                    showTableValue(check_table_mem.size()-1);
                }
            }
        }

        catch (Exception e)
        {
            System.out.println("only_parser error " + e.getMessage() );
        }
    }


    public static int[][] pgnAlgorithm(String move0, int weight0,int black_bottom_opt)
    {
        int piece_index = 0;
        int piece_value = 1 + weight0*6; //default piece is pawn
        int row_index = -1;
        int col_index = -1;
        boolean xcountrol = false;

        if(move0.equals("O-O"))
        {
            cur_chess_table[bottom_p - (7*weight0)][right_p-3] = 0; //king -> (-)
            cur_chess_table[bottom_p - (7*weight0)][right_p] = 0; //right rook -> (-)
            cur_chess_table[bottom_p - (7*weight0)][right_p-2] = piece_data.get("R") + weight0*6;
            cur_chess_table[bottom_p - (7*weight0)][right_p-1] = piece_data.get("K") + weight0*6;
        }

        else if(move0.equals("O-O-O"))
        {
            cur_chess_table[bottom_p - (7*weight0)][left_p+4] = 0; //king -> (-)
            cur_chess_table[bottom_p - (7*weight0)][left_p] = 0; //right rook -> (-)
            cur_chess_table[bottom_p - (7*weight0)][left_p+3] = piece_data.get("R") + weight0*6;
            cur_chess_table[bottom_p - (7*weight0)][left_p+2] = piece_data.get("K") + weight0*6;
        }


        else {
                try {
                    char cur_char = move0.charAt(piece_index);
                    char x_char = '@';
                    boolean pawn_state = true;

                    while (piece_index < move0.length()-2) { //control current string of chess piece

                        if (Character.isUpperCase(cur_char)) { //definition what piece
                            piece_value = piece_data.get("" + cur_char) + 6 * weight0;
                            pawn_state = false;
                        }

                        if (cur_char == '+') //check state, visual event#1,example
                        {

                        }
                        else if (cur_char == '#') //check mate state, visual event#2
                        {

                        }
                        else if (cur_char == 'x') // remove piece state
                        {
                            xcountrol = true;
                        }

                        else if(cur_char == '=' && (!pawn_state)) //promotion control
                        {
                            col_index = piece_data.get("" + move0.charAt(0));
                            row_index = piece_data.get("" + move0.charAt(1)); //charat(2) is '=', example h8=Q
                            piece_value = piece_data.get("" + move0.charAt(3))+ 6 * weight0;

                            setChessTable(piece_value,row_index,col_index); //cur_chess_table[row_index][col_index] = piece_value;
                            return cur_chess_table;
                        }

                        else{
                            x_char = cur_char;
                        }

                        ++piece_index;
                    }

                        //control piece's next position
                        cur_char = move0.charAt(piece_index); //get col_data from pgn
                        col_index = piece_data.get("" + cur_char);
                        if (col_index < 0 || col_index >= 8) {
                            logger.info("error in col_index " + col_index);
                            logger.info("cur char is " + cur_char);
                        }

                        cur_char = move0.charAt(++piece_index); //get row_data from pgn
                        row_index = piece_data.get("" + cur_char);
                        if (row_index < 0 || row_index >= 8) {
                            logger.info("error in col_index " + row_index);
                            logger.info("cur char is " + cur_char);
                        }

                        cur_chess_table[row_index][col_index] = piece_value;


                        //control piece's previous position
                        boolean previous_move = false;
                        if(piece_value == 1 || piece_value == 7) //pawn case control
                        {
                            if(xcountrol) //catch other piece
                            {
                                int[][] tmp_array = {{1,-1},{1,1}};
                                previous_move = calculatePreviousMove(tmp_array,row_index,col_index,piece_value,x_char);
                            }

                            else
                            {
                                int[][] tmp_array = {{1,0}};
                                previous_move = calculatePreviousMove(tmp_array,row_index,col_index,piece_value,x_char);
                            }
                        }

                        else if(piece_value == 2 || piece_value == 8) //night case control
                        {
                                int[][] tmp_array = {{2,1},{2,-1},{1,2},{1,-2},{-1,2},{-1,-2},{-2,1},{-2,-1}};
                                previous_move = calculatePreviousMove(tmp_array,row_index,col_index,piece_value,x_char);
                        }


                        else if(piece_value == 3 || piece_value == 9) //bishop case control
                        {
                            int[][] tmp_array = new int[28][2];
                            int[][] multiple = {{1,1},{1,-1},{-1,1},{-1,-1}};
                            for(int cnt = 1; cnt < board_size; ++cnt)
                            {
                               for(int cnt2 = 0; cnt2 < multiple.length; ++cnt2)
                               {
                                   int tmp_array_index = (cnt-1)*multiple.length + cnt2;
                                   tmp_array[tmp_array_index][0] = cnt * multiple[cnt2][0];
                                   tmp_array[tmp_array_index][1] = cnt * multiple[cnt2][1];
                               }

                            }

                            previous_move = calculatePreviousMove(tmp_array,row_index,col_index,piece_value,x_char);
                        }

                        else if(piece_value == 4 || piece_value == 10) //rook case control
                        {
                            int[][] tmp_array = new int[28][2];
                            int[][] multiple = {{1,0},{-1,0},{0,1},{0,-1}};
                            for(int cnt = 1; cnt < board_size; ++cnt)
                            {
                                for(int cnt2 = 0; cnt2 < multiple.length; ++cnt2)
                                {
                                    int tmp_array_index = (cnt-1)*multiple.length + cnt2;
                                    tmp_array[tmp_array_index][0] = cnt * multiple[cnt2][0];
                                    tmp_array[tmp_array_index][1] = cnt * multiple[cnt2][1];
                                }

                            }

                            previous_move = calculatePreviousMove(tmp_array,row_index,col_index,piece_value,x_char);
                        }

                        else if(piece_value == 5 || piece_value == 11) //queen case control
                        {
                            int[][] tmp_array = new int[56][2];
                            int[][] multiple = {{1,0},{-1,0},{0,1},{0,-1},{1,0},{-1,0},{0,1},{0,-1}};
                            for(int cnt = 1; cnt < board_size; ++cnt)
                            {
                                for(int cnt2 = 0; cnt2 < multiple.length; ++cnt2)
                                {
                                    int tmp_array_index = (cnt-1)*multiple.length + cnt2;
                                    tmp_array[tmp_array_index][0] = cnt * multiple[cnt2][0];
                                    tmp_array[tmp_array_index][1] = cnt * multiple[cnt2][1];
                                }

                            }

                            previous_move = calculatePreviousMove(tmp_array,row_index,col_index,piece_value,x_char);
                        }

                        else if(piece_value == 6 || piece_value == 12) //king case control
                        {
                            int[][] tmp_array = new int[8][2];
                            int[][] multiple = {{1,0},{-1,0},{0,1},{0,-1},{1,0},{-1,0},{0,1},{0,-1}};
                            for(int cnt = 1; cnt < 2; ++cnt)
                            {
                                for(int cnt2 = 0; cnt2 < multiple.length; ++cnt2)
                                {
                                    int tmp_array_index = (cnt-1)*multiple.length + cnt2;
                                    tmp_array[tmp_array_index][0] = cnt * multiple[cnt2][0];
                                    tmp_array[tmp_array_index][1] = cnt * multiple[cnt2][1];
                                }

                            }

                            previous_move = calculatePreviousMove(tmp_array,row_index,col_index,piece_value,x_char);
                        }


                        if(!previous_move)
                            logger.info("error in process " + move0);


                } catch (Exception e) {
                    System.out.println("pgn_algorithm error " + e.toString());
                }

            }


        if(black_bottom_opt > 0)// if you wnat to get black bottom position board, use option black_bottom_opt
        {
            int [][] rotate_array = new int[board_size][board_size];
            for(int row0 = top_p ; row0 <= bottom_p; ++row0)
            {
                for(int col0 = left_p ; col0 <= right_p; ++col0)
                {
                    rotate_array[row0][col0] = cur_chess_table[col0][row0];
                }
            }

            return  rotate_array;
        }

        else {
            return cur_chess_table;
        }
    }

    public  static  boolean calculatePreviousMove(int[][] tmp_array, int row_index, int col_index, int piece_value, char x_char)
    {
        try {
            int black_opt = 1;

            if(piece_value > 6) //black piece num :7 ~ 12
            {
                black_opt = -1;
            }

            for (int cnt = 0; cnt < tmp_array.length; ++cnt) {

                int bargain_row = tmp_array[cnt][0] * black_opt * (-1);
                int bargain_col = tmp_array[cnt][1] * (-1);
                int prv_row_index = row_index + bargain_row;
                int prv_col_index = col_index + bargain_col;

                int match_row_index = prv_row_index;
                int match_col_index = prv_col_index;

                if (Character.isAlphabetic(x_char)) {
                    prv_col_index = piece_data.get("" + x_char);
                } else if (Character.isDigit(x_char)) {
                    prv_row_index = piece_data.get("" + x_char);
                }

                if ((top_p <= prv_row_index) && (prv_row_index <= bottom_p)) {

                    if (cur_chess_table[prv_row_index][prv_col_index] == 0)
                    {
                        continue;
                    }

                   else if(cur_chess_table[prv_row_index][prv_col_index] == piece_value)
                   {
                       if((prv_row_index == match_row_index) && (prv_col_index == match_col_index))
                       {
                           setChessTable(piece_value,prv_row_index,prv_col_index); //cur_chess_table[prv_row_index][prv_col_index] = 0;
                           return true;
                       }
                   }
                }
            }
        }catch (Exception e) {
            System.out.println("calculatePreviousMove error " + e.toString());
        }

        return false;
    }


    public static void showTableValue(int where0)
    {
        for(int row0 = top_p ; row0 <= bottom_p; ++row0)
        {
            for(int col0 = left_p ; col0 <= right_p; ++col0)
            {
                System.out.print(String.format("%02d ",check_table_mem.get(where0)[row0][col0]));
            }

            System.out.println();
            System.out.println();
        }
    }

    public static void setChessTable(int piece_value, int row, int col)
    {
        cur_chess_table[board_size - row][col] = piece_value;
    }

    public static void main(String[] args){
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        onlyParser(pgn_example2,0,1);
    }
}