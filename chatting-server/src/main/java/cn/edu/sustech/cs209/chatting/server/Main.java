package cn.edu.sustech.cs209.chatting.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


//这是个服务器
//它只需要不断的运行（while true循环），并在收到不同的请求时予以恰当的反馈

public class Main {

    //目前已经登录的用户名清单
    static Map<String, PrintWriter> loggedInUsers = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread thread = new Thread(new ClientHandler(clientSocket));
            thread.start();
        }

    }
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));

                while (true) {
                    String request = in.readLine();

                    if (request != null) {
                        System.out.println(request);
                        if (request.startsWith("CHAT_MESSAGE:")) {
                            String message = request.substring(13);
                            System.out.println("log: " + message);
                            String[] s = message.split(":");
                            Long timestamp = Long.parseLong(s[0]);
                            String sentBy = s[1];
                            String sendTo = s[2];
                            String data = s[3];
                            System.out.println(data);
                            if (!sendTo.contains(",")) {//发给单人
                                loggedInUsers.get(sendTo).println("TRANSFER_MESSAGE:"+timestamp+":" +sentBy+":"+sendTo+":"+ data);
                            }
                            else {
                                String[]tmp=sendTo.split(",");
                                for (int m=0;m<tmp.length;m++){
                                    loggedInUsers.get(tmp[m]).println("TRANSFER_MESSAGE:"+timestamp+":" +sentBy+":"+sendTo+":"+ data);
                                }
                            }
                            out.println("OK");
                        } else if (request.contains("LOGIN_TO_SERVER")) {
                            if (loggedInUsers.containsKey(request.substring(15))) {
                                String falseAlert = "EXIST_USER";
                                out.println(falseAlert);
                            } else {
                                loggedInUsers.put(request.substring(15), out);
                                String success = "LOGIN_SUCCESS";
                                out.println(success);
                            }
                        } else if (request.startsWith("GET_USER_LIST")) {
                            String userList = "USER_LIST:";
                            for (String element : loggedInUsers.keySet()) {
                                userList += element;
                                userList += ",";
                            }
                            System.out.println("request:" + userList);
                            out.println(userList);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client request: " + e.getMessage());
            }
        }
    }

}

//    public static void main(String[] args) throws IOException {
//        int portNumber = 12345;
//        ServerSocket serverSocket = new ServerSocket(portNumber);
//            try (Socket clientSocket = serverSocket.accept();
//                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
//                while (true) {
//                    String request = in.readLine();
//                System.out.println(request);
//                if(request.startsWith("CHAT_MESSAGE:")){
//                    String message=request.substring(13);
//                    System.out.println("log: "+message);
//                    String[]s=message.split(":");
//                    Long timestamp=Long.parseLong(s[0]);
//                    String sentBy=s[1];
//                    String sendTo=s[2];
//                    String data=s[3];
//                    if(!sendTo.contains(",")){//发给单人
//                        loggedInUsers.get(sendTo).println("TRANSFER_MESSAGE:"+data);
//                    }
//                    out.println("OK");
//                }
//                if(request.contains("LOGIN_TO_SERVER")){
//                    if (loggedInUsers.containsKey(request.substring(15))) {
//                        String falseAlert="EXIST_USER";
//                         out.println(falseAlert);
//                    }
//                    else {
//                        loggedInUsers.put(request.substring(15),out);
//                        String success="LOGIN_SUCCESS";
//                        out.println(success);
//                    }
//                }
//                if (request.startsWith("GET_USER_LIST")) {
//                    String userList = "USER_LIST:";
//                    for(String element:loggedInUsers.keySet()){
//                        userList+=element;
//                        userList+=",";
//                    }
//                    System.out.println("request:"+userList);
//                    out.println(userList);
//                }
//            }
//        }catch (IOException e) {
//                System.out.println("Error handling client request: " + e.getMessage());
//            }
//    }
//
//}
