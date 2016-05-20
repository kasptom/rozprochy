package pl.edu.agh.dsrg.sr.chat.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import org.jgroups.JChannel;
import pl.edu.agh.dsrg.sr.chat.manager.ChatManager;

import java.util.*;

/**
 * Created by Tomasz Kasprzyk on 2016-05-17.
 */
public class ChatController implements EventHandler<ActionEvent> {
    ChatView view;
    ChatManager manager;
    String nickname;
    Map<Integer, MyChannel> myChannels = new TreeMap();
    Map<Integer, List<String>> allChannels = new TreeMap<>();
    Integer currentChannel = new Integer(0);
    private boolean isOn = false;

    public ChatController(){
        isOn = true;
        updateGUI();
    }

    public void printMessage(String text){
        System.out.println("Message to print: " + text);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
               // view.getMessagesField().setPrefRowCount(rows);
                view.getMessagesField().setText(text);
            }
        });

    }

    public void handle(ActionEvent event) {
        try {
            for(MyChannel myChannel : myChannels.values()){
                if(myChannel.channelButton == event.getSource()){
                    loadUsers(myChannel.number);
                    System.out.println("CHANNEL: "+ myChannel.number);
                    if(currentChannel != 0){
                        myChannels.get(currentChannel).channelButton.setBackground(new Background(new BackgroundFill(Color.GRAY, null,null)));
                    }
                    myChannel.channelButton.setBackground(new Background(new BackgroundFill(Color.GREEN, null,null)));
                    currentChannel = new Integer(myChannel.number);
                    printMessage(myChannel.getBuffer().toString());
                    break;
                }
            }
            if (event.getSource() == view.getWriteBox()) {
                String text = view.getWriteBox().getText();
                view.getWriteBox().setText("");
                if(currentChannel != 0 && !text.equals("")){
                    manager.sendMessage(myChannels.get(currentChannel).jChannel, text);
                }else{
                    System.out.println("You have not chosen a channel or Your message is empty");
                }
            }

            if (event.getSource() == view.getAddButton()) {
                currentChannel = 0;
                String channelName = view.getCreateChannelTextField().getText();
                Integer ch = Integer.parseInt(channelName);
                System.out.println("Channel to add: " + ch);
                //TODO sprawdzanie poprawności i dodawanie kanału

                if (ch <= 200 && ch >= 1) {
                    System.out.println("correct channel name");
                    if (myChannels.containsKey(ch)) {
                        System.out.println("You are already registered to channel " + ch);
                    }else{ //channel already exists but we are not registered
                        JChannel newChannel = manager.joinChannel(ch); //allChannels users updated
                        MyChannel myChannel = new MyChannel(new Button(channelName), ch, newChannel );
                        myChannels.put(ch, myChannel);
                    }
                    view.setChannelsList(myChannels);
                } else {
                    System.out.println("channel name should be an integer from the range: [1, 200]");
                }
            }

            if (event.getSource() == view.getRmButton()) {
                currentChannel = 0;
                String channelName = view.getCreateChannelTextField().getText();
                Integer ch = Integer.parseInt(channelName);
                System.out.println("Channel to remove: " + ch);
                //TODO sprawdzanie poprawności i dodawanie kanału

                if (ch <= 200 && ch >= 1) {
                    System.out.println("correct channel name");
                    if (!myChannels.containsKey(ch)) {
                        System.out.println("You were not registered to channel: " + ch);
                    }else{
                        MyChannel channelToLeave = myChannels.remove(ch);
                        manager.leaveChannel(ch, channelToLeave.jChannel);  //allChannels updated
                        view.setChannelsList(myChannels);
                    }
                } else {
                    System.out.println("channel name should be an integer from the range: [1, 200]");
                }
            }

            if(event.getSource() == view.getListButton()){
                listAllChannels();
            }
        }catch(Exception e){
           // e.printStackTrace();
            System.out.println("Incorrect channel name");
        }finally {
            view.getCreateChannelTextField().setText("");
        }
    }

    private void listAllChannels() {
        StringBuffer channelsToLabel = new StringBuffer();
        channelsToLabel.append("Channels' list: \n");
        for(Map.Entry<Integer, List<String>> channel : allChannels.entrySet()){
            channelsToLabel.append(" * channel: \"" + channel.getKey()+ "\"\n");
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                view.getMessagesField().setText(channelsToLabel.toString());
            }
        });

    }

    public void updateGUI(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
//
            }
        });

    }

    private void loadUsers(Integer ch){
        StringBuffer usersToLabel = new StringBuffer();
        List<String> channelUsers = allChannels.get(ch);
        if(channelUsers != null)
            for( String user : channelUsers){
                usersToLabel.append(user+"\n");
            }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                view.getUsersLabel().setText(usersToLabel.toString());
            }
        });

    }

    public void setUp(String nickname, ChatView view) throws Exception {
        this.view = view;
        this.nickname = nickname;
        this.view.setNickNameLabel(nickname);
        updateGUI();
        manager = new ChatManager(nickname, this);
    }

    /**
     * Called on first connection
     * @param channels
     */
    public void setChannels(Map<Integer, List<String>> channels){
        this.allChannels = channels;
       // if(currentChannel != 0){
            loadUsers(currentChannel);
      //  }
    }

    public void appendMessage(Integer ch, String textMessage) {
        System.out.println("CURRENT CHANNEL: "+currentChannel);
        System.out.println("CHANNEL: " + ch);
        myChannels.get(ch).appendBuffer(textMessage);
        myChannels.get(ch).newContent = true;
        if(currentChannel == ch.intValue()) {
            System.out.println("TAK");
           printMessage(myChannels.get(ch).getBuffer().toString());
        }
    }

    public String getNick(){
        return this.nickname;
    }
}

class MyChannel {
    private static final int MAX_BUFF_SIZE = 45;
    Button channelButton;
    Integer number;
    JChannel jChannel;
    StringBuffer buffer;
    int bufferSize = 0;
    boolean newContent = false;

    MyChannel(Button chanelButton, Integer number, JChannel jChannel) {
        this.channelButton = chanelButton;
        this.number = number;
        this.jChannel = jChannel;
        this.buffer = new StringBuffer();
    }

    public void appendBuffer(String messageText){
        buffer.append(messageText);
        String lines[] = buffer.toString().split("\n");
        System.out.println("LINES NUMBER: " + lines.length);
        bufferSize = lines.length;
        if(bufferSize >= MAX_BUFF_SIZE){
            buffer.replace(0, lines[0].length()+1, "");
        }
    }

    public int getBufferSize(){
        return bufferSize;
    }

    public StringBuffer getBuffer(){ return buffer; }

    @Override
    public String toString(){
        return String.valueOf(number);
    }
}