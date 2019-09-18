import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;

/**
 * @author li
 * @date 2019/9/17
 */
public class ClientHandler implements CompletionHandler<Integer, Object> {

    private AsynchronousSocketChannel clientChannel;

    ClientHandler(AsynchronousSocketChannel clientChannel) {
        this.clientChannel = clientChannel;
    }


    @Override
    public void completed(Integer result, Object attachment) {
        Map<String,Object> map = (Map<String, Object>) attachment;
        String type = (String) map.get("type");
        if ("read".equals(type)){
            ByteBuffer buffer = (ByteBuffer) map.get("buffer");
            buffer.flip();
            map.put("type","write");
            clientChannel.write(buffer,map,this);
            buffer.clear();
        }else if ("write".equals(type)){
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            map.put("type","read");
            map.put("buffer",buffer);
            clientChannel.read(buffer,map,this);
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {

    }
}
