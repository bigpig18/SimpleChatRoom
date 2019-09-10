package client;

import constant.ChatConstant;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author li
 * @date 2019/9/10
 */
public class ChatClient {
    private final String host;
    private final int port;

    private Charset charset = Charset.forName("UTF-8");
    private ByteBuffer rBuffer = ByteBuffer.allocate(ChatConstant.BUFFER_SIZE);
    private ByteBuffer wBuffer = ByteBuffer.allocate(ChatConstant.BUFFER_SIZE);
    private SocketChannel client;
    private Selector selector;

    public ChatClient(){
        this(ChatConstant.DEFAULT_HOST,ChatConstant.DEFAULT_PORT);
    }

    public ChatClient(String host,int port){
        this.host = host;
        this.port = port;
    }

    public void start(String clientName){
        try {
            System.out.println("正在连接服务器...");
            client = SocketChannel.open();
            client.configureBlocking(false);

            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT);
            client.connect(new InetSocketAddress(host,port));

            while (true){
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys){
                    handles(key,clientName);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (ClosedSelectorException e){
            //用户正常退出
            System.out.println(clientName + "退出");
        }finally {
            try {
                close(selector);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handles(SelectionKey key, String clientName) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        // CONNECT - 连接就绪
        if (key.isConnectable()){
            if (client.isConnectionPending()){
                client.finishConnect();
                //处理用户输入
                new Thread(new UserInputHandler(this,clientName)).start();
            }
            client.register(selector,SelectionKey.OP_READ);
        }
        //READ - 服务端转发消息
        else if (key.isReadable()){
            String msg = receive(client);
            if (msg.isEmpty()){
                // 服务器端异常
                close(selector);
            }else{
                System.out.println(msg);
            }
        }
    }

    public void send(String msg,String name) throws IOException {
        if (msg.isEmpty()){
            return;
        }
        wBuffer.clear();
        wBuffer.put(charset.encode(name + ": " + msg));
        wBuffer.flip();
        while (wBuffer.hasRemaining()){
            client.write(wBuffer);
        }

        //检查用户退出
        if (readyToExit(msg)){
            close(selector);
        }
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while (client.read(rBuffer) > 0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    public boolean readyToExit(String msg){
        return msg.equals(ChatConstant.EXIT);
    }

    private void close(Closeable... closeable) throws IOException {
        for (Closeable clo : closeable){
            if (clo != null){
                clo.close();
            }
        }
    }
}
