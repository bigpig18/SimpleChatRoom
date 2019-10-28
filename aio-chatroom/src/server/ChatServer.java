package server;

import constant.AioConstant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 描述: AIO 简易聊天室服务端
 *
 * @author li
 * @date 2019/10/28
 */
public class ChatServer {

    private AsynchronousServerSocketChannel serverSocketChannel;
    private Charset charset = Charset.forName("UTF-8");
    private List<ClientHandler> connectedClients;
    private int port;

    private ChatServer() {
        this(AioConstant.DEFAULT_PORT);
    }

    private ChatServer(int port) {
        this.port = port;
        //在线客户列表
        connectedClients = new ArrayList<>();
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }

    private void start(){
        try {
            //创建线程池
            ExecutorService executorService = Executors.newFixedThreadPool(AioConstant.THREADPOOL_SIZE);
            //自定义一个channelGroup
            AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            //现在我们的serverSocketChannel是属于上面自定义的channelGroup
            serverSocketChannel = AsynchronousServerSocketChannel.open(channelGroup);
            //绑定监听的主机地址以及端口号
            serverSocketChannel.bind(new InetSocketAddress(AioConstant.LOCALHOST,AioConstant.DEFAULT_PORT));
            System.out.printf("服务器已启动，监听[%s]端口\n",AioConstant.DEFAULT_PORT);
            //持续监听端口
            while (true){
                //当监听到accept事件时，会异步调用 AcceptHandler
                serverSocketChannel.accept(null,new AcceptHandler());
                //阻塞式调用，阻塞在这，避免资源浪费
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            AioConstant.close(serverSocketChannel);
        }
    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel,Object> {
        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
            if (serverSocketChannel.isOpen()){
                //只要服务器端的channel是开放的状态，我们就要继续地等待客户端的连接
                serverSocketChannel.accept(null,this);
            }
            if (clientChannel != null && clientChannel.isOpen()){
                ClientHandler handler = new ClientHandler(clientChannel);
                //定义buffer
                ByteBuffer buffer = ByteBuffer.allocate(AioConstant.BUFFER);
                //将新用户添加到在线用户列表
                addClient(handler);
                //第一个buffer: 当从clientChannel上读取到什么信息的时候，写到这个buffer里面
                //第二个buffer: 当做参数传入到回调函数，操作这个buffer，把clientChannel里面的数据提取出来
                clientChannel.read(buffer,buffer,handler);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("连接异常: " + exc);
        }
    }

    /**
     * 将用户添加到在线用户列表
     * @param handler 在线用户
     */
    private synchronized void addClient(ClientHandler handler) {
        connectedClients.add(handler);
        System.out.println(getClientName(handler.clientChannel) + "已上线...");
    }

    /**
     * 将用户踢出用户在线列表
     * @param handler 要下线的用户
     */
    private synchronized void removeClient(ClientHandler handler) {
        connectedClients.remove(handler);
        AioConstant.close(handler.clientChannel);
        System.out.println(getClientName(handler.clientChannel) + "已断开连接...");
    }

    private class ClientHandler implements CompletionHandler<Integer,ByteBuffer>{

        private AsynchronousSocketChannel clientChannel;

        ClientHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            //通过判断buffer是否为空，看是write还是read事件
            if (buffer != null){
                if (result <= 0){
                    // 客户端异常,将用户踢出在线列表
                    removeClient(this);
                } else {
                    buffer.flip();
                    String fwdMsg = receive(buffer);
                    System.out.println(getClientName(clientChannel) + ": " + fwdMsg);
                    forwardMessage(clientChannel,fwdMsg);
                    buffer.clear();

                    //检查用户是否退出
                    if (AioConstant.readyToExit(fwdMsg)){
                        //是就将用户踢出用户在线列表
                        removeClient(this);
                    }else {
                        //如果对象并不是想退出，这个时候我们仍需要监听其读事件
                        clientChannel.read(buffer,buffer,this);
                    }
                }
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            System.out.println("读或写操作失败: " + exc);
        }
    }

    /**
     * 服务器转发信息
     * @param clientChannel 发送该信息的客户端
     * @param fwdMsg 发送的信息
     */
    private synchronized void forwardMessage(AsynchronousSocketChannel clientChannel, String fwdMsg) {
        //遍历在线用户列表，转发信息给在线用户
        for (ClientHandler handler : connectedClients){
            //除去发送该消息的客户端不转发
            if (!clientChannel.equals(handler.clientChannel)){
                try {
                    ByteBuffer buffer = charset.encode(getClientName(handler.clientChannel) + ": "+ fwdMsg);
                    handler.clientChannel.write(buffer,null,handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 得到客户端端口
     * @param clientChannel 客户端
     * @return clientPort
     */
    private String getClientName(AsynchronousSocketChannel clientChannel) {
        int clientPort = -1;
        try {
            InetSocketAddress address = (InetSocketAddress) clientChannel.getRemoteAddress();
            clientPort = address.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "客户端["+ clientPort +"]";
    }

    /**
     * 解码buffer里面的数据
     * @param buffer 数据
     * @return String
     */
    private String receive(ByteBuffer buffer) {
        CharBuffer charBuffer = charset.decode(buffer);
        return String.valueOf(charBuffer);
    }


}
