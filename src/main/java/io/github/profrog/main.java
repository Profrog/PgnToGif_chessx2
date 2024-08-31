package io.github.profrog;
import io.github.profrog.PgnParse;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * example for using PgnParse
 */
public class main {

    public  static  String pgn_example = "[Event \"Live Chess\"]\n" +
            "[Site \"Chess.com\"]\n" +
            "[Date \"2024.05.09\"]\n" +
            "[Round \"?\"]\n" +
            "[White \"Fluffy7Bunny\"]\n" +
            "[Black \"PFchessX2\"]\n" +
            "[Result \"1-0\"]\n" +
            "[ECO \"D06\"]\n" +
            "[WhiteElo \"1489\"]\n" +
            "[BlackElo \"1418\"]\n" +
            "[TimeControl \"300+5\"]\n" +
            "[EndTime \"5:51:21 PDT\"]\n" +
            "[Termination \"Fluffy7Bunny won by resignation\"]\n" +
            "\n" +
            "1. d4 d5 2. c4 Nf6 3. cxd5 Nxd5 4. e4 Nb6 5. Nf3 Bg4 6. Be2 e6 7. O-O Be7 8. Nc3\n" +
            "O-O 9. Bf4 Nc6 10. Be3 Bxf3 11. Bxf3 Nc4 12. b3 Nxe3 13. fxe3 Bb4 14. Rc1 Qg5\n" +
            "15. Qe2 Rfd8 16. Nb5 Rac8 17. e5 a6 18. Bxc6 bxc6 19. Na7 Ra8 20. Nxc6 Ba3 21.\n" +
            "Nxd8 Bxc1 22. Rxc1 Rxd8 23. Rxc7 h6 24. Qf3 Rd5 25. Rxf7 Qg6 26. Rf8+ Kh7 27.\n" +
            "Qf1 Qe4 28. Rf3 Rd7 29. Qxa6 Rc7 30. Qxe6 Rc1+ 31. Kf2 Qh4+ 32. Rg3 Rc2+ 33. Kf3\n" +
            "Qh5+ 34. Qg4 Qf7+ 35. Ke4 Qb7+ 36. Kd3 Rxa2 37. Qe4+ 1-0";

    public static void main(String[] args) throws IOException {
        String dir0 = "/home/mingyu/Pictures/chess";
        List<int[][]> alpa =  PgnParse.parserInit(pgn_example,0,0);
        String input_dir = PgnToImage.imageInit(alpa,dir0,null);

        ImageToGif.gifInit(dir0 + "/test.gif", input_dir, 1000);
    }
}
