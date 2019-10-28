package server;

import constant.AioConstant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 描述: AIO 简易聊天室服务端
 *
 * @author li
 * @date 2019/10/28
 */
public class ChatServer {

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverSocketChannel;
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public ChatServer() {
        this(AioConstant.DEFAULT_PORT);
    }

    private ChatServer(int port) {
        this.port = port;
    }

    private void start(){
        try {
            //创建线程池
            ExecutorService executorService = Executors.newFixedThreadPool(AioConstant.THREADPOOL_SIZE);
            //自定义一个channelGroup
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            //现在我们的serverSocketChannel是属于上面自定义的channelGroup
            serverSocketChannel = AsynchronousServerSocketChannel.open(channelGroup);
            //绑定监听的主机地址以及端口号
            serverSocketChannel.bind(new InetSocketAddress(AioConstant.LOCALHOST,AioConstant.DEFAULT_PORT));
            System.out.printf("服务器已启动，监听[%s]端口",AioConstant.DEFAULT_PORT);
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
        public void completed(AsynchronousSocketChannel result, Object attachment) {

        }

        @Override
        public void failed(Throwable exc, Object attachment) {

        }
    }
}
