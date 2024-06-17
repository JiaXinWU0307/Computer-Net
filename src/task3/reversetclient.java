package task3;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class reversetclient {

    public static void main(String[] args) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("�������������ip��ַ: ");
        String serverIp = input.readLine();
        System.out.print("������������Ķ˿ں�: ");
        int serverPort = Integer.parseInt(input.readLine());
        System.out.print("������Lmin: ");
        int Lmin = Integer.parseInt(input.readLine());
        System.out.print("������Lmax: ");
        int Lmax = Integer.parseInt(input.readLine());

        String filePath = "src/input.txt";
        String outputFilePath = "src/output.txt";

        try (Socket client = new Socket(serverIp, serverPort);
             DataOutputStream dos = new DataOutputStream(client.getOutputStream());
             DataInputStream dis = new DataInputStream(client.getInputStream());
             FileInputStream fis = new FileInputStream(filePath)) {

            File file = new File(filePath);
            long fileSize = file.length();

            // ȷ��ÿ�����ݿ�Ĵ�С
            List<Integer> blockSizes = new ArrayList<>();
            Random random = new Random();
            long remainingSize = fileSize;

            while (remainingSize > 0) {
                int blockSize = Lmin + random.nextInt(Lmax - Lmin + 1);
                if (blockSize > remainingSize) {
                    blockSize = (int) remainingSize;
                }
                blockSizes.add(blockSize);
                remainingSize -= blockSize;
            }

            int N = blockSizes.size();

            // ���� Initialization ����
            dos.writeShort(1); // Initialization ����
            dos.writeInt(N);   // �������
            dos.flush();

            // ���� Agree ����
            short type = dis.readShort();
            if (type != 2) {
                throw new IOException("Expected agree message from server.");
            }

            // �����ļ���
            byte[] buffer = new byte[Lmax];
            int blockIndex = 1;
            List<String> reversedBlocks = new ArrayList<>(); // List to store reversed blocks

            for (int blockSize : blockSizes) {
                int bytesRead = fis.read(buffer, 0, blockSize);

                // ���� ReverseRequest ����
                dos.writeShort(3); // ReverseRequest ����
                dos.writeInt(bytesRead); // ���ݳ���
                dos.write(buffer, 0, bytesRead);
                dos.flush(); // ȷ�����ݱ�����

                // ���� ReverseAnswer ����
                type = dis.readShort();
                if (type != 4) {
                    throw new IOException("Expected reverse answer from server.");
                }
                int length = dis.readInt();
                byte[] reversedBuffer = new byte[length];
                dis.readFully(reversedBuffer);

                String reversedBlock = new String(reversedBuffer, "UTF-8");
                reversedBlocks.add(reversedBlock); // Store reversed block in list
                System.out.printf("��%d��: %s\n", blockIndex, reversedBlock);
                blockIndex++;
            }
            Collections.reverse(reversedBlocks);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                for (String block : reversedBlocks) {
                    writer.write(block);
                }
            }

            System.out.println("�ļ�������ɣ�����ѱ��浽 " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
