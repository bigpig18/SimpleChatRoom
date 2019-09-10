package client;

/**
 * @author li
 * @date 2019/9/10
 */
public class BobClient {
    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start("Bob");
    }
}
