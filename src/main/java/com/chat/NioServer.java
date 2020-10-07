package com.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO 服务端
 */
public class NioServer {
    // TODO: 2020/10/7 启动
    public void start() throws Exception {
        // TODO 1、创建Selector
        Selector selector = Selector.open();
        // TODO 2、通过ServerSocketChannel 创建Channel 通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // TODO 3、为channel 通道绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8000));
        // TODO 4、设置channel 为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // TODO 5、 将channel 注册到selector上 注册监听事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println(" 服务器启动成功");

        // TODO 6、循环等待新接入的连接
        for (; ; ) {
            int readyChannels = selector.select();
            if (readyChannels == 0) {
                continue;
            }
            // 获取可用channel 的集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                // selectionKey 的实力
                SelectionKey selectionKey = (SelectionKey) iterator.next();

                //移除Set中当前的selectionKey
                iterator.remove();
                // TODO 7、 根据状态进行分别处理
                // 如果是接入事件
                if (selectionKey.isAcceptable()) {
                    acceptHandler(serverSocketChannel, selector);
                }
                // 如果是可读事件
                if (selectionKey.isReadable()) {
                    readHandler(selectionKey, selector);
                }
                //
            }
        }
        // TODO 7、 根据就绪状态，调用对应方法处理业务逻辑
    }


    // 接入事件
    private void acceptHandler(ServerSocketChannel serverSocketChannel,
                               Selector selector) throws IOException {
        // 如果是接入事件，创建socketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();
        // 将socketChannel 设置为非阻塞模式
        socketChannel.configureBlocking(false);

        // 将channel 注册到selector上，监听 可读事件

        socketChannel.register(selector, SelectionKey.OP_READ);
        // 回复客户端提示信息

        socketChannel.write(Charset.forName("UTF-8")
                .encode("你与聊天室里的其他人都不是朋友关系，请注意隐私安全"));
    }

    // 可读事件
    private void readHandler(SelectionKey selectionKey,
                             Selector selector) throws IOException {
        // 要从selectionKey 中获取已经就绪的channel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        // 创建buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        // 循环读取客户端请求信息

        String request = "";
        while (socketChannel.read(byteBuffer) > 0) {
            // 切换buffer为读模式
            byteBuffer.flip();
            // 读取buffer中的内容
            request += Charset.forName("UTF-8").decode(byteBuffer);

        }
        // 将channel 再次注册到selector上，监听其他的可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);
        // 将客户端发送的请求信息，广播到其他客户端
        if (request.length() > 0) {
            // 广播到其他客户端
            System.out.println(" :: " + request);
        }
    }


    /**
     * 主方法
     *
     * @param args
     */
    public static void main(String[] args) {

        NioServer nioServer = new NioServer();
    }
}
