/**
 * Created by kasptom on 28.03.16.
 */
public class ShipsCounter {
    private int xSize = 1;
    private int ySize = 1;
    private int[] counter;
    private int[] dx = {-1,0,1,0};
    private int [] dy = {0,-1,0,1};
    private int[] ax = {-1,1,1,-1};
    private int[] ay = {-1,-1,1,1};
    private char[][] board;

    public ShipsCounter(int xSize, int ySize, char[][] board){
        this.xSize = xSize;
        this.ySize = ySize;
        counter = new int[4];
        this.board = board;
    }

    public boolean countShips(){
        int count;
        boolean flagOK = false;
        counter = new int[4];
        for(int j=0; j<ySize; j++){
            for(int i=0; i<xSize; i++){
                if(board[j][i] == 's'){
                    board[j][i] = 'c';
                    count = checkNeighbours(i,j);
                    if(count >= 1 && count <=4)
                        counter[count-1]++;
                    else {
                        System.out.println("Size of at least one ship is too big!");
                    }
                    //System.out.print(" "+count);
                    count = 0;
                }
            }
        }
        if(counter[0] == 4 && counter[1] == 3 && counter[2] == 2 && counter[3] == 1){
            if(checkSeparation()){
                System.out.println("OK");
                flagOK = true;
            }else{
                System.out.println("Ships cannot stick to each other");
            }
        }
        else{
            System.out.println("You must have: ");
            System.out.println("| Ship size | required  |  on board   ");
            System.out.println("|    1      |   x4      |   "+ counter[0]);
            System.out.println("|    2      |   x3      |   "+ counter[1]);
            System.out.println("|    3      |   x2      |   "+ counter[2]);
            System.out.println("|    4      |   x1      |   "+ counter[3]);
        }
        System.out.println();
      //  printBoard();
        for(int j=0; j<ySize; j++)
            for(int i=0; i<xSize; i++)
                if(board[j][i] == 'c')
                    board[j][i] = 's';
        //return count;
        return flagOK;
    }
    private int checkNeighbours(int i, int j){
        int count = 1;
        for(int k=0; k<4; k++){
            if(i+dx[k] >= 0 && i+dx[k] < xSize && j+dy[k] >= 0 && j+dy[k] < ySize){
                if(board[j+dy[k]][i+dx[k]] == 's') {
                    board[j+dy[k]][i+dx[k]] = 'c';
                    count += checkNeighbours(i+dx[k], j+dy[k]);
                }
            }
        }
        return count;
    }

    private boolean checkSeparation(){
        for(int j=0; j<ySize-1; j ++){
            for(int i=0; i<xSize-1; i ++){
                if(board[j][i] == board[j+1][i+1]
                        && board[j][i] != board[j][i+1]
                        && board[j][i+1] == board[j+1][i])
                    return false;
            }
        }
        return true;
    }

    private void printBoard(){
        for(int j=0; j<ySize; j++) {
            for (int i = 0; i < xSize; i++)
                System.out.print(" "+board[j][i]);
            System.out.println();
        }
    }
}
