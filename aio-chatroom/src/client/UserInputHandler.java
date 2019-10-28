package client;

import java.util.Scanner;

/**
 * 描述: 处理用户输入
 *
 * @author li
 * @date 2019/10/28
 */
public class UserInputHandler implements Runnable {

    private ChatClient chatClient;

    UserInputHandler(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true){
            String msg = sc.nextLine();
            chatClient.send(msg);
        }
    }
}
