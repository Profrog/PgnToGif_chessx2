package io.github.profrog;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;


/**
 * it is module for pgn string data from chess.com/lichess.org convert to array table for java language
 * @author mingyu Kim(ache159@naver.com)
 */
public class PgnParse {
    //public static Logger logger = Logger.getLogger(PgnParse.class.getName());

    /**
     *  array table contain current piece position
     */
    public static int[][] cur_chess_table;


    /**
     *  it is array stack for cur_chess_table
     */
    public static  List<int[][]> check_table_mem;

    /**
     * it is map for matching chess's meaning with pgn's meaning
     */
    public static Map<String,String> pgn_state;

    /**
     * it is map for matching gif_cheesx2 table data with pgn's meaning
     */
    public static Map<String,Integer> piece_data;


    /**
     * it is pgn data stack for white piece movement
     */
    public static List<String> white_move;


    /**
     * it is pgn data stack for black piece movement
     */
    public static  List<String> black_move;


    /**
     * all pawn can move 2 block in first move, it is check that possibility
     */
    public static  StringBuilder[] pawn_first_move = new StringBuilder[2];

    /**
     * chess board size, in normally, chess board size is 8x8
     */
    public static final int board_size = 8;

    /**
     * it mean that is black pieces are placed in bottom of board?
     */
    public static int black_bottom_option = 0;

    /**
     * it contains char for showing chess board
     */
    public static String chess_piece_line = "_♙♘♗♖♕♔♟♞♝♜♛♚*";

    /**
     * left position of board
     */
    public static int left_p = 0; //left column basis index

    /**
     * right position of board
     */
    public static int right_p = board_size-1; //right column basis index

    /**
     * top position of board
     */
    public static int top_p = 0; //top row basis index

    /**
     * bottom position of board
     */
    public static int bottom_p = board_size -1; //bottom row basis index


    /**
     * check enpassant_state, only case 1 mean that enpassant enable
     */
    public static int enpassant_state = 0;

    /**
     * check where enpassant target
     */
    public static int enpassant_col = '@';


    /**
     * it is init method for controlling pgn data in gif_chessx2
     * @param pgn_data0 - pgn data which you get from get chess.com or lichess.org
     * @param black_bottom_opt - if that param is value 0 white piece locate in bottom in chess board, if that value is bigger than 0, black piece will locate in bottom side of chess board
     * @param want_seeing_table_opt - that option has data bigger than 0, this method will show cur board state following process in visually
     * example1 : PgnParse.parserInit(pgn_example,0,0); white piece is down position, black piece is up position, debugging mode off
     * example2 : PgnParse.parserInit(pgn_example,1,1); black piece is down position, white piece is up position, debugging mode on
     */
    public static List<int[][]> parserInit(String pgn_data0, int black_bottom_opt, int want_seeing_table_opt)
    {
        try {

            //print("data init");
            pgn_data0 = pgn_data0.replace('\n', ' ');
            check_table_mem = new ArrayList<>();
            pgn_state = new HashMap<String, String>();
            white_move = new ArrayList<>(); //list of white piece moving data
            black_move = new ArrayList<>(); //
            piece_data = new HashMap<>();
            pawn_first_move[0] = new StringBuilder("00000000"); //controlling possibility 2 blocking moving in first pawn moving
            pawn_first_move[1] = new StringBuilder("00000000");
            black_bottom_option = black_bottom_opt;

            cur_chess_table = getInitialBoard(); //current chesstable setting
            check_table_mem.add(blackBottomControl(black_bottom_opt)); //memory
            addMetadataToPiecedata();

            //print("showing init board state");
            if(want_seeing_table_opt > 0)
            {
                showTableValue(0);
            }

            //print("extract piece data from pgn format");
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

                if(raw_data1[raw_data1.length -1].charAt(idx) == '}') //controlling lichess format change to gif_chessx2 format
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

                //print("white_piece case control to table")
                check_table_mem.add(nxtMoveAlgorithm(white_move.get(idx),0,black_bottom_opt)); //1,3,5... white piece moving control

                if(want_seeing_table_opt > 0)
                {
                    showTableValue(check_table_mem.size()-1);
                }

                //print("black_piece case control to table")
                if(idx < black_move.size())
                {
                    check_table_mem.add(nxtMoveAlgorithm(black_move.get(idx),1,black_bottom_opt)); //2,4,6.. black piece moving control
                }

                if(want_seeing_table_opt > 0)
                {
                    showTableValue(check_table_mem.size()-1);
                }
            }

            System.out.println("finished PgnParser Process");
        }

        catch (Exception e)
        {
            print("parserInit error " + e.getMessage());
        }

        return check_table_mem;
    }


    /**
     * controlling piece position with newly moving following pgn data
     * @param move0 - where piece will go
     * @param isblack - it marks current piece is black(white piece has 1 ~ 6, black piece has 7 ~ 12)
     * @param black_bottom_opt - if that param is value 0 white piece locate in bottom in chess board, if that value is bigger than 0, black piece will locate in bottom side of chess board
     * @return current board state which interpret pgn logic
     */
    public static int[][] nxtMoveAlgorithm(String move0, int isblack,int black_bottom_opt)
    {
        //print("init nxtMoveAlgorithm");
        print(move0);
        int piece_index = 0;
        int piece_value = 1 + isblack*6; //default piece is pawn, 
        int row_index = -1; //row position piece will go
        int col_index = -1; //col position piece will go

        int board_label = board_size -1;
        int piece_label = board_size -2;


        --enpassant_state;

        if(move0.equals("O-O"))
        {
            //print("king side castling");
            cur_chess_table[bottom_p - (board_label*isblack)][right_p-3] = 0; //king -> (-)
            cur_chess_table[bottom_p - (board_label*isblack)][right_p] = 0; //right rook -> (-)
            cur_chess_table[bottom_p - (board_label*isblack)][right_p-2] = piece_data.get("R") + isblack*piece_label;
            cur_chess_table[bottom_p - (board_label*isblack)][right_p-1] = piece_data.get("K") + isblack*piece_label;
        }

        else if(move0.equals("O-O-O"))
        {
            //print("queen side castling");
            cur_chess_table[bottom_p - (board_label*isblack)][left_p+4] = 0; //king -> (-)
            cur_chess_table[bottom_p - (board_label*isblack)][left_p] = 0; //right rook -> (-)
            cur_chess_table[bottom_p - (board_label*isblack)][left_p+3] = piece_data.get("R") + isblack*piece_label;
            cur_chess_table[bottom_p - (board_label*isblack)][left_p+2] = piece_data.get("K") + isblack*piece_label;
        }


        else {
                try {
                    char cur_char = move0.charAt(piece_index);
                    char x_char = '@';
                    boolean x_countrol = false; //checking for catch state
                    boolean pawn_state = true;
                    boolean promotion_check = false;

                    int end_index = 2;
                    int back_index = move0.length() - 1;
                    char last_char = move0.charAt((back_index));

                    while(!Character.isDigit(last_char)) { //check piece state in back statement
                        if (last_char == '+')
                        {
                            //print("check state");
                        }
                        else if (last_char == '#') {
                            //print("check mate state");
                        }

                        else if(last_char == '=')
                        {
                            //print("promotion");
                            promotion_check = true;
                        }

                        else if(Character.isUpperCase(last_char))
                        {
                            //print("check which piece for promotion");
                            piece_value = piece_data.get("" + last_char) + piece_label * isblack;
                        }

                        last_char = move0.charAt((--back_index));
                        ++end_index;
                    }


                    while (piece_index < move0.length()-end_index) { //control current position of chess piece

                        if (Character.isUpperCase(cur_char)) { //definition what piece type, default is pawn
                            piece_value = piece_data.get("" + cur_char) + piece_label * isblack;
                            pawn_state = false;
                        }

                        else if (cur_char == 'x')
                        {
                            //print("remove piece state");
                            x_countrol = true;
                        }

                        else{
                            //print("set position for piece move where");
                            x_char = cur_char;
                            //case 1 : gxf6 -> saving g
                            //case 2 : Rfd6 -> saving f
                            //case 3 : fd6 -> sabing f
                        }

                        cur_char = move0.charAt(++piece_index);
                    }


                    //print(control piece's next  col position);
                    cur_char = move0.charAt(piece_index); //get col_data from pgn
                    col_index = piece_data.get("" + cur_char);
                    if (col_index < 0 || col_index >= board_size) {
                        print("error in col_index " + col_index);
                        print("cur char is " + cur_char);
                    }

                    //print(control piece's next  row position);
                    cur_char = move0.charAt(++piece_index); //get row_data from pgn
                    row_index = piece_data.get("" + cur_char);

                    if (row_index < 0 || row_index >= board_size) {
                        print("error in col_index " + row_index);
                        print("cur char is " + cur_char);
                    }
                    
                    setChessTable(piece_value,getRealRow(row_index),col_index);

                    
                    //print(control piece's previous position);
                    boolean previous_move = false;
                    if(piece_value == 1 || piece_value == 7 || promotion_check)
                    {
                        //print("pawn & promotion case control");
                        if(promotion_check)
                        {
                            piece_value = 1 + piece_label*isblack;
                        }

                        if(x_countrol) //catch state for other piece
                        {
                            int[][] prv_pos_array = {{1,-1},{1,1}}; //{0,-1},{0,1} for control enpassnt contorl
                            previous_move = prvMoveAlgorithm(prv_pos_array,row_index,col_index,piece_value,x_char,x_countrol);
                        }

                        else
                        {
                            int[][] prv_pos_array = new int[2][2];
                            prv_pos_array[0][0] = 1;
                            prv_pos_array[0][1] = 0;
                            prv_pos_array[1][0] = 1; //for consideration first moving
                            prv_pos_array[1][1] = 0;

                            if(pawn_first_move[piece_value/7].charAt(col_index) == '0') //check first pawn moving
                            {
                                prv_pos_array[1][0] = 2;
                                pawn_first_move[piece_value/7].setCharAt(col_index, '1');
                                enpassant_state = 2; //it means that next moving has case of enpassant
                                enpassant_col = col_index;
                            }

                            previous_move = prvMoveAlgorithm(prv_pos_array,row_index,col_index,piece_value,x_char,x_countrol);
                        }
                    }

                    else if(piece_value == 2 || piece_value == 8)
                    {
                            //print("night case control");
                            int[][] prv_pos_array = {{2,1},{2,-1},{1,2},{1,-2},{-1,2},{-1,-2},{-2,1},{-2,-1}};
                            previous_move = prvMoveAlgorithm(prv_pos_array,row_index,col_index,piece_value,x_char,x_countrol);
                    }


                    else if(piece_value == 3 || piece_value == 9)
                    {
                        //print("bishop case control");
                        int[][] prv_pos_array = new int[28][2];
                        int[][] multiple = {{1,1},{1,-1},{-1,1},{-1,-1}};
                        for(int cnt = 1; cnt < board_size; ++cnt)
                        {
                           for(int cnt2 = 0; cnt2 < multiple.length; ++cnt2)
                           {
                               int prv_pos_array_index = (cnt-1)*multiple.length + cnt2;
                               prv_pos_array[prv_pos_array_index][0] = cnt * multiple[cnt2][0];
                               prv_pos_array[prv_pos_array_index][1] = cnt * multiple[cnt2][1];
                           }

                        }

                        previous_move = prvMoveAlgorithm(prv_pos_array,row_index,col_index,piece_value,x_char,x_countrol);
                    }

                    else if(piece_value == 4 || piece_value == 10)
                    {
                        //print("rook case control");
                        int[][] prv_pos_array = new int[28][2];
                        int[][] multiple = {{1,0},{-1,0},{0,1},{0,-1}};
                        for(int cnt = 1; cnt < board_size; ++cnt)
                        {
                            for(int cnt2 = 0; cnt2 < multiple.length; ++cnt2)
                            {
                                int prv_pos_array_index = (cnt-1)*multiple.length + cnt2;
                                prv_pos_array[prv_pos_array_index][0] = cnt * multiple[cnt2][0];
                                prv_pos_array[prv_pos_array_index][1] = cnt * multiple[cnt2][1];
                            }

                        }

                        previous_move = prvMoveAlgorithm(prv_pos_array,row_index,col_index,piece_value,x_char,x_countrol);
                    }

                    else if(piece_value == 5 || piece_value == 11)
                    {
                        //print("queen case control");
                        int[][] prv_pos_array = new int[56][2];
                        int[][] multiple = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
                        for(int cnt = 1; cnt < board_size; ++cnt)
                        {
                            for(int cnt2 = 0; cnt2 < multiple.length; ++cnt2)
                            {
                                int prv_pos_array_index = (cnt-1)*multiple.length + cnt2;
                                prv_pos_array[prv_pos_array_index][0] = cnt * multiple[cnt2][0];
                                prv_pos_array[prv_pos_array_index][1] = cnt * multiple[cnt2][1];
                            }

                        }

                        previous_move = prvMoveAlgorithm(prv_pos_array,row_index,col_index,piece_value,x_char,x_countrol);
                    }

                    else if(piece_value == 6 || piece_value == 12)
                    {
                        //print("king case control");

                        int[][] prv_pos_array = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
                        previous_move = prvMoveAlgorithm(prv_pos_array,row_index,col_index,piece_value,x_char,x_countrol);
                    }


                } catch (Exception e) {
                    System.out.println("pgn_algorithm error " + e.toString());
                }

            }

        return blackBottomControl(black_bottom_opt);
    }


    /**
     * following nxtMoveAlgorithm piece move other position, and then piece data previous position need to be deleted, this method controls it
     * @param prv_pos_array - it is array data which contain possibility movement about target piece
     * @param row_index - target piece's new row index following nxtMoveAlgorithm
     * @param col_index - target piece's new col index following nxtMoveAlgorithm
     * @param piece_value - gets piece value data from nxtMoveAlgorithm (for more detail follow addMetadataToPiecedata())
     * @param x_char - option index for control
     * @param x_control - option true : catch enemy, false : controly my piece
     * @return true/false -: is this method's successful?
     */
    public  static  boolean prvMoveAlgorithm(int[][] prv_pos_array, int row_index, int col_index, int piece_value, char x_char, boolean x_control)
    {
        try {
            int black_opt = 1;

            if(piece_value > 6) //black piece num :7 ~ 12, board arrow
            {
                black_opt = -1;
            }

            for (int cnt = 0; cnt < prv_pos_array.length; ++cnt) {

                int bargain_row = prv_pos_array[cnt][0] * black_opt;
                int bargain_col = prv_pos_array[cnt][1] * black_opt;
                int prv_row_index = row_index - bargain_row;
                int prv_col_index = col_index - bargain_col;

                int match_row_index = prv_row_index;
                int match_col_index = prv_col_index;

                if (Character.isAlphabetic(x_char)) { //check optional moving
                    match_col_index = piece_data.get("" + x_char);
                } else if (Character.isDigit(x_char)) {
                    match_row_index = piece_data.get("" + x_char);
                }

                //print(String.valueOf(piece_value) + " " +String.valueOf(prv_row_index) + " " + String.valueOf(prv_col_index));
                //print(String.valueOf(piece_value) + " " +String.valueOf(match_row_index) + " " + String.valueOf(match_col_index));
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

               else if(x_char == '@' && cur_chess_table[getRealRow(prv_row_index)][prv_col_index] == piece_value)
               {
                   //print("no option state about special moving");
                   setChessTable(0, getRealRow(prv_row_index), prv_col_index); //cur_chess_table[prv_row_index][prv_col_index] = 0;
                   return true;
               }

               else if(cur_chess_table[getRealRow(prv_row_index)][prv_col_index] == piece_value)
               {
                   //print("option state about special moving");
                   if((prv_row_index == match_row_index) && (prv_col_index == match_col_index))
                   {
                       if(enpassant_state == 1 && (enpassant_col == prv_col_index) &&(piece_value == 1 || piece_value == 7)) //enpassant control
                       {
                           //print("enpassant_control");
                           if(piece_value == 8 - cur_chess_table[getRealRow(row_index - black_opt)][col_index]) {
                               setChessTable(0, getRealRow(row_index - black_opt), col_index);
                           }
                       }

                       setChessTable(0, getRealRow(prv_row_index), prv_col_index); //cur_chess_table[prv_row_index][prv_col_index] = 0;
                       return true;
                   }
               }

               else
               {
                    //print("abnormal state");
               }

            }
        }catch (Exception e) {
            print("prvMoveAlgorithm error " + e.toString());
        }

        return false;
    }

    /**
     * show nth chess board table basis on pgn data
     * @param where0 - (where0 -: n) th
     */
    public static void showTableValue(int where0)
    {
        if (where0 > 0) {
            for (int row0 = top_p; row0 <= bottom_p; ++row0) {
                for (int col0 = left_p; col0 <= right_p; ++col0) {
                    System.out.print(chess_piece_line.charAt(check_table_mem.get(where0)[row0][col0]));
                }
                print("");
            }
            print("");
        }

        else {
            int[][] present_table = blackBottomControl(black_bottom_option);
            for (int row0 = top_p; row0 <= bottom_p; ++row0) {
                for (int col0 = left_p; col0 <= right_p; ++col0) {
                    //System.out.print(String.format("%d ",check_table_mem.get(where0)[row0][col0]));
                    System.out.print(chess_piece_line.charAt(present_table[row0][col0]));
                }
                print("");
            }
            print("");
        }
    }


    /**
     * it is method for debugging detail coordinate following input {row, col}
     * @param row - row index which want to debug position
     * @param col - column index which want to debug position
     */
    public static void debug_piece(int row, int col) //show * piece where i want
    {
        int tmp = cur_chess_table[getRealRow(row)][col];
        setChessTable(13,getRealRow(row),col);
        showTableValue(-1);
        setChessTable(tmp,getRealRow(row),col);
    }

    /**
     * in array system top index is 0, bottom index is n(it is root structure), but in natural view for human, it is more easily top index is n and bottom index is 0, that method solve that problem
     * @param row - row index for changing from natural view to real view for computer
     * @return
     */
    public static int getRealRow(int row)
    {
        return board_size -1 - row;
    }


    /**
     * setting piece data to board
     * @param piece_value - gets piece value data from nxtMoveAlgorithm (for more detail follow addMetadataToPiecedata())
     * @param row - row index of piece which want to change piece value
     * @param col - column index of piece which want to change piece value
     */
    public static void setChessTable(int piece_value, int row, int col)
    {
        cur_chess_table[row][col] = piece_value;
    }


    /**
     * initailing chessboard's original state
     * @return chess board's initial table
     */
    public static int[][] getInitialBoard()
    {
        //print("getInitialBoard init");
       return new int[][]{ //definition first state of board, white piece 1~6, black piece 1~6
                {10,8,9,11,12,9,8,10},
                {7,7,7,7,7,7,7,7},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {1,1,1,1,1,1,1,1},
                {4,2,3,5,6,3,2,4},
        };
    }

    /**
     * matching protocol about chess's meaning with pgn meaning
     */
    public static void addMetadataToPiecedata()
    {
        //print("piece meta data is added");
        piece_data.put("P",1); //white pawn -> 1, black pawn -> 7
        piece_data.put("N",2); //white night -> 2, black night -> 8
        piece_data.put("B",3); //white bishop -> 3, black bishop -> 9
        piece_data.put("R",4); //white rook  -> 4, black rook -> 10
        piece_data.put("Q",5); //white queen -> 5, black queen -> 11
        piece_data.put("K",6); //white king -> 6, black king -> 12

        piece_data.put("8",7); //board row 8
        piece_data.put("7",6); //board row 7
        piece_data.put("6",5); //board row 6
        piece_data.put("5",4); //board row 5
        piece_data.put("4",3); //board row 4
        piece_data.put("3",2); //board row 3
        piece_data.put("2",1); //board row 2
        piece_data.put("1",0); //board row 1

        piece_data.put("h",7); //board col 8
        piece_data.put("g",6); //board col 7
        piece_data.put("f",5); //board col 6
        piece_data.put("e",4); //board col 5
        piece_data.put("d",3); //board col 4
        piece_data.put("c",2); //board col 3
        piece_data.put("b",1); //board col 2
        piece_data.put("a",0); //board col 1
    }

    /**
     * it rotate chess board table, when user want to black pieces go to bottom in board(in default white pieces are placed in bottom)
     * @param black_bottom_opt - if that param is value 0 white piece locate in bottom in chess board, if that value is bigger than 0, black piece will locate in bottom side of chess board
     * @return
     */
    public static int[][] blackBottomControl(int black_bottom_opt)
    {
        int [][] rotate_array = new int[board_size][board_size];
        if(black_bottom_opt > 0)// if you want to get black bottom position board, use option black_bottom_opt
        {
            for(int row0 = 0 ; row0 < board_size; ++row0)
            {
                for(int col0 = 0 ; col0 < board_size; ++col0)
                {
                    rotate_array[board_size - 1 - col0][board_size - 1 - row0] = cur_chess_table[col0][row0];
                }
            }
        }

        else
        {
            for(int row0 = 0 ; row0 < board_size; ++row0)
            {
                for(int col0 = 0 ; col0 < board_size; ++col0)
                {
                    rotate_array[col0][row0] = cur_chess_table[col0][row0];
                }
            }
        }

        return rotate_array;
    }


    /**
     * change piece skin for customizing
     * @param skin - skin data
     */
    public static void setChessSkin(String skin)
    {
        chess_piece_line = skin;
    }

    /**
     * it is method for using System.out.println more easily
     * @param data - string data for System.out.println
     */
    public static void print(String data)
    {
        System.out.println(data);
    }
}