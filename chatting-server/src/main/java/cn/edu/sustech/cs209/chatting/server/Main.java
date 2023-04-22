package cn.edu.sustech.cs209.chatting.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


//这是个服务器
//它只需要不断的运行（while true循环），并在收到不同的请求时予以恰当的反馈

public class Main {

    //目前已经登录的用户名清单
   static Set<String> loggedInUsers =new HashSet<>();

    public static void main(String[] args) throws IOException {
        int portNumber = 1234;
        ServerSocket serverSocket = new ServerSocket(portNumber);

        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String request = in.readLine();
                if(request.startsWith("CHAT_MESSAGE")){
                    System.out.println(request.substring(12));
                }
                if(request.contains("LOGIN_TO_SERVER")){
                    if (loggedInUsers.contains(request.substring(15))) {
                        String falseAlert="EXIST_USER";
                         out.println(falseAlert);
                    }
                    else {
                        loggedInUsers.add(request.substring(15));
                        String success="LOGIN_SUCCESS";
                        out.println(success);
                    }
                }
                if ("GET_USER_LIST".equals(request)) {
                    String userList = "USER_LIST:";
                    for(String element:loggedInUsers){
                        userList+=element;
                        userList+=",";
                    }
                    out.println(userList);
                }
            } catch (IOException e) {
                System.out.println("Error handling client request: " + e.getMessage());
            }
        }
    }

}
