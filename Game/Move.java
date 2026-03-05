package Game;


public class Move
{
    private int towardsRow;
    private int towardsCol;
    private int fromRow;
    private int fromCol;
    private Mark captured;

    public Move(){
        towardsRow              = -1;
        towardsCol              = -1;
        fromRow                 = -1;
        fromCol                 = -1;
        captured = Mark.EMPTY;
    }

    public Move(int fXcol, int fYRow, int toXcol, int toYrow )
    {
        fromRow         = fYRow;
        fromCol         = fXcol;
        towardsRow      = toYrow;
        towardsCol      = toXcol;
        captured = Mark.EMPTY;
    }

    public Move(int xCol, int yLig){
        towardsRow = yLig;
        towardsCol = xCol;
    }

    public int getFromRow(){
        return fromRow;
    }

    public int getFromCol(){
        return fromCol;
    }

    public void setFromRow(int r){
        fromRow = r;
    }

    public void setFromCol(int c){
        fromCol = c;
    }


    public int getTowardsRow(){
        return towardsRow;
    }

    public int getTowardsCol(){
        return towardsCol;
    }

    public void setTowardsRow(int r){
        towardsRow = r;
    }

    public void setTowardsCol(int c){
        towardsCol = c;
    }

    public void setCaptured(Mark m) {
    captured = m;
    }

    public Mark getCaptured() {
        return captured;
    }

    //verification si case capturer
    public boolean isCapture() 
    {
        return captured != Mark.EMPTY;
    }


    @Override
    public String toString()
    {
        return (getTowardsCol() + "" + getTowardsRow());
    }
}
