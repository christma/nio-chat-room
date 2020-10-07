package com.chat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NioClientHandler implements Runnable {

    private Selector selector;

    public NioClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) iterator.next();

                    iterator.remove();
                    if (selectionKey.isReadable()) {
                        readHandler(selectionKey, selector);
                    }
                    //
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 可读事件
    private void readHandler(SelectionKey selectionKey,
                             Selector selector) throws IOException {
        // 要从selectionKey 中获取已经就绪的channel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        // 创建buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        // 循环读取服务端请求信息

        String response = "";
        while (socketChannel.read(byteBuffer) > 0) {
            // 切换buffer为读模式
            byteBuffer.flip();
            // 读取buffer中的内容
            response += Charset.forName("UTF-8").decode(byteBuffer);

        }
        // 将channel 再次注册到selector上，监听其他的可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);
        // 将服务端信息打印 到 本地
        if (response.length() > 0) {
            // 广播到其他客户端
            System.out.println(" :: " + response);
        }
    }
}
