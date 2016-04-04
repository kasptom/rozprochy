import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;


import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;



public class NoteBoardClient extends Task<Void> implements INoteBoardListener{
	private String[] args;
	private INoteBoard nb;
	private IUser u;
	private Button[][] buttons;
	private Button[][] buttons2;
	private char[][] board;
	private int[] dx = {-1,0,1,0};
	private int [] dy = {0,-1,0,1};
    private int xSize = 10;
    private int ySize = 10;
	private int pointsLeft = 20;


	private MyBoard myBoard;
	private static String boardStr;
	String fs = File.separator.toString();
	Image imageDown = new Image(getClass().getResourceAsStream("resources"+fs+"down.png"));
	Image imageMiss = new Image(getClass().getResourceAsStream("resources"+fs+"miss.png"));
	Image imageDestr = new Image(getClass().getResourceAsStream("resources"+fs+"destroyed.png"));
	private boolean myTurn = true;
	private boolean cpu = false;
	private String oppNick = null;

	public NoteBoardClient(){
		super();
	}

	public void setArgs(String[] args){
		this.args = args;
	}
	public void setButtons(Button[][]buttons, Button[][] buttons2){
		this.buttons = buttons;
		this.buttons2 = buttons2;
	}
	public void setBoard(MyBoard myBoard){
		this.myBoard = myBoard;
	}
	private boolean notLost = true;


	public Void call() {
		try {

			if(args.length != 4){
				System.out.println("Usage <nick> <server ip> <port> <opponent: real | cpu>");
				return null;
			}

			System.out.println("Your nickname is: \""+args[0]+"\"");
			//successfully registered
			if(args[3].equals("cpu"))
				cpu = true;

			u = (IUser)(new User(args[0], cpu));
			INoteBoardListener nbl = (INoteBoardListener)this;//(INoteBoardListener)(new NoteBoardClient());
			UnicastRemoteObject.exportObject(nbl,0);


			Object o = Naming.lookup("rmi://"+args[1]+":"+args[2]+"/note");
			System.out.println(o.getClass().getName());


			nb = (INoteBoard)o;
			//user registration rejection may occur
			nb.register(u, nbl);

			Scanner reader = new Scanner(System.in);  // Reading from System.in
			String text;
			while(notLost){
				text = reader.nextLine();
				if(text.equals("/exit")){
					nb.unregister(u);
					System.out.println("Good bye");
					return null;
				}
				/*if(text.equals("/gui")){
					updateGUI(5,5);
				}

                if(text.equals("/board")){
                    printBoard();
                }

                if(text.equals("/shot")){
                    onOpponentsShot(0,1);
                }*/
				nb.appendText( u.getNick(), text );
			}
		} catch (UserRejectedException ure) {
			ure.printStackTrace();
			System.out.println("[ERROR] User nick name has been rejected");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void quitTheGame() throws RemoteException, NotBoundException, MalformedURLException{
		nb.unregister(u);
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
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try{
					//System.out.println("Opponents shot: "+x+", "+y);
                    if(myBoard.board[y][x] == 'w')
                        buttons[y][x].setGraphic(new ImageView(imageMiss));
                    if(myBoard.board[y][x] == 'd')
                        buttons[y][x].setGraphic(new ImageView(imageDown));
                    if(myBoard.board[y][x] == 'x')
                        buttons[y][x].setGraphic(new ImageView(imageDestr));
				}catch (Exception e){
					e.printStackTrace();
				}
                //System.out.println("inside board["+y+"]"+"["+x+"] = "+myBoard.board[y][x]);
			}
		});
		//System.out.println("outside board["+y+"]"+"["+x+"] = "+myBoard.board[y][x]);
		if(pointsLeft == 0) {
			System.out.println("You've lost!");
			notLost = true;
			return 'l';
		}
        return myBoard.board[y][x]; /* return opponent his shot result */
	}

	public void updateGUI(int x, int y){
        myTurn = false;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
                try{
                    char field;
                    field = nb.takeShot(args[0], x,y);
                    //if you change the UI, do it here !
                    if(field == 'd')
                        buttons2[y][x].setGraphic(new ImageView(imageDown));
                    if(field == 'w')
                        buttons2[y][x].setGraphic(new ImageView(imageMiss));
                    if(field == '?') {
						System.out.println("Wait for your opponent");
						myTurn = true;
					}
					if(field == 'x') //ship destroyed
						buttons2[y][x].setGraphic(new ImageView(imageDestr));
					if(field == 'l') {
						buttons2[y][x].setGraphic(new ImageView(imageDestr));
						System.out.println("You've won!");
						Thread.sleep(2000);
						System.exit(0);
					}
                }catch (Exception e){
                    e.printStackTrace();
                }
			}
		});

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
	public void onOpponentsJoin(String oppNick){
		System.out.println("your opponent: "+ oppNick);
		this.oppNick = oppNick;
	}


	public void onOpponentsQuit(){
		//this.oppNick = null;
		System.exit(0);
	}

	public String getOppNick(){
		return oppNick;
	}
}