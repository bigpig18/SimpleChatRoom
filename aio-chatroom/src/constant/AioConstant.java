package constant;

import java.io.Closeable;
import java.io.IOException;

/**
 * 描述: AIO 简易聊天室所要使用到的常量以及方法
 *
 * @author li
 * @date 2019/10/28
 */
public class AioConstant {

    private static final String EXIT = "exit";

    public static final String LOCALHOST = "localhost";
    public static final int DEFAULT_PORT = 8080;
    public static final int BUFFER = 1024;
    public static final int THREADPOOL_SIZE = 8;

    /**
     * 资源关闭
     * @param closeables 要关闭的资源
     */
    public static void close(Closeable... closeables){
        for (Closeable closeable : closeables){
            if (closeable != null){
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 是否退出
     * @param msg 输入信息
     * @return boolean
     */
    public static boolean readyToExit(String msg){
        return EXIT.equals(msg);
    }
}
