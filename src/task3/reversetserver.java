package task3;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class reversetserver {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(20000)) {
            System.out.println("�������ڶ˿� 20000 �ϼ���");

            while (true) {
                try (Socket client = serverSocket.accept();
                     DataInputStream dis = new DataInputStream(client.getInputStream());
                     DataOutputStream dos = new DataOutputStream(client.getOutputStream())) {

                    // ���� Initialization ����
                    short type = dis.readShort();
                    if (type != 1) {
                        throw new IOException("Expected initialization message from client.");
                    }
                    int N = dis.readInt();
                    System.out.println("��Ҫ����Ŀ���: " + N);

                    // ���� Agree ����
                    dos.writeShort(2); // Agree ����
                    dos.flush();

                    // ����ÿ�����ݿ�
                    for (int i = 0; i < N; i++) {
                        type = dis.readShort();
                        if (type != 3) {
                            throw new IOException("Expected reverse request from client.");
                        }
                        int length = dis.readInt();
                        byte[] buffer = new byte[length];
                        dis.readFully(buffer);

                        // ��ת���ݿ�����
                        String receivedChunk = new String(buffer, "UTF-8");
                        String reversedChunk = new StringBuilder(receivedChunk).reverse().toString();
                        byte[] reversedBuffer = reversedChunk.getBytes("UTF-8");

                        // ���� ReverseAnswer ����
                        dos.writeShort(4); // ReverseAnswer ����
                        dos.writeInt(reversedBuffer.length); // ��ת���ݵĳ���
                        dos.write(reversedBuffer);
                        dos.flush();
                    }

                    System.out.println("�ļ����ղ�������ɡ�");
                } catch (IOException e) {
                    System.err.println("�ӿͻ��˶�ȡ����ʧ��: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("����������ʧ��: " + e.getMessage());
        }
    }
}

