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
        System.out.print("请输入服务器的ip地址: ");
        String serverIp = input.readLine();
        System.out.print("请输入服务器的端口号: ");
        int serverPort = Integer.parseInt(input.readLine());
        System.out.print("请输入Lmin: ");
        int Lmin = Integer.parseInt(input.readLine());
        System.out.print("请输入Lmax: ");
        int Lmax = Integer.parseInt(input.readLine());

        String filePath = "src/input.txt";
        String outputFilePath = "src/output.txt";

        try (Socket client = new Socket(serverIp, serverPort);
             DataOutputStream dos = new DataOutputStream(client.getOutputStream());
             DataInputStream dis = new DataInputStream(client.getInputStream());
             FileInputStream fis = new FileInputStream(filePath)) {

            File file = new File(filePath);
            long fileSize = file.length();

            // 确定每个数据块的大小
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

            // 发送 Initialization 报文
            dos.writeShort(1); // Initialization 类型
            dos.writeInt(N);   // 块的数量
            dos.flush();

            // 接收 Agree 报文
            short type = dis.readShort();
            if (type != 2) {
                throw new IOException("Expected agree message from server.");
            }

            // 发送文件块
            byte[] buffer = new byte[Lmax];
            int blockIndex = 1;
            List<String> reversedBlocks = new ArrayList<>(); // List to store reversed blocks

            for (int blockSize : blockSizes) {
                int bytesRead = fis.read(buffer, 0, blockSize);

                // 发送 ReverseRequest 报文
                dos.writeShort(3); // ReverseRequest 类型
                dos.writeInt(bytesRead); // 数据长度
                dos.write(buffer, 0, bytesRead);
                dos.flush(); // 确保数据被发送

                // 接收 ReverseAnswer 报文
                type = dis.readShort();
                if (type != 4) {
                    throw new IOException("Expected reverse answer from server.");
                }
                int length = dis.readInt();
                byte[] reversedBuffer = new byte[length];
                dis.readFully(reversedBuffer);

                String reversedBlock = new String(reversedBuffer, "UTF-8");
                reversedBlocks.add(reversedBlock); // Store reversed block in list
                System.out.printf("第%d块: %s\n", blockIndex, reversedBlock);
                blockIndex++;
            }
            Collections.reverse(reversedBlocks);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                for (String block : reversedBlocks) {
                    writer.write(block);
                }
            }

            System.out.println("文件传输完成，结果已保存到 " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
