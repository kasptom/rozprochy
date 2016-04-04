/**
 * Created by kasptom on 26.03.16.
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.FileSystem;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Scanner;


public class Main extends Application implements EventHandler<ActionEvent>{
    private static int xSize = 10;
    private static int ySize = 10;
    private boolean ready = false;
    private volatile static String strNick;
    private static boolean computerFlag = false;
    private boolean shotTaken = false;

    private char field;
    static String fs = File.separator.toString();

    Image imageWater = new Image(getClass().getResourceAsStream("resources"+fs+"water.png"));
    Image imageMiss = new Image(getClass().getResourceAsStream("resources"+fs+"miss.png"));
    Image imageShip = new Image(getClass().getResourceAsStream("resources"+fs+"ship.png"));
    Image imageDown = new Image(getClass().getResourceAsStream("resources"+fs+"down.png"));

    public static char[][] board = new char[xSize][ySize];
    private static MyBoard myBoard;

    static Button[][] buttons = new Button[ySize][xSize];
    static Button[][] buttons2 = new Button[ySize][xSize];
    Label[][] labels = new Label[2][xSize];
    Label[][] labels2 = new Label[2][xSize];
    ShipsCounter sp = new ShipsCounter(xSize, ySize, board);

    private static NoteBoardClient nbc = new NoteBoardClient();

    public static void main(String[] args) {
        //System.out.println("File separator: " + fs);
        strNick = args[0];
        nbc.setArgs(args);
        nbc.setButtons(buttons, buttons2);
        nbc.setBoard(myBoard);
        myBoard = new MyBoard();
        myBoard.board = board;


        Thread noteBoardThread = new Thread(nbc);
        noteBoardThread.setDaemon(true);
        noteBoardThread.start();

        //handle ctr+C
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Quitting the game...");
                try{
                    if(noteBoardThread.isAlive())
                        nbc.quitTheGame();
                }catch(RemoteException | NotBoundException | MalformedURLException | NullPointerException ex){
                    //ex.printStackTrace();
                }
            }
        });

        launch(args);
        System.exit(0);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        nbc.setBoard(myBoard);
        primaryStage.setTitle("Battleships");
        BorderPane mainLayout = new BorderPane();
        GridPane layout = new GridPane();
        GridPane layout2 = new GridPane();  //opponents layout

        layout.setPrefSize(280,280);
        layout2.setPrefSize(280,280);

        mainLayout.setLeft(layout);
        mainLayout.setRight(layout2);
        Scene scene = new Scene(mainLayout, (280+280), 280);
        primaryStage.setScene(scene);
        layout.setPadding(new Insets(5));


        for(int i=0; i<xSize; i++){
            labels[0][i] = new Label(String.valueOf(i+1));
            labels[1][i] = new Label(String.valueOf((char)('A'+i)));
            labels[0][i].setPrefSize(22,22);
            labels[1][i].setPrefSize(22,22);

            labels2[0][i] = new Label(String.valueOf(i+1));
            labels2[1][i] = new Label(String.valueOf((char)('A'+i)));
            labels2[0][i].setPrefSize(22,22);
            labels2[1][i].setPrefSize(22,22);
            for(int j=0; j<ySize; j++) {
                board[j][i] = 'w';
                buttons[j][i] = new Button();
                buttons[j][i].setGraphic(new ImageView(imageWater));
                buttons[j][i].setOnAction(this);   /*this - class that handles an event */
                //buttons[j][i].setGraphic(new ImageView(imageWater));
                buttons[j][i].setPrefSize(22, 22);
                buttons[j][i].setPadding(Insets.EMPTY);
                // layout.getChildren().add(buttons[i][j]);
                layout.add(buttons[j][i], i + 1, j + 1);

                buttons2[j][i] = new Button();
                buttons2[j][i].setGraphic(new ImageView(imageWater));
                buttons2[j][i].setOnAction(this);   /*this - class that handles an event */
                //buttons[j][i].setGraphic(new ImageView(imageWater));
                buttons2[j][i].setPrefSize(22, 22);
                buttons2[j][i].setPadding(Insets.EMPTY);
                // layout.getChildren().add(buttons[i][j]);
                layout2.add(buttons2[j][i], i + 1, j + 1);
            }
            layout.add(labels[0][i],0,i+1);
            layout.add(labels[1][i],i+1,0);

            layout2.add(labels2[0][i],0,i+1);
            layout2.add(labels2[1][i],i+1,0);
        }
        //add bottom description
        GridPane bottomGrid = new GridPane();
        Label userLabel = new Label(strNick);
        userLabel.setPrefSize(230,22);
        Label opponentLabel = new Label("Opponent");
        mainLayout.setBottom(bottomGrid);
        bottomGrid.add(userLabel, 0, 0);
        bottomGrid.add(opponentLabel, 1, 0);
        bottomGrid.setAlignment(Pos.CENTER);

        primaryStage.show();
    }

    public void handle(ActionEvent event) {

        if(!ready){
            for(int i=0; i<xSize; i++){
                for(int j=0; j<ySize; j++){
                    if(event.getSource() == buttons[j][i]){
                       // System.out.println("x="+i+", y="+j);
                        if(board[j][i] == 'w') {
                            buttons[j][i].setGraphic(new ImageView(imageShip));
                            board[j][i] = 's';
                        }
                        else {
                            buttons[j][i].setGraphic(new ImageView(imageWater));
                            board[j][i] = 'w';
                        }
                        if (sp.countShips()) {
                            ready = true;
                            //nbc.setBoard(myBoard);
                        }
                    }
                }
            }
        }else if(nbc.isMyTurn()){
           // shotTaken = true;
            for(int i=0; i<xSize; i++){
                for(int j=0; j<ySize; j++){
                    if(event.getSource() == buttons2[j][i]){
                      //  System.out.println("x="+i+", y="+j);
                        nbc.updateGUI(i,j);
                    }
                }
            }
        }
    }
}
