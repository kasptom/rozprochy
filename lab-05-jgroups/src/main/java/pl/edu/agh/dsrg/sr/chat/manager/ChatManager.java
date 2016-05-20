package pl.edu.agh.dsrg.sr.chat.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;
import pl.edu.agh.dsrg.sr.chat.main.ChatController;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatMessage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Tomasz Kasprzyk on 2016-05-17.
 */
public class ChatManager {
    private static final String MANAGEMENT_CHANNEL = "ChatManagement321123";
    private JChannel managementChannel;
    private ProtocolStack managementStack;
    private ChatAction action;
    private String nickName;
    private ChatController controller;

    private Map<Integer, List<String>> channels = new HashMap();


    public ChatManager(String nickName, ChatController controller) throws Exception {
        this.nickName = nickName;
        this.controller = controller;
        //ORDER IS VERY IMPORTANT
        managementChannel = new JChannel(false);
        managementStack = new ProtocolStack();
        managementChannel.setProtocolStack(managementStack);
        managementChannel.setName(nickName);
        prepareStack(managementStack, null);
        managementStack.init();
        //ORDER IS VERY IMPORTANT

        //stos protokołów który należy wykorzystać
        receiveManagementMessages();
        managementChannel.connect(MANAGEMENT_CHANNEL);
        managementChannel.getState(null, 30000);
    }

    public JChannel joinChannel(int channelNumber) throws Exception {
        //ORDER IS VERY IMPORTANT
        JChannel newChannel = new JChannel(false);
        newChannel.setName(controller.getNick());
        ProtocolStack channelStack = new ProtocolStack();
        newChannel.setProtocolStack(channelStack);
        prepareStack(channelStack, channelNumber);

        //ORDER IS VERY IMPORTANT

        action = ChatAction.newBuilder()
                .setAction(ChatAction.ActionType.JOIN)
                .setChannel(String.valueOf(channelNumber))
                .setNickname(nickName)
                .build();
        newChannel.setProtocolStack(channelStack);
        channelStack.init();

        byte[] rawStream = action.toByteArray();
        Message msg = new Message(null, null, rawStream);
        newChannel.connect(String.valueOf(channelNumber));
        managementChannel.send(msg);
        receiveMessages(newChannel, channelNumber);
        return newChannel;
    }

    public void leaveChannel(int channelNumber, JChannel leftChannel) throws Exception {
        action = ChatAction.newBuilder()
                .setAction(ChatAction.ActionType.LEAVE)
                .setChannel(String.valueOf(channelNumber))
                .setNickname(nickName)
                .build();
        byte[] rawStream = action.toByteArray();
        Message msg = new Message(null, null, rawStream);
        managementChannel.send(msg);

        leftChannel.close();
    }

    public void sendMessage(JChannel aChannel, String text) throws Exception {
        ChatMessage chatMessage = ChatMessage.newBuilder()
                .setMessage(text)
                .build();
        byte[] rawStream = chatMessage.toByteArray();
        Message msg = new Message(null, null, rawStream);
        aChannel.send(msg);
    }

    private void prepareStack(ProtocolStack stack, Integer ch) throws Exception {
        UDP udp = new UDP();
        if (ch != null) {
            udp.setValue("mcast_group_addr", InetAddress.getByName("230.0.0." + String.valueOf(ch)));
        }
        stack.addProtocol(udp)
                .addProtocol(new PING())
                .addProtocol(new MERGE2())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK())
                .addProtocol(new UNICAST2())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE_TRANSFER())
                .addProtocol(new FLUSH());
    }

    private void receiveMessages(JChannel aChannel, int channelNumber) {
        aChannel.setReceiver(
                new ReceiverAdapter() {
                    @Override
                    public void viewAccepted(View view) {
                        super.viewAccepted(view);
                        System.out.println("View accepted from: "+ channelNumber + " on channel: " + aChannel.getName());
                        System.out.println("-----users----");
                        List<String> currentUsers = new LinkedList<>();
                        List<Address> addresses = view.getMembers();
                        System.out.println("--------------");

                        for (Address address : addresses) {
                            String user = address.toString();
                            currentUsers.add(user);
                            System.out.println(user);
                        }

                        System.out.println(view.toString());
                    }

                    @Override
                    public void receive(Message msg) {
                        try {
                            ChatMessage message = ChatMessage.parseFrom(msg.getRawBuffer());
                            String textMessage = message.getMessage();
                            System.out.println("on channel: " + channelNumber);
                            System.out.println("received msg from "
                                    + aChannel.getName() + ": "
                                    + textMessage);
                            controller.appendMessage(channelNumber, "[" + aChannel.getName(msg.getSrc()) + "]: " + textMessage + "\n");
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    private void receiveManagementMessages() {
        managementChannel.setReceiver(
                new ReceiverAdapter() {
                    @Override
                    public void viewAccepted(View view) {
                        System.out.println("chatManager.viewAccepted()");
                        synchronized(channels) {
                            super.viewAccepted(view);
                            List<Address> usersAddresses = view.getMembers();
                            List<String> connectedUsers = new LinkedList<>();

                            System.out.println("-----------members---------------");
                            for(Address address : usersAddresses){
                                connectedUsers.add(managementChannel.getName(address));
                                System.out.println("    * "+managementChannel.getName(address));
                            }
                            System.out.println("--------------------------------------");
                            for(Map.Entry<Integer, List<String>> channel : channels.entrySet()){
                                List<String> channelUsers = new LinkedList<>(channel.getValue());
                                channelUsers.retainAll(connectedUsers); //channelUsers contains only connected users
                                channel.setValue(channelUsers);
                            }
                            controller.setChannels(channels);
                        }
                    }


                    @Override
                    public void receive(Message msg) {
                        System.out.println("chatManager.receive()");
                        try {
                            synchronized (channels) {
                                ChatAction action = ChatAction.parseFrom(msg.getRawBuffer());
                                Integer ch = Integer.parseInt(action.getChannel());
                                String nickName = action.getNickname();
                                ChatAction.ActionType type = action.getAction();
                                System.out.println("nick: "+ nickName);
                                System.out.println("ch: "+ ch);
                                System.out.println("type: "+ type.toString());
                                //prepare user list
                                List<String> users = channels.get(ch);
                                if (users == null) {
                                    users = new LinkedList<>();
                                }

                                if (type == ChatAction.ActionType.JOIN) {
                                    users.add(nickName);
                                    channels.put(ch, users);
                                    controller.setChannels(channels);
                                    controller.updateGUI();
                                } else if (type == ChatAction.ActionType.LEAVE) {
                                    users.remove(nickName);
                                    channels.put(ch, users);
                                    controller.setChannels(channels);
                                    controller.updateGUI();
                                } else {
                                    System.out.println("unknown action");
                                }
                                printChannels();
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void getState(OutputStream output) throws Exception {
                        System.out.println("chatManager.getState()");
                        printChannels();
                        if(channels.isEmpty())
                            System.out.println("channels.isEmpty()");
                        synchronized (channels) {
                            ChatOperationProtos.ChatState.Builder builder = ChatOperationProtos.ChatState.newBuilder();

                            for (Map.Entry<Integer, List<String>> channel : channels.entrySet()) {
                                Integer ch = channel.getKey();
                                List<String> users = channel.getValue();

                                for (String nick : users) {
                                    builder.addStateBuilder()
                                            .setAction(ChatAction.ActionType.JOIN)
                                            .setChannel(String.valueOf(ch))
                                            .setNickname(nick);
                                }
                            }

                            ChatOperationProtos.ChatState state = builder.build();
                            state.writeTo(output);
//                            Util.objectToStream(state, new DataOutputStream(output));
                        }
                    }

                    @Override
                    public void setState(InputStream input) throws Exception {
                        System.out.println("chatManager.setState()");
                        synchronized (channels) {
                            ChatOperationProtos.ChatState state = ChatOperationProtos.ChatState.parseFrom(input);
                            //Util.objectFromStream(new DataInputStream(input));
                            channels.clear();

                            if(state.getStateList().isEmpty())
                                System.out.println("state.getStateList().isEmpty()");
                            for (ChatAction action : state.getStateList()) {
                                String channelName = action.getChannel();
                                String nick = action.getNickname();
                                Integer ch = Integer.parseInt(channelName);

                                if (!channels.containsKey(Integer.parseInt(channelName))) {
                                    channels.put(ch, new LinkedList<String>());
                                }

                                channels.get(ch).add(nick);
                                controller.setChannels(channels);
                                controller.updateGUI();
                            }
                        }
                    }
                }
        );
    }
    private  void printChannels(){
        System.out.println("------------------------------------");
        for(Map.Entry<Integer, List<String>> channel : channels.entrySet()){
            System.out.println("CH: "+channel.getKey());
            for(String user : channel.getValue()){
                System.out.println("    * "+ user);
            }
        }
        System.out.println("------------------------------------");
    }
}
