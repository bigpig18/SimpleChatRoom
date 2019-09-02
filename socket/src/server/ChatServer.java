package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author li
 * @date 2019/9/2
 */
public class ChatServer {

    private final String exit = "exit";

    private ServerSocket serverSocket;
    private Map<Integer, Writer> connectedClients;

    private ChatServer(){
        connectedClients = new HashMap<>(16);
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }

    /**
     * 服务器端启动逻辑
     */
    private void start(){
        try {
            //绑定监听端口
            int defaultPort = 8888;
            serverSocket = new ServerSocket(defaultPort);
            System.out.println("服务器已启动(监听端口" + defaultPort + ")");
            while (true){
                //等待客户端连接
                Socket socket = serverSocket.accept();
                //TODO 创建ChatHandler
                new Thread(new ChatHandler(this,socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    /**
     * 用户上线
     * @param socket socket
     * @throws IOException io异常
     */
    synchronized void addClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connectedClients.put(port,writer);
            System.out.println("客户端[" + port + "]已连接到服务器...");
        }
    }

    /**
     * 用户下线
     * @param socket socket
     * @throws IOException io异常
     */
    synchronized void removeClient(Socket socket) throws IOException {
        if (socket != null){
            int port = socket.getPort();
            if (connectedClients.containsKey(port)) {
                connectedClients.get(port).close();
            }
            connectedClients.remove(port);
            System.out.println("客户端["+port+"]断开连接...");
        }
    }

    /**
     * 广播信息
     * @param socket socket
     * @param fwdMsg 要广播的信息
     * @throws IOException io异常
     */
    synchronized void forwardMessage(Socket socket, String fwdMsg) throws IOException {
        for (Integer id : connectedClients.keySet()){
            if (!id.equals(socket.getPort())){
                Writer writer = connectedClients.get(id);
                writer.write(fwdMsg);
                writer.flush();
            }
        }
    }

    /**
     * 检查用户是否退出
     * @param msg 客户端发送信息
     * @return boolean
     */
    boolean readyToExit(String msg){
        return exit.equals(msg);
    }

    /**
     * 释放资源
     */
    synchronized private void close(){
        if (serverSocket != null){
            try {
                serverSocket.close();
                System.out.println("serverSocket已关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
