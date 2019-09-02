package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author li
 * @date 2019/9/2
 */
public class UserInputHandler implements Runnable {

    private ChatClient chatClient;

    UserInputHandler(ChatClient chatClient){
        this.chatClient = chatClient;
    }

    @Override
    public void run() {
        try {
            //等待用户输入信息
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                String input = consoleReader.readLine();
                //向服务器发送消息
                chatClient.sendMsg(input);

                //检查用户是否退出
                if (chatClient.readyToExit(input)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
