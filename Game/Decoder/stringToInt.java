package Game.Decoder;
public class stringToInt {

    int moveDecoded         = 0;
    int colonneDecode       = 0;
    int ligneDecode         = 0;
    public int decode(String toDecode)
    {
       
        return moveDecoded;
    }



    public int decodeCol(String decoderColonne)
    {
        switch(decoderColonne.charAt(0))
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


    public int decodeLigne(String decoderLigner)
    {
         switch (decoderLigner.charAt(1)) {
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
    
}
