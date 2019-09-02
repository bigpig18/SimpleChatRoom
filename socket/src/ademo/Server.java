package ademo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author li
 * @date 2019/8/30
 */
public class Server {

    public static void main(String[] args) {
        final int defaultPort = 8888;
        final String exit = "exit";
        ServerSocket serverSocket = null;
        try{
            //绑定监听端口
            serverSocket = new ServerSocket(defaultPort);
            System.out.println("服务器已启动，监听端口:"+defaultPort);
            while (true){
                //等待客户端连接
                Socket socket = serverSocket.accept();
                System.out.println("客户端["+socket.getPort()+"]已连接");
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String msg = null;
                while ((msg = reader.readLine()) != null) {
                    //读取客户端信息
                    System.out.println("客户端:[" + socket.getPort() + "]" + msg);
                    //回复客户端发送的消息
                    writer.write("服务器:" + msg + "\n");
                    writer.flush();

                    //查看客户端是否退出
                    if (msg.equals(exit)){
                        System.out.println("客户端[" + socket.getPort() + "]已退出");
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (serverSocket != null){
                try {
                    serverSocket.close();
                    System.out.println("关闭服务端serverSocket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
