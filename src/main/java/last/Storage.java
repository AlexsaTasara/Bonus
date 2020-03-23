package last;
import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zeromq.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;

public class Storage {
    private static long timeout;
    private static ZContext context;
    private static ZMQ.Poller poller;
    private static ZMQ.Socket sMain;
    private static Map<Integer, String> storage;

    private static HashMap<Integer, ArrayList<StorageMSG>> data = new HashMap<>();

    public static void main(String[] args) throws IOException {
        context = new ZContext();
        //Открывает сокет DEALER, подключается к JSAkkaTester
        //sMain = context.createSocket(SocketType.REQ);
        ZMQ.Socket sMain = context.createSocket(SocketType.REP);
        ZMQ.Socket sExecuter = context.createSocket(SocketType.ROUTER);
        sMain.connect("tcp://localhost:8002"); //Сокет соединение с main Получает от него сообщения
        sExecuter.connect("tcp://localhost:8001");//Сокет соединения с испольнителем Получает от него сообщения

        //storage = new HashMap<>();

        /*
        //Задаем размеры хранилища
        Scanner in = new Scanner(System.in);
        int start = in.nextInt();
        int end = in.nextInt();
        ZFrame initFrame = new ZFrame("INIT" + " " + start + " " + end);
        initFrame.send(sMain, 0);
        */


        //Пишем сообщение где хранилище, если подключились и задали размеры хранилища
        System.out.println("Storage start on tcp://localhost:8002");
        ObjectMapper objectMapper = new ObjectMapper();
        //Принимаем сообщения от JSAkkaTester
        poller = context.createPoller(2);
        poller.register(sMain, ZMQ.Poller.POLLIN);
        poller.register(sExecuter, ZMQ.Poller.POLLIN);
        //Задаем интервал остановки
        timeout = System.currentTimeMillis() + 3000;
        while (poller.poll(3000) != -1){
            //После подключения с определнным интервалом времени высылает сообщение NOTIFY в котором сообщает
            //интервал хранимых значений.
            isTimeout(sMain);
            //Если получили сообщение от main, то нужно отправить обратно ответ
            if (poller.pollin(0)){
                System.out.println("Получил сообщение от main");
                ZMsg recv = ZMsg.recvMsg(sMain);
                String msg = new String(recv.getLast().getData(), ZMQ.CHARSET);
                GetMSG m = objectMapper.readValue(msg, GetMSG.class);
                //Настроить!
                ZMsg z = new ZMsg();
                z.add(data.get(m.getPackageId()).toString());


                //Отправляем сообщение
                z.send(sMain);
                /*
                getSender().tell(
                        data.get(msg.getPackageId()).toArray(),
                        ActorRef.noSender()
                );
                */
            }
            else{

                //Если получили сообщение от исполнителя, то сохраняем
                if(poller.pollin(1)){
                    System.out.println("Получил сообщение от исполнителя");
                    ZMsg recv = ZMsg.recvMsg(sMain);
                    String msg = new String(recv.getLast().getData(), ZMQ.CHARSET);
                    StorageCommand com = objectMapper.readValue(msg, StorageCommand.class);
                    if (data.containsKey(com.getPackageID())) {
                        ArrayList<StorageMSG> tests = data.get(com.getPackageID());
                        tests.add(com.getStorageMSG());
                        data.put(com.getPackageID(), tests);
                    } else {
                        ArrayList<StorageMSG> tests = new ArrayList<>();
                        tests.add(com.getStorageMSG());
                        data.put(com.getPackageID(), tests);
                    }

                }
            }
        }
        //Заканчиваем работу и закрывааем сокеты
        context.destroySocket(sMain);
        context.destroySocket(sExecuter);
        context.destroy();
    }

    private static void isTimeout(ZMQ.Socket socket) {
        if (System.currentTimeMillis() >= timeout) {
            //System.out.println("TIMEOUT");
            timeout = System.currentTimeMillis() + 3000;
            ZFrame frame = new ZFrame("TIMEOUT");
            //frame.send(socket, 0);
        }
    }
}
