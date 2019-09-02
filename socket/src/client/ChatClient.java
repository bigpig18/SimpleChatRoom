package client;

import java.io.*;
import java.net.Socket;

/**
 * @author li
 * @date 2019/9/2
 */
public class ChatClient {

    private final String defaultHost = "127.0.0.1";
    private final int defaultPort = 8888;
    private final String exit = "exit";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /**
     * 启动客户端
     */
    public void start(){
        try {
            //创建socket
            socket = new Socket(defaultHost,defaultPort);
            //创建io流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //TODO 处理用户输入(另起一个线程)
            new Thread(new UserInputHandler(this)).start();

            //读取服务器转发的信息
            String msg;
            while((msg = receiveMsg()) != null){
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    /**
     * 发送消息给服务器
     * @param msg 要发送给服务器端的信息
     */
    public void sendMsg(String msg) throws IOException {
        if (!socket.isOutputShutdown()){
            writer.write(msg + "\n");
            writer.flush();
        }
    }

    /**
     * 接收信息
     * @return string
     */
    public String receiveMsg() throws IOException {
        String msg = null;
        if (!socket.isInputShutdown()){
            msg = reader.readLine();
        }
        return msg;
    }

    /**
     * 检查用户是否准备退出
     * @param msg 输入信息
     * @return boolean
     */
    public boolean readyToExit(String msg){
        return exit.equals(msg);
    }

    public void close(){
        if (writer != null){
            try {
                writer.close();
                System.out.println("关闭socket...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
