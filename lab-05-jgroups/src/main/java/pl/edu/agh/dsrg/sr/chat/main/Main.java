package pl.edu.agh.dsrg.sr.chat.main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pl.edu.agh.dsrg.sr.chat.manager.ChatManager;

import java.io.IOException;

/**
 * Created by Tomasz Kasprzyk on 2016-05-17.
 */
public class Main extends Application{
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Chat");
        initRootLayout();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void initRootLayout() throws Exception {
        try {
            // get layout from view
            ChatView chatView = new ChatView();
            BorderPane mainLayout = chatView.getMainLayout();
            // add layout to a scene and show them all
            Scene scene = new Scene(mainLayout, chatView.getSceneWidth(), chatView.getSceneHeight());
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    System.out.println("Stage is closing");
                    System.exit(0);
                }
            });
        } catch (IOException e) {
            // don't do this in common apps
            e.printStackTrace();
        }
    }
}
