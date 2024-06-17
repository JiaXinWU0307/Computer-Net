package task3;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class reversetserver {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(20000)) {
            System.out.println("服务器在端口 20000 上监听");

            while (true) {
                try (Socket client = serverSocket.accept();
                     DataInputStream dis = new DataInputStream(client.getInputStream());
                     DataOutputStream dos = new DataOutputStream(client.getOutputStream())) {

                    // 接收 Initialization 报文
                    short type = dis.readShort();
                    if (type != 1) {
                        throw new IOException("Expected initialization message from client.");
                    }
                    int N = dis.readInt();
                    System.out.println("将要处理的块数: " + N);

                    // 发送 Agree 报文
                    dos.writeShort(2); // Agree 类型
                    dos.flush();

                    // 处理每个数据块
                    for (int i = 0; i < N; i++) {
                        type = dis.readShort();
                        if (type != 3) {
                            throw new IOException("Expected reverse request from client.");
                        }
                        int length = dis.readInt();
                        byte[] buffer = new byte[length];
                        dis.readFully(buffer);

                        // 反转数据块内容
                        String receivedChunk = new String(buffer, "UTF-8");
                        String reversedChunk = new StringBuilder(receivedChunk).reverse().toString();
                        byte[] reversedBuffer = reversedChunk.getBytes("UTF-8");

                        // 发送 ReverseAnswer 报文
                        dos.writeShort(4); // ReverseAnswer 类型
                        dos.writeInt(reversedBuffer.length); // 反转数据的长度
                        dos.write(reversedBuffer);
                        dos.flush();
                    }

                    System.out.println("文件接收并处理完成。");
                } catch (IOException e) {
                    System.err.println("从客户端读取数据失败: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("启动服务器失败: " + e.getMessage());
        }
    }
}

