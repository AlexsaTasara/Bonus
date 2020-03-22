package last;
import akka.actor.ActorRef;
import javafx.util.Pair;
import org.zeromq.*;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
//Исполняет один тест из пакета.
public class Executer {
    private static final String JS_ENGINE = "nashorn";
    private static final String WRONG_ANSWER = "WRONG ANSWER!", CORRECT_ANSWER = "CORRECT ANSWER!";
    private static long timeout;
    private static ZContext context;
    private static ZMQ.Poller poller;
    private static Map<Integer, String> storage;

    public static void main(String[] args) {
        context = new ZContext();
        //Открывает сокет DEALER, подключается к mainActor
        ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
        socket.connect("tcp://localhost:8003");

        storage = new HashMap<>();
        //Пишем сообщение где хранилище, если подключились и задали размеры хранилища
        System.out.println("Storage start on tcp://localhost:8003");

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
                /*
                ZMsg recv = ZMsg.recvMsg(socket);
                Pair<Integer, FunctionPackage> msg = //recv.getMsg();
                int index = msg.getKey();
                FunctionPackage functionPackage = msg.getValue();
                //Получаем тесты
                Test test = functionPackage.getTests()[index];
                ScriptEngine engine = new ScriptEngineManager().getEngineByName(JS_ENGINE);
                try{
                    engine.eval(functionPackage.getJSScript());
                } catch (ScriptException e){
                    e.printStackTrace();
                }
                Invocable invocable = (Invocable) engine;
                String res = invocable.invokeFunction(functionPackage.getFunctionName(), test.getParams()).toString();
                String check = WRONG_ANSWER;
                if(res.equals(test.getExpectedResult())){
                    check = CORRECT_ANSWER;
                }
                //Создаем сообщение о команде
                StorageMSG storageMSG = new StorageMSG(res, test.getExpectedResult(), check,test.getParams(), test.getTestName());
                StorageCommand storageCommand = new StorageCommand(functionPackage.getPackageId(), storageMSG);
                //Отправляем сообщение о команде
                getSender().tell(storageCommand, ActorRef.noSender());

                 */
                //poller.send()
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
