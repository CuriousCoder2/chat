package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;



public class Controller implements Initializable {
    @FXML
    public ListView<String> chatList;
    @FXML
    ListView<Message> chatContentList;
    ObservableList<Message> chatContent;

    HashMap<String,ListView<Message>> record=new HashMap<>();

    @FXML
    TextArea inputArea;

    Socket socket;
    PrintWriter out;
    BufferedReader in;

    boolean flag=false;
    int transferIdentify=0;
    List<String> userList;//从服务器获取的在线用户信息

    String username;

    String othername;
    @FXML
    Label currentUsername;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && !input.get().isEmpty()) {
            username = input.get();
            currentUsername.setText("当前用户："+username);


            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
            try {
                socket=new Socket("localhost",12345);
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                in=new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                startListening();
                out.println("LOGIN_TO_SERVER" + username);
                out.flush();
                Thread.currentThread().sleep(300);
                if(!flag){
                    // 如果已经有相同的用户名登录，则要求用户重新输入用户名(设置了一个警报来解决）
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Duplicate Username");
                    alert.setHeaderText(null);
                    alert.setContentText(
                        "The username has already been taken, please choose another one.");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        //收到警报后重新打开登录界面
                        initialize(url, resourceBundle);
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("服务器宕机");
            } catch (InterruptedException e){

            }
        } else {
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }
        chatList.setOnMouseClicked(event -> {
           String selectedChat = chatList.getSelectionModel().getSelectedItem();
           System.out.println(selectedChat);
           if(selectedChat!=null) {
               chatContentList.setItems(FXCollections.observableArrayList());
               chatContent=chatContentList.getItems();
               for (int q = 0; q < record.get(selectedChat).getItems().size(); q++) {
                   chatContent.add(record.get(selectedChat).getItems().get(q));
                System.out.println(record.get(selectedChat).getItems().get(q));
               }
           }
        });
        chatContentList.setCellFactory(new MessageCellFactory());
    }



    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();
        chatContentList.setItems(FXCollections.observableArrayList());
        chatContent=chatContentList.getItems();


        // FIXME: get the user list from server, the current user's name should be filtered out
        try {
            out.println("GET_USER_LIST");
            Thread.currentThread().sleep(200);
            System.out.println(userList);
            userList = userList.stream().filter(name -> !name.equals(username)).collect(Collectors.toList());

        }catch (Exception e){
            System.out.println(e);
        }
        userSel.getItems().addAll(userList);


        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
        othername=user.get();
        // Check if there is already a chat pane with the selected user
        for (String tab : chatList.getItems()) {
            if (tab.equals(othername)) {
                System.out.println(record.get(tab).getItems().size());
                chatList.getSelectionModel().select(tab);
                for(int q=0;q<record.get(tab).getItems().size();q++){
                    chatContent.add(record.get(tab).getItems().get(q));
                    System.out.println(record.get(tab).getItems().get(q));
                }
                return;
            }
        }

        // Create a new chat pane if not already existing
        chatList.getItems().add(othername);
        if(!record.containsKey(othername)) {
            record.put(othername, new ListView<>());
        }
        else {
            for(int q=0;q<record.get(othername).getItems().size();q++){
                chatContent.add(record.get(othername).getItems().get(q));
                System.out.println(record.get(othername).getItems().get(q));
            }
        }
        chatList.getSelectionModel().select(othername);
    }




    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() throws IOException, InterruptedException {
        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();
        chatContentList.setItems(FXCollections.observableArrayList());
        chatContent=chatContentList.getItems();
        // FIXME: get the user list from server, the current user's name should be filtered out
        out.println("GET_USER_LIST");
        Thread.currentThread().sleep(200);
        userList=userList.stream().filter(name -> !name.equals(username)).collect(Collectors.toList());
        userSel.getItems().addAll(userList);
        Label promptLabel = new Label("Select users to add to the group chat:");
        ListView<String> userListView = new ListView<>();
        userListView.getItems().addAll(userList);
        userListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        Button createBtn = new Button("Create");
        createBtn.setOnAction(event -> {
            List<String> selectedUsers = userListView.getSelectionModel().getSelectedItems();
            if (selectedUsers.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select at least one user to create a group chat.");
                alert.showAndWait();
            } else {
                String chatTitle;
                othername=String.join(",",selectedUsers);
                if (selectedUsers.size() ==1) {
                    chatTitle = selectedUsers.get(0);
                } else {
                    chatTitle = String.join(", ", selectedUsers);
                }
//                chatTitle=chatTitle+" (" + selectedUsers.size() + ")";

                // Check if there is already a chat pane with the selected users
                for (String tab : chatList.getItems()) {
                    if (tab.equals(chatTitle)) {
                        chatList.getSelectionModel().select(tab);
                        for(int q=0;q<record.get(tab).getItems().size();q++){
                            chatContent.add(record.get(tab).getItems().get(q));
                            System.out.println(record.get(tab).getItems().get(q));
                        }
                        stage.close();
                        return;
                    }
                }

                // Create a new chat pane if not already existing
                chatList.getItems().add(chatTitle);
                chatList.getSelectionModel().select(chatTitle);
                stage.close();
            }
        });

        VBox box = new VBox(10, promptLabel, userListView, createBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        stage.setScene(new Scene(box));
        stage.showAndWait();

    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage(){

        // TODO

        try  {
            String content=inputArea.getText();
            System.out.println("content"+content);
            if(content!=null && content.length()!=0) {
                Message message=new Message(System.currentTimeMillis(),username,othername,content);
                out.println("CHAT_MESSAGE:"+message.getTimestamp()+":"+message.getSentBy()+":"+message.getSendTo()+":"+message.getData()); // 发送请求消息
                while (transferIdentify==0) {
                }
                inputArea.clear();
                chatContent.add(message);
                record.get(othername).getItems().add(message);
                transferIdentify=0;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                        String info=in.readLine();
                        if(info!=null) {
                            System.out.println(info);
                            if (info.startsWith("TRANSFER_MESSAGE:")) {
                                String received = info.substring("TRANSFER_MESSAGE:".length());
                                String[] s = received.split(":");
                                Long timestamp = Long.parseLong(s[0]);
                                String sentBy = s[1];
                                String sendTo = s[2];
                                String data = s[3];
                                System.out.println(data);
                                    Message reciveMessage =
                                        new Message(timestamp, sentBy, sendTo, data);
                                    Platform.runLater(()-> {
                                        chatContent.clear();
                                        chatContent.add(reciveMessage);
                                        }
                                    );

                            }
                            if (info.startsWith("LOGIN_SUCCESS")) {
                                flag=true;
                            }
                            if(info.startsWith("EXIST_USER")){

                            }
                            if(info.startsWith("USER_LIST:")){
                                String[] userArray = info.substring("USER_LIST:".length()).split(",");
                                userList = Arrays.asList(userArray);
                                System.out.println("userList:"+userList);
                            }
                            if(info.startsWith("OK")){
                                System.out.println("succeed");
                                transferIdentify=1;
                            }
                        }
                    }

            } catch (IOException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("错误");
                    alert.setHeaderText("连接错误");
                    alert.setContentText("无法连接到服务器");
                    alert.showAndWait();
                });;
            }
        }).start();
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
