package last;
import org.zeromq.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;

public class Storage {
    private static long timeout;
    private static ZContext context;
    private static ZMQ.Poller poller;
    private static Map<Integer, String> storage;
    private HashMap<Integer, ArrayList<StorageMSG>> data = new HashMap<>();

    public static void main(String[] args) {
        context = new ZContext();
        //Открывает сокет DEALER, подключается к JSAkkaTester
        ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
        socket.connect("tcp://localhost:8002");

        storage = new HashMap<>();
        //Задаем размеры хранилища
        Scanner in = new Scanner(System.in);
        int start = in.nextInt();
        int end = in.nextInt();

        ZFrame initFrame = new ZFrame("INIT" + " " + start + " " + end);
        initFrame.send(socket, 0);
        //Пишем сообщение где хранилище, если подключились и задали размеры хранилища
        System.out.println("Storage start on tcp://localhost:8002");

        //Принимаем сообщения от JSAkkaTester
        poller = context.createPoller(1);
        poller.register(socket, ZMQ.Poller.POLLIN);
        //Задаем интервал остановки
        timeout = System.currentTimeMillis() + 3000;
        while (poller.poll(3000) != -1){
            //После подключения с определнным интервалом времени высылает сообщение NOTIFY в котором сообщает
            //интервал хранимых значений.
            isTimeout(socket);
            //Если получили сообщение от него
            if (poller.pollin(0)){
                ZMsg recv = ZMsg.recvMsg(socket);
                //Если размер сообщения равен трем словам, тогда
                if (recv.size() == 3) {
                    //Делим сообщение на слова
                    String[] message = recv.getLast().toString().split(" ");
                    //Первое слово команда, второе и третье параметры
                    String command = message[0];
                    //На извлечение ячейки.
                    if (command.equals("GET")){
                        int key = Integer.parseInt(message[1]);
                        sendG(key, recv, socket);
                    //На изменение ячейки кэша.
                    } else if (command.equals("PUT")){
                        int key = Integer.parseInt(message[1]);
                        String val = message[2];
                        sendP(key, val, recv);
                    }
                }
            }
        }
        //Заканчиваем работу и закрывааем сокеты
        context.destroySocket(socket);
        context.destroy();
    }

    private static void isTimeout(ZMQ.Socket socket) {
        if (System.currentTimeMillis() >= timeout) {
            System.out.println("TIMEOUT");
            timeout = System.currentTimeMillis() + 3000;
            ZFrame frame = new ZFrame("TIMEOUT");
            frame.send(socket, 0);
        }
    }
    //Выполнение команд GET и PUT, нужно их заменить
    private static void sendP(int key, String val, ZMsg recv) {
        storage.put(key, val);
        recv.destroy();
        System.out.println("PUT | key: " + key + " | val: " + val);
    }

    private static void sendG(int key, ZMsg recv, ZMQ.Socket socket) {
        String answer = storage.getOrDefault(key, "ex");
        recv.getLast().reset(answer);
        recv.send(socket);
        System.out.println("GET | key: " + key);
    }
}
