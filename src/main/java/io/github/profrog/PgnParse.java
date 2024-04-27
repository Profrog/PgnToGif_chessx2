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

    public static  StringBuilder[] pawn_first_move = new StringBuilder[2];

    public static final int board_size = 8; //in normally, chess board size is 8x8
    public static int left_p = 0; //left column basis index
    public static int right_p = board_size-1; //right column basis index
    public static int top_p = 0; //top row basis index
    public static int bottom_p = board_size -1; //bottom row basis index

    public static List<int[][]> move_condition;

    public static String chess_piece_line = "_♟♞♝♜♛♚♙♘♗♖♕♔*";


    //void parser_init(Image[] chess_img0, Color boardline_lout0, String pgn_data0)

    public PgnParse()
    {
        System.out.println("parser init");
    }

    public static void onlyParser(String pgn_data0, int black_bottom_opt, int want_seeing_table_opt)
    {
        try {


            check_table_mem = new ArrayList<>();
            pgn_state = new HashMap<String, String>();
            white_move = new ArrayList<>();
            black_move = new ArrayList<>();
            piece_data = new HashMap<>();
            pawn_first_move[0] = new StringBuilder("00000000");
            pawn_first_move[1] = new StringBuilder("00000000");


            cur_chess_table = new int[][]{ //definition first state of board, white piece 1~6, black piece 1~6
                    {10,8,9,11,12,9,8,10},
                    {7,7,7,7,7,7,7,7},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {1,1,1,1,1,1,1,1},
                    {4,2,3,5,6,3,2,4},
            };

            check_table_mem.add(cur_chess_table);

            if(want_seeing_table_opt > 0)
            {
                showTableValue(check_table_mem.size()-1);
            }


            piece_data.put("P",1); //piece meta data is added
            piece_data.put("N",2);
            piece_data.put("B",3);
            piece_data.put("R",4);
            piece_data.put("Q",5);
            piece_data.put("K",6);

            piece_data.put("8",7);
            piece_data.put("7",6);
            piece_data.put("6",5);
            piece_data.put("5",4);
            piece_data.put("4",3);
            piece_data.put("3",2);
            piece_data.put("2",1);
            piece_data.put("1",0);

            piece_data.put("h",7);
            piece_data.put("g",6);
            piece_data.put("f",5);
            piece_data.put("e",4);
            piece_data.put("d",3);
            piece_data.put("c",2);
            piece_data.put("b",1);
            piece_data.put("a",0);


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

                    int end_index = 2;
                    char last_char = move0.charAt((move0.length()-1));

                    if (last_char == '+')
                    {
                        //print("check state");
                        ++end_index;
                    }
                    else if (last_char == '#')
                    {
                        //print("check mate state");
                        ++end_index;
                    }

                    while (piece_index < move0.length()-end_index) { //control current string of chess piece

                        if (Character.isUpperCase(cur_char)) { //definition what piece
                            piece_value = piece_data.get("" + cur_char) + 6 * weight0;
                            pawn_state = false;
                        }

                        else if (cur_char == 'x')
                        {
                            print("remove piece state");
                            xcountrol = true;
                        }

                        else if(cur_char == '=' && (!pawn_state))
                        {
                            //print("promotion control state");
                            col_index = piece_data.get("" + move0.charAt(0));
                            row_index = piece_data.get("" + move0.charAt(1)); //charat(2) is '=', example h8=Q
                            piece_value = piece_data.get("" + move0.charAt(3))+ 6 * weight0;

                            setChessTable(piece_value,getRealRow(row_index),col_index); //cur_chess_table[row_index][col_index] = piece_value;
                            return cur_chess_table;
                        }

                        else{
                            print("set position for remove piece state");
                            x_char = cur_char;
                        }

                        cur_char = move0.charAt(++piece_index);
                    }


                    //control piece's next position
                    cur_char = move0.charAt(piece_index); //get col_data from pgn
                    col_index = piece_data.get("" + cur_char);
                    if (col_index < 0 || col_index >= 8) {
                        System.out.println("error in col_index " + col_index);
                        System.out.println("cur char is " + cur_char);
                    }

                    ++piece_index;
                    cur_char = move0.charAt(piece_index); //get row_data from pgn
                    row_index = piece_data.get("" + cur_char);

                    if (row_index < 0 || row_index >= 8) {
                        System.out.println("error in col_index " + row_index);
                        System.out.println("cur char is " + cur_char);
                    }

                    //System.out.println(String.valueOf(row_index) + " " + String.valueOf(col_index));
                    setChessTable(piece_value,getRealRow(row_index),col_index);

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
                            int[][] tmp_array = new int[2][2];
                            tmp_array[0][0] = 1;
                            tmp_array[0][1] = 0;
                            tmp_array[1][0] = 1;
                            tmp_array[1][1] = 0;

                            if(pawn_first_move[piece_value/7].charAt(col_index) == '0')
                            {
                                tmp_array[1][0] = 2;
                                pawn_first_move[piece_value/7].setCharAt(col_index, '1');
                            }

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
                        int[][] multiple = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
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
                        int[][] multiple = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
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


                    //if(!previous_move)
                        //System.out.println("error in process " + move0);


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

                int bargain_row = tmp_array[cnt][0] * black_opt;
                int bargain_col = tmp_array[cnt][1] * black_opt;
                int prv_row_index = row_index - bargain_row;
                int prv_col_index = col_index - bargain_col;

                int match_row_index = prv_row_index;
                int match_col_index = prv_col_index;

                if (Character.isAlphabetic(x_char)) {
                    match_col_index = piece_data.get("" + x_char);
                } else if (Character.isDigit(x_char)) {
                    match_row_index = piece_data.get("" + x_char);
                }

                //System.out.println(String.valueOf(piece_value) + " " +String.valueOf(bargain_row) + " " + String.valueOf(bargain_col));
                System.out.println(String.valueOf(piece_value) + " " +String.valueOf(prv_row_index) + " " + String.valueOf(prv_col_index));
                System.out.println(String.valueOf(piece_value) + " " +String.valueOf(match_row_index) + " " + String.valueOf(match_col_index));


                if(piece_value == 11 || piece_value == 11) {
                    //debug_piece(prv_row_index, prv_col_index);
                }


                //setChessTable(piece_value,prv_row_index,prv_col_index);
                //showTableValue(-1);

                if(prv_row_index < 0 || prv_row_index >= board_size)
                {
                    //print("prv_row size is starange " + String.valueOf(prv_row_index));
                }

                else if(prv_col_index < 0 || prv_col_index >= board_size)
                {
                    //print("prv_col size is starange " + String.valueOf(prv_col_index));
                }


                else if (cur_chess_table[getRealRow(prv_row_index)][prv_col_index] == 0)
                {
                    //print("there are no piece ");
                }

               else if(cur_chess_table[getRealRow(prv_row_index)][prv_col_index] == piece_value)
               {
                   //setChessTable(0,prv_row_index,prv_col_index);
                   //showTableValue(-1);

                   if((prv_row_index == match_row_index) && (prv_col_index == match_col_index))
                   {
                       setChessTable(0, getRealRow(prv_row_index),prv_col_index); //cur_chess_table[prv_row_index][prv_col_index] = 0;
                       //showTableValue(-1);
                       return true;
                   }
               }

               else
               {
                    //System.out.println("piece_value " + cur_chess_table[prv_row_index][prv_col_index]);
                   //setChessTable(13,prv_row_index,prv_col_index);
                   //showTableValue(-1);

               }

            }
        }catch (Exception e) {
            print("calculatePreviousMove error " + e.toString());
        }

        return false;
    }


    public static void showTableValue(int where0)
    {
        if (where0 >= 0) {
            for (int row0 = top_p; row0 <= bottom_p; ++row0) {
                for (int col0 = left_p; col0 <= right_p; ++col0) {
                    //System.out.print(String.format("%d ",check_table_mem.get(where0)[row0][col0]));
                    System.out.print(chess_piece_line.charAt(check_table_mem.get(where0)[row0][col0]));
                }
                System.out.println();
            }
            System.out.println();
        }

        else {
            for (int row0 = top_p; row0 <= bottom_p; ++row0) {
                for (int col0 = left_p; col0 <= right_p; ++col0) {
                    //System.out.print(String.format("%d ",check_table_mem.get(where0)[row0][col0]));
                    System.out.print(chess_piece_line.charAt(cur_chess_table[row0][col0]));
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    public static void debug_piece(int row, int col)
    {
        int tmp = cur_chess_table[getRealRow(row)][col];
        setChessTable(13,getRealRow(row),col);
        showTableValue(-1);
        setChessTable(tmp,getRealRow(row),col);
    }

    public static int getRealRow(int row)
    {
        return board_size -1 - row;
    }

    public static void setChessTable(int piece_value, int row, int col)
    {
        cur_chess_table[row][col] = piece_value;
    }

    public static void print(String data)
    {
        System.out.println(data);
    }
}