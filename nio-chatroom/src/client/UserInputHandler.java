package client;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author li
 * @date 2019/9/10
 */
public class UserInputHandler implements Runnable {

    private ChatClient chatClient;
    private String name;

    public UserInputHandler(ChatClient chatClient,String name) {
        this.chatClient = chatClient;
        this.name = name;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true){
            String msg = sc.nextLine();

            try {
                chatClient.send(msg,name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
