package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author li
 * @date 2019/9/2
 */
public class ChatHandler implements Runnable{

    private ChatServer chatServer;
    private Socket socket;

    public ChatHandler(ChatServer chatServer, Socket socket){
        this.chatServer = chatServer;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //存储新上线用户
            chatServer.addClient(socket);
            //读取用户发送的信息
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String msg;
            while((msg = reader.readLine()) != null){
                String fwdMsg = "客户端["+socket.getPort()+"]: " + msg +"\n";
                System.out.print(fwdMsg);
                //广播消息
                chatServer.forwardMessage(socket,fwdMsg);
                //检查用户是否退出
                if (chatServer.readyToExit(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                chatServer.removeClient(socket);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
