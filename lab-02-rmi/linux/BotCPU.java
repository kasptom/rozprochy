import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BotCPU extends Thread{
    private INoteBoard nb;
    private IUser u;
    private char[][] board = {
            {'w','w','w','w','w','w','w','w','w','w'},
            {'w','s','w','w','w','s','w','s','s','w'},
            {'w','s','s','w','w','w','w','w','s','w'},
            {'w','w','s','w','w','s','w','w','w','w'},
            {'w','w','w','w','w','w','w','w','w','w'},
            {'w','s','s','w','w','w','s','w','w','w'},
            {'w','w','w','w','w','w','w','w','s','w'},
            {'w','s','s','w','w','w','s','w','s','w'},
            {'w','w','w','w','s','w','w','w','w','w'},
            {'w','w','w','s','s','w','w','w','w','w'}
    };
    private int[] dx = {-1,0,1,0};
    private int [] dy = {0,-1,0,1};
    private int xSize = 10;
    private int ySize = 10;
    private int pointsLeft = 20;
    private boolean won = false;
    private List<Point> shots;
    private Random randomGenerator = new Random();

    private MyBoard myBoard = new MyBoard();
    private INoteBoardListener nbl;
    private static String boardStr;
    private boolean myTurn = false;

    public BotCPU(INoteBoardListener l){
        myBoard.board = board;
        nbl = l;
        shots = new ArrayList<Point>();
        for(int j=0; j<ySize; j++){
            for(int i=0; i<xSize; i++){
                shots.add(new Point(i,j));
            }
        }
    }

    public void run() {
        while(pointsLeft > 0 || !won){
            try {
                Thread.sleep(100);
                if (isMyTurn()) {
                    botShot();
                }
            }catch(Exception e){}

        }
        return;
    }

    public void onNewText(String text){
        System.out.println(text);
    }

    public char onOpponentsShot(int x, int y){
        myTurn = true;
        if(myBoard.board[y][x] == 's') {
            pointsLeft = pointsLeft - 1;
            myBoard.board[y][x] = 'd';
            boolean destr = checkDestroyed(x,y);
            //cleare board after checking
            for(int j=0; j<ySize; j++){
                for(int i=0; i<xSize; i++) {
                    if (myBoard.board[j][i] == 'c') {
                        if(destr)
                            myBoard.board[j][i] = 'x';  //destroyed
                        else
                            myBoard.board[j][i] = 'd';  //down
                    }
                }
            }
        }
        //System.out.println("outside board["+y+"]"+"["+x+"] = "+myBoard.board[y][x]);
        if(pointsLeft == 0) {
            System.out.println("You've lost!");
            return 'l';
        }
        return myBoard.board[y][x]; /* return opponent his shot result */
    }

    public void botShot() throws RemoteException{
        myTurn = false;
        char field;

        int index = randomGenerator.nextInt(shots.size());
        Point p = shots.remove(index);
        field = nbl.onOpponentsShot(p.x,p.y);
        //if you change the UI, do it here !
        /*if(field == 'd');
            //buttons2[y][x].setGraphic(new ImageView(imageDown));
        if(field == 'w');
            //buttons2[y][x].setGraphic(new ImageView(imageMiss));
        if(field == '?') {
            //System.out.println("Wait for your opponent");
            myTurn = true;
        }
        if(field == 'x') //ship destroyed
            //buttons2[y][x].setGraphic(new ImageView(imageDestr));*/
        if(field == 'l') {
            //buttons2[y][x].setGraphic(new ImageView(imageDestr));
            System.out.println("You've won!");
            won = true;
        }
    }

    public boolean isMyTurn(){
        return myTurn;
    }

    public void printBoard(){
        System.out.println("SPRAWDZAM");
        for(int i=0; i<10; i++) {
            for (int j = 0; j < 10; j++) {
                System.out.print(" "+myBoard.board[i][j]);
            }
            System.out.println();
        }
    }

    private boolean checkDestroyed(int x, int y){
        boolean isDestroyed = true;
        myBoard.board[y][x] = 'c';
        for(int k=0; k<4; k++){
            if(x+dx[k] >= 0 && x+dx[k] < xSize && y+dy[k] >= 0 && y+dy[k] < ySize){
                if(myBoard.board[y+dy[k]][x+dx[k]] == 'd') {
                    myBoard.board[y+dy[k]][x+dx[k]] = 'c';
                    checkDestroyed(x+dx[k], y+dy[k]);
                }
                else if(myBoard.board[y+dy[k]][x+dx[k]] == 's'){
                    isDestroyed = false;
                    break;
                }
            }
        }
        return isDestroyed;
    }

    public void onOpponentsQuit(){
        won = true;
    }
}

class Point{
    int x;
    int y;
    Point(int x, int y){
        this.x = x;
        this.y = y;
    }
}