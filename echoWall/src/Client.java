
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author li
 * @date 2019/9/17
 */
public class Client {

    private AsynchronousSocketChannel clientChannel;

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void start(){
        try {
            String host = "localhost";
            int port = 8081;
            //创建channel
            clientChannel = AsynchronousSocketChannel.open();
            Future<Void> future = clientChannel.connect(new InetSocketAddress(host,port));
            future.get();
            // 等待用户输入
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                String input = reader.readLine();
                byte[] inputByte = input.getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(inputByte);
                //发送给服务器
                Future<Integer> writeResult = clientChannel.write(buffer);
                writeResult.get();
                //读取服务器返回的消息
                buffer.flip();
                Future<Integer> readResult = clientChannel.read(buffer);
                readResult.get();
                String echo = Arrays.toString(buffer.array());
                buffer.clear();
                System.out.println(echo);
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }finally {
            close(clientChannel);
        }
    }

    private void close(Closeable closeable){
        if (closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
