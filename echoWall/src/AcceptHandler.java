import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * @author li
 * @date 2019/9/17
 */
public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

    @Override
    public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
        if (attachment.isOpen()){
            attachment.accept(attachment,this);
        }

        if (result != null && result.isOpen()){
            ClientHandler handler = new ClientHandler(result);

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            Map<String,Object> map = new HashMap<>(16);
            map.put("type","read");
            map.put("buffer",buffer);
            result.read(buffer,map,handler);
        }
    }

    @Override
    public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
        //处理错误
        exc.getStackTrace();
    }
}
