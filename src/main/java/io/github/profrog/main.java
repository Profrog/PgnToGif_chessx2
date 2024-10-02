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

    public  static  String pgn_example = "[Event \"Let's Play!\"]\n" +
            "[Site \"Chess.com\"]\n" +
            "[Date \"2024-09-17\"]\n" +
            "[White \"jadenlee0724\"]\n" +
            "[Black \"alexPark0609\"]\n" +
            "[Result \"1-0\"]\n" +
            "[WhiteElo \"783\"]\n" +
            "[BlackElo \"1003\"]\n" +
            "[TimeControl \"1/172800\"]\n" +
            "[EndDate \"2024-09-21\"]\n" +
            "[Termination \"jadenlee0724님이 체크메이트로 승리하였습니다\"]\n" +
            "1. d4 Nf6 2. Nc3 d5 3. Bg5 e6 4. Nf3 h6 5. Bxf6 Qxf6 6. e4 c6 7. exd5 cxd5 8.\n" +
            "Bb5+ Nd7 9. O-O a6 10. Bxd7+ Bxd7 11. Re1 Bd6 12. Nxd5 Qd8 13. Ne5 Bxe5 14. Rxe5\n" +
            "O-O 15. Nc3 Qb6 16. Rb1 Bc6 17. d5 Rfd8 18. Qf3 Bd7 19. dxe6 Bxe6 20. Nd5 Qd6\n" +
            "21. Rxe6 fxe6 22. Nc3 b5 23. Ne4 Qd5 24. g3 Qxa2 25. Nc3 Qa5 26. b4 Qb6 27. Ne4\n" +
            "Qc6 28. Nf6+ gxf6 29. Qxc6 Kf7 30. Qf3 Rac8 31. Qh5+ Kg7 32. Qe2 e5 33. Ra1 Rc6\n" +
            "34. Qe4 Rdd6 35. Rc1 Kf7 36. c4 bxc4 37. Rxc4 Rb6 38. Qh7+ Ke6 39. Qg8+ Kf5 40.\n" +
            "Qg4# 1-0";

    public static void main(String[] args) throws IOException {
        String dir0 = "/home/mingyu/Pictures/chess";
        List<int[][]> alpa =  PgnParse.parserInit(pgn_example,0,1);
        String input_dir = PgnToImage.imageInit(alpa,dir0,null);
        ImageToGif.gifInit(dir0 + "/test.gif", input_dir, 1000);
    }
}
