package pl.edu.agh.dsrg.sr.chat.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Tomasz Kasprzyk on 2016-05-17.
 */
public class ChatView {
    private int sceneWidth = 800;
    //private int sceneHeight = 830;
    private int sceneHeight = 600;

    private BorderPane mainLayout = new BorderPane();
    private VBox channelMenu = new VBox();
    private GridPane channelsPane = new GridPane();
    GridPane messagesPane = new GridPane();
    Label messagesField = new Label();

    HBox writePane = new HBox(5);
    TextField writeBox;
    Label createChannelLabel;
    Button addButton = new Button("+");
    Button rmButton = new Button("-");
    Button listButton = new Button("list channels");
    HBox addRemove = new HBox();

    TextField createChannelTextField;


    GridPane usersPane = new GridPane();
    Label usersLabel = new Label();

    ChatController controller;
    Label nickNameLabel;


    public ChatView() throws Exception {

        controller = new ChatController();
        //
        TextInputDialog dialog = new TextInputDialog("nickname");
        dialog.setTitle("Choose Your nickname");
        dialog.setContentText("Please enter your nickname:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        String nickname = null;
        if (result.isPresent()) {
            System.out.println("Your name: " + result.get());
            nickname = result.get();
        }

        mainLayout.setBackground(new Background(new BackgroundFill(Color.DARKKHAKI, null, null)));

        channelMenu.setPrefWidth(175);
        channelsPane.setPrefHeight(800);
        //add channel [             ]

        createChannelLabel = new Label("create channel: ");
        createChannelTextField = new TextField();
        addRemove.getChildren().add(createChannelTextField);
        addRemove.getChildren().add(addButton);
        addRemove.getChildren().add(rmButton);
        rmButton.setOnAction(controller);
        addButton.setOnAction(controller);
        listButton.setOnAction(controller);
        listButton.setPrefWidth(200);
        listButton.setPrefHeight(20);

        channelMenu.getChildren().add(addRemove);
        channelMenu.getChildren().add(listButton);

        addButton.setPadding(new Insets(0,0,0,0));
        rmButton.setPadding(new Insets(0,0,0,0));
        addButton.setPrefSize(20,40);
        rmButton.setPrefSize(20,40);

        channelMenu.getChildren().add(channelsPane);
        // createChannelTextField.setOnAction(controller);

        // nickname: [text field]
        nickNameLabel = new Label("N/A");
        writeBox = new TextField();
        writeBox.setOnAction(controller);
        nickNameLabel.setPrefWidth(100);
        nickNameLabel.setAlignment(Pos.BASELINE_CENTER);
        writeBox.setPrefWidth(500);
        writePane.getChildren().addAll(nickNameLabel, writeBox);
        //  writePane.setSpacing(10);

        usersPane.setPrefWidth(100);
        usersPane.getChildren().add(usersLabel);
        usersLabel.setPadding(new Insets(4,4,4,4));
        usersLabel.setAlignment(Pos.TOP_LEFT);

        //channelsPane.setPrefSize(280 * scale, 280 * scale);
        channelMenu.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        channelsPane.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));

        channelsPane.setHgap(2);
        channelsPane.setVgap(2);
        channelsPane.setPadding(new Insets(2, 2, 2, 2));  //(top/right/bottom/left)
        messagesPane.setBackground(new Background(new BackgroundFill(Color.DEEPSKYBLUE, null, null)));

        messagesField.setPrefSize(500, 800);
        messagesPane.setPadding(new Insets(5,15,5,15));
        messagesField.setAlignment(Pos.TOP_LEFT);
        messagesPane.getChildren().add(messagesField);


        writePane.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));
        usersPane.setBackground(new Background(new BackgroundFill(Color.ORANGE, null, null)));

        mainLayout.setLeft(channelMenu);
        mainLayout.setCenter(messagesPane);
        mainLayout.setBottom(writePane);
        mainLayout.setRight(usersPane);


        //  channelsPane.setPadding(new Insets(5));
        controller.setUp(nickname, this);
    }

    public BorderPane getMainLayout() {
        return mainLayout;
    }

    public void setNickNameLabel(String nickName) {
        nickNameLabel.setText(nickName);
    }

    public void setChannelsList(Map<Integer, MyChannel> channels) {
        //clear displayed channels
        channelsPane.getChildren().clear();
        int rows = (int) Math.floor(channelMenu.getHeight() / 10);
        int cols = (int) Math.floor(channelsPane.getWidth() / 35);
        int row = 0;
        int col = 0;
        for (MyChannel myChannel : channels.values()) {
            myChannel.channelButton.setPrefSize(35, 10);
            myChannel.channelButton.setPadding(new Insets(0));
            myChannel.channelButton.setBackground(new Background(new BackgroundFill(Color.GRAY, null, null)));
            myChannel.channelButton.setOnAction(controller);
            channelsPane.add(myChannel.channelButton, col, row);
            col++;
            if (col == cols) {
                col = 0;
                row++;
            }
        }
    }


    public TextField getWriteBox() {
        return writeBox;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getRmButton() {
        return rmButton;
    }

    public Button getListButton() {
        return listButton;
    }

    public TextField getCreateChannelTextField() {
        return createChannelTextField;
    }

    public Label getMessagesField(){ return messagesField; }

    public int getSceneHeight() {
        return sceneHeight;
    }

    public int getSceneWidth() {
        return sceneWidth;
    }

    public Label getUsersLabel() {return usersLabel; }

}
