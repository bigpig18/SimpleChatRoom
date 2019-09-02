package ademo;

import java.io.*;
import java.net.Socket;

/**
 * @author li
 * @date 2019/8/30
 */
public class Client {

    public static void main(String[] args) {

        final String exit = "exit";
        final String defaultHost = "127.0.0.1";
        final int defaultPort = 8888;
        Socket socket = null;
        BufferedWriter writer = null;
        try {
            //创建socket
            socket = new Socket(defaultHost,defaultPort);

            //创建IO流
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //等待用户输入
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = consoleReader.readLine();

                //发送消息给服务器
                writer.write(input + "\n");
                writer.flush();

                //读取服务器返回消息
                String msg = reader.readLine();
                System.out.println(msg);

                //查看用户是否退出
                if (exit.equals(input)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (writer != null){
                try {
                    writer.close();
                    System.out.println("关闭客户端socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
