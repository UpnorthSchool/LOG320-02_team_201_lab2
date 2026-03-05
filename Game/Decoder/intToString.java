package Game.Decoder;
import Game.Move;
public class intToString {

    String moveReturn       = "";
    public String encode(Move encodeMove)
    {
        moveReturn = toString(encodeMove.getFromCol()) + (encodeMove.getFromRow()+1) + "-" + toString(encodeMove.getTowardsCol()) + (encodeMove.getTowardsRow()+1);
        return moveReturn;
    }


       public static String toString(int column) {
        switch (column) {
            case 0:
                return "A";
            case 1:
                return "B";
            case 2:
                return "C";
            case 3:
                return "D";
            case 4:
                return "E";
            case 5:
                return "F";
            case 6:
                return "G";
            case 7:
                return "H";
            default:
                return "?";
        }
    }



    
}
