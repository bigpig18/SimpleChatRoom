import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

/**
 * @author li
 * @date 2019/9/17
 */
public class Server {

    private AsynchronousServerSocketChannel serverChannel;

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public void start(){
        try {
            String host = "localhost";
            int port = 8081;
            serverChannel = AsynchronousServerSocketChannel.open();
            // 绑定监听端口
            serverChannel.bind(new InetSocketAddress(host,port));
            System.out.println("服务器已启动[监听端口:"+ port +"]");
            while (true) {
                serverChannel.accept(serverChannel,new AcceptHandler());
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close(serverChannel);
        }
    }

    /**
     * 关闭资源
     * @param closeables 要关闭的资源
     */
    private void close(Closeable... closeables){
        for(Closeable clo : closeables){
            if (clo != null){
                try {
                    clo.close();
                    System.out.println("关闭资源:" + clo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
