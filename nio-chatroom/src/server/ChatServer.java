package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author li
 * @date 2019/9/9
 */
public class ChatServer {
    private static final int DEFAULT_PORT = 8081;
    private static final String EXIT = "EXIT";
    private static final int BUFFER_SIZE = 1024;

    private ServerSocketChannel server = null;
    private Selector selector = null;
    private Charset charset = Charset.forName("UTF-8");
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private int port;

    private ChatServer(){
        this(DEFAULT_PORT);
    }

    /**
     * 可自定义端口
     * @param port 端口号
     */
    private ChatServer(int port){
        this.port = port;
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }

    public void start(){
        try {
            selector = Selector.open();

            server = ServerSocketChannel.open();
            //设置为非阻塞
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));
            //监听accept事件
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器已启动[" + port + "],等待连接...");

            while(true){
                int num = selector.select();
                if (num == 0){
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys){
                    // 处理被触发的事件
                    handles(key);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException {
        //获取已就绪channel
        SocketChannel client = (SocketChannel) key.channel();
        // ACCEPT事件 - 与客户端建立连接
        if (key.isAcceptable()){
            client.configureBlocking(false);
            //将channel注册为可读事件
            client.register(selector,SelectionKey.OP_READ);
            //回复客户端信息
            client.write(charset.encode("已连接服务器..."));
            //本地提示
            System.out.println("客户端["+ client.socket().getPort() +"]已连接...");
        }
        // READ事件 - 客户端发送了消息给服务端
        else if (key.isReadable()){
            String request = receive(client);
            if (request.isEmpty()){
                //客户端异常
                key.cancel();
                selector.wakeup();
            }else{
                forwardMessage(client,request);
                //检查用户是否退出
                if (readyToExit(request)){
                    key.cancel();
                    selector.wakeup();
                    System.out.println("客户端["+ client.socket().getPort() +"]断开连接...");
                }
            }
        }
    }

    private void forwardMessage(SocketChannel client, String request) throws IOException {
        //把所有注册在selector上的key都返回
        Set<SelectionKey> selectionKeys = selector.keys();
        for (SelectionKey key : selectionKeys){
            if (key.isValid() && key.channel() instanceof SocketChannel){
                if (!key.channel().equals(client)){
                    wBuffer.clear();
                    wBuffer.put(charset.encode(request));
                    wBuffer.flip();
                    while (wBuffer.hasRemaining()){
                        ((SocketChannel) key.channel()).write(wBuffer);
                    }
                }
            }
        }
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while(client.read(rBuffer) > 0){};
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    private boolean readyToExit(String msg){
        return msg.equals(EXIT);
    }

    private void close(Closeable... closeable){
        for (Closeable clo : closeable){
            if (clo != null){
                try {
                    clo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
