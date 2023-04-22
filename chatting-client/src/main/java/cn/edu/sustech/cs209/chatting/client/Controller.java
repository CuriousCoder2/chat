package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
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


import java.util.HashSet;
import java.util.Set;


public class Controller implements Initializable {
    @FXML
    public ListView<String> chatList;
    @FXML
    ListView<Message> chatContentList;


    String username;


    TabPane chatPane;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && !input.get().isEmpty()) {
            username = input.get();
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
            try {
                if (!loginToServer()) {
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
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }

        chatContentList.setCellFactory(new MessageCellFactory());
        try {
            System.out.println(getUserListFromServer());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //添加自己的名字到服务器的在线用户列表中，并且判断是否服务器在线用户列表中已经有自己的名字
    public boolean loginToServer() throws IOException {
        String hostName = "localhost"; // 监听的服务器主机名
        int portNumber = 1234; // 服务器端口号

        try (Socket socket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()))) {

            out.println("LOGIN_TO_SERVER" + username); // 发送请求消息
            String response = in.readLine(); // 接收响应消息
            if (response != null && response.startsWith("LOGIN_SUCCESS")) {
                return true;
            } else if (response != null && response.startsWith("EXIST_USER")) {
                return false;
            }
            return false;
        }
    }


    //获得目前在线的所有用户
    public List<String> getUserListFromServer() throws IOException {
        String hostName = "localhost"; // 监听的服务器主机名
        int portNumber = 1234; // 服务器端口号
        List<String> userList = new ArrayList<>();

        try (Socket socket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_USER_LIST"); // 发送请求消息
            String response = in.readLine(); // 接收响应消息
            if (response != null && response.startsWith("USER_LIST:")) {
                String[] userArray = response.substring("USER_LIST:".length()).split(",");
                userList = Arrays.asList(userArray);
            }
        }
        return userList;
    }


    @FXML
    public void createPrivateChat() throws IOException {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        List<String> userList = getUserListFromServer();
        userList =
            userList.stream().filter(name -> !name.equals(username)).collect(Collectors.toList());
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
        String selectedUser = user.get();
//        if (selectedUser != null) {
//            // Check if the user already has a private chat with the selected user
//            boolean alreadyExists = false;
//            for (String item : chatList.getItems()) {
//                if (item.equals(selectedUser)) {
//                    alreadyExists = true;
//                    chatList.getSelectionModel().select(item);
//                    break;
//                }
//            }
//
//            if (!alreadyExists) {
//                // If no chat exists with the selected user, create a new one
////                ChatItem newItem = new ChatItem(ChatItemType.PRIVATE_CHAT, selectedUser);
////                chatList.getItems().add(newItem);
////                chatList.getSelectionModel().select(newItem);
//
//            }
//        }
        // Check if there is already a chat pane with the selected user
        for (String tab : chatList.getItems()) {
            if (tab.equals(selectedUser)) {
                chatList.getSelectionModel().select(tab);
                return;
            }
        }
        //可以删
        if(selectedUser==null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Duplicate Username");
            alert.setHeaderText(null);
            alert.setContentText(
                "The username has already been taken, please choose another one.");

            Optional<ButtonType> result = alert.showAndWait();
        }

        // Create a new chat pane if not already existing
        String tab = selectedUser;
        VBox chatBox = new VBox();
        ListView<Message> chatContentList = new ListView<>();
        chatContentList.setCellFactory(new MessageCellFactory());
        TextField inputField = new TextField();
        Button sendBtn = new Button("Send");
        sendBtn.setOnAction(event -> {
            Message message=new Message(System.currentTimeMillis(),username,selectedUser,inputField.getText());
            if(message.getData()!=null) {
                try {
                    doSendMessage();//入参：selectedUser, inputField.getText()
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            inputField.clear();
        });
        chatBox.getChildren().addAll(chatContentList, new HBox(10, inputField, sendBtn));
//        tab.setContent(chatBox);
        chatList.getItems().add(tab);
        chatList.getSelectionModel().select(tab);
    }



//        // 判断是否已经存在与所选用户的聊天
//        Optional<Tab> existingTab = chatPane.getTabs().stream()
//            .filter(tab -> Objects.equals(tab.getText(), user.get()))
//            .findFirst();
//
//        if (existingTab.isPresent()) {
//            chatPane.getSelectionModel().select(existingTab.get());
//        } else {
//            // 如果不存在，创建一个新的聊天窗口
//            Tab tab = new Tab(user.get());
//            tab.setClosable(true);
//            TextArea chatContent = new TextArea();
//            chatContent.setWrapText(true);
//            chatContent.setEditable(false);
//            VBox content = new VBox();
//            content.getChildren().add(chatContent);
//
//            TextField input = new TextField();
//            input.setOnAction(event -> {
//                // 向服务器发送消息
//                String message = input.getText();
//                Message msg = new Message(System.currentTimeMillis(),username, user.get(), message);
//                try {
//                    Socket socket =new Socket("server address",8080);
//                    Client client =new Client(socket);
//                }
//                catch (Exception e){
//
//                }
//                client.sendMessage(msg);
//                input.clear();
//
//                // 在聊天窗口中显示发送的消息
//                String prevContent = chatContent.getText();
//                chatContent.setText(prevContent + username + ": " + message + "\n");
//            });
//
//            content.getChildren().add(input);
//            tab.setContent(content);
//            chatPane.getTabs().add(tab);
//            chatPane.getSelectionModel().select(tab);
//        }



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
    public void createGroupChat() throws IOException {
        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        List<String> userList = getUserListFromServer();
        userList =
            userList.stream().filter(name -> !name.equals(username)).collect(Collectors.toList());
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
                // Sort the selected users in lexicographic order
//                selectedUsers.sort(String::compareTo);

                String chatTitle;
                if (selectedUsers.size() > 3) {
                    chatTitle = String.join(", ", selectedUsers.subList(0, 3)) + "... (" + selectedUsers.size() + ")";
                } else {
                    chatTitle = String.join(", ", selectedUsers) + " (" + selectedUsers.size() + ")";
                }

                // Check if there is already a chat pane with the selected users
                for (String tab : chatList.getItems()) {
                    if (tab.equals(chatTitle)) {
                        chatList.getSelectionModel().select(tab);
                        stage.close();
                        return;
                    }
                }

                // Create a new chat pane if not already existing
                VBox chatBox = new VBox();
                ListView<Message> chatContentList = new ListView<>();
                chatContentList.setCellFactory(new MessageCellFactory());
                TextField inputField = new TextField();
                Button sendBtn = new Button("Send");
                sendBtn.setOnAction(e -> {
                    Message message=new Message(System.currentTimeMillis(),username,chatTitle,inputField.getText());
                    if(message.getData()!=null) {
                        try {
                            doSendMessage();//入参：selectedUser, inputField.getText()
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    }

                    inputField.clear();
                });
                chatBox.getChildren().addAll(chatContentList, new HBox(10, inputField, sendBtn));
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
    public void doSendMessage()throws IOException {

//        // TODO
//        String hostName = "localhost"; // 监听的服务器主机名
//        int portNumber = 1234; // 服务器端口号
//        List<String> userList = new ArrayList<>();
//
//        try (Socket socket = new Socket(hostName, portNumber);
//             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//             BufferedReader in = new BufferedReader(
//                 new InputStreamReader(socket.getInputStream()))) {
//
//            out.println("CHAT_MESSAGE"+message); // 发送请求消息
//            String response = in.readLine(); // 接收响应消息
//            if (response != null && response.startsWith("OK")) {
//               System.out.println("succeed");
//            }
//        }
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
                        ////////////
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
