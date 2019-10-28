package client;

import constant.AioConstant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 描述: AIO 简易聊天室客户端
 *
 * @author li
 * @date 2019/10/28
 */
public class ChatClient {

    private AsynchronousSocketChannel clientChannel;
    private Charset charset = Charset.forName("UTF-8");
    private int port;
    private String host;

    public ChatClient() {
        this(AioConstant.LOCALHOST,AioConstant.DEFAULT_PORT);
    }

    public ChatClient(String host,int port) {
        this.port = port;
        this.host = host;
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }

    private void start(){
        try {
            // 创建clientChannel
            clientChannel = AsynchronousSocketChannel.open();
            Future<Void> future = clientChannel.connect(new InetSocketAddress(host,port));
            future.get();
            //启动一个新线程，处理用户输入
            new Thread(new UserInputHandler(this)).start();

            ByteBuffer buffer = ByteBuffer.allocate(AioConstant.BUFFER);
            while(true){
                //要将读到的数据写入到之前声明的byteBuffer里面
                Future<Integer> readResult = clientChannel.read(buffer);
                //等待future对象返回读取的结果
                int result = readResult.get();
                //判断是否读到服务器上的数据
                if (result <= 0){
                    //服务器异常
                    System.out.println("服务器断开连接...");
                    AioConstant.close(clientChannel);
                    //服务器异常，客户端就没必要等待了，直接把所有的线程都关闭退出
                    System.exit(1);
                }else {
                    //读到了数据，将buffer解码，打印到控制台
                    buffer.flip();
                    String msg = String.valueOf(charset.decode(buffer));
                    System.out.println(msg);
                }
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    void send(String msg){
        //如果客户端发送空白消息，直接返回
        if (msg.isEmpty()){
            return;
        }
        ByteBuffer buffer = charset.encode(msg);
        Future<Integer> writeResult = clientChannel.write(buffer);
        try {
            writeResult.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("发送消息失败...");
            e.printStackTrace();
        }
    }
}
