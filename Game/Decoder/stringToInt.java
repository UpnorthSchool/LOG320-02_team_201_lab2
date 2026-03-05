package Game.Decoder;

import Game.Move;

public class stringToInt {

    Move moveDecoded = new Move();
    int colonneDecode       = 0;
    int ligneDecode         = 0;
    public Move decode(String toDecode)
    {
        toDecode = removeWhitespace(toDecode);
        moveDecoded.setFromCol(decodeCol(toDecode.charAt(0)));
        moveDecoded.setFromRow(decodeLigne(toDecode.charAt(1)));
        moveDecoded.setTowardsCol(decodeCol(toDecode.charAt(3)));
        moveDecoded.setTowardsRow(decodeLigne(toDecode.charAt(4)));
        return moveDecoded;
    }



    public int decodeCol(char decoderColonne)
    {
        switch(decoderColonne)
                {
                    //colonne
                    // lire comme un chiffre binaire. 2exposant 2 = carr/e en bas a gauche,colonnec donc derniere colonne tictactoe
                    case 'a':
                        colonneDecode = 0;break;
                    case 'b':
                        colonneDecode = 1;break;
                    case 'c':
                        colonneDecode = 2;break;
                    case 'd':
                        colonneDecode = 3;break;
                    case 'e':
                        colonneDecode = 4;break;
                    case 'f':
                        colonneDecode = 5;break;
                    case 'g':
                        colonneDecode = 6;break;
                    case 'h':
                        colonneDecode = 7;break;
                    default:
                        return -1;
                }
        return colonneDecode;
    }


    public int decodeLigne(char decoderLigner)
    {
         switch (decoderLigner) {
            case '1':
                ligneDecode = 0;break;
            case '2':
                ligneDecode = 1;break;
            case '3':
                ligneDecode = 2;break;
            case '4':
                ligneDecode = 3;break;
            case '5':
                ligneDecode = 4;break;
            case '6':
                ligneDecode = 5;break;
            case '7':
                ligneDecode = 6;break;
            case '8':
                ligneDecode = 7;break;   
            default:
                return -1;
        }

        return ligneDecode;
    }

    //from stackoverflow removing whitespace
    // Source - https://stackoverflow.com/a/63270077
    // Posted by Bohemian
    // Retrieved 2026-03-05, License - CC BY-SA 4.0
    //added trim end and beginning and lowercase
    public static String removeWhitespace(String s) {
        return s.replaceAll("\\s", "").trim().toLowerCase(); // use regex
    }

    
}
