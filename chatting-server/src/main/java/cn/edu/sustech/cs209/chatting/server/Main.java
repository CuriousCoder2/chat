package cn.edu.sustech.cs209.chatting.server;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
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
        out =
            new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));

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
//                            out.println("OK");
              System.out.println("shunLi");
              if (!sendTo.contains(",")) {//发给单人
                if (loggedInUsers.containsKey(sendTo)) {
                  loggedInUsers.get(sendTo).println(
                      "TRANSFER_MESSAGE:" + timestamp + ":" + sentBy + ":" +
                          sendTo + ":" + data);
                } else {
                  System.out.println("notonline " + sendTo);
                }
              } else {
                String[] tmp = sendTo.split(",");
                for (int m = 0; m < tmp.length; m++) {
                  if (loggedInUsers.containsKey(tmp[m])) {
                    loggedInUsers.get(tmp[m]).println(
                        "TRANSFER_MESSAGE:" + timestamp + ":" + sentBy + ":" +
                            sendTo + ":" + data);
                  }
                }
              }
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
              StringBuilder userList = new StringBuilder("USER_LIST:");
              for (String element : loggedInUsers.keySet()) {
                userList.append(element);
                userList.append(",");
              }
              System.out.println("request:" + userList);
              out.println(userList);
            }
//                        else if(request.startsWith("FileName:")){
//                            // 从客户端接收文件名
//                            String fileinfo = in.readLine();
//                            String[]ssd=fileinfo.split(":");
//                            String fileName=ssd[1];
//                            String giveTo=ssd[2];//仅实现单人数据传输拉倒了
//
//                            // 创建输出流，以便将数据转发给下一个客户端
//                            OutputStream outputStream = clientSocket.getOutputStream();
//
//                            // 将文件名转发给下一个客户端
//                            outputStream.write(fileName.getBytes());
//                            outputStream.write('\n');
//
//                            // 将文件数据转发给下一个客户端
//                            byte[] buffer = new byte[1024];
//                            int bytesRead = 0;
//                            while ((bytesRead = clientSocket.getInputStream().read(buffer)) != -1) {
//                                outputStream.write(buffer, 0, bytesRead);
//                            }
//
//                        }
          }
        }
      } catch (IOException e) {
        loggedInUsers.values().removeIf(value -> value.equals(out));
        System.out.println("Error handling client request: " + e.getMessage());
      } catch (NullPointerException np) {
        System.out.println("The other client is offline");
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
