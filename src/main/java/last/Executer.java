package last;
import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import org.zeromq.*;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;

//Исполняет один тест из пакета.
public class Executer {
    private static final String JS_ENGINE = "nashorn";
    private static final String WRONG_ANSWER = "WRONG ANSWER!", CORRECT_ANSWER = "CORRECT ANSWER!";
    private static long timeout;
    private static ZContext context;
    private static ZMQ.Poller poller;

    public static void main(String[] args) throws ScriptException, NoSuchMethodException, IOException {
        context = new ZContext();
        //Открывает сокет DEALER, подключается к mainActor
        ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
        socket.connect("tcp://localhost:8003");
        //Пишем сообщение где хранилище, если подключились и задали размеры хранилища
        System.out.println("Executer start on tcp://localhost:8003");

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
                String all = new String(recv.getLast().getData(), ZMQ.CHARSET);
                /*
                String packId = recv.popString();
                System.out.println(packId);
                String jss = recv.popString();
                System.out.println(jss);
                String fn = recv.popString();
                System.out.println(fn);
                String tests = recv.popString();
                while(recv!=null){
                    tests+= recv.popString();
                }
                System.out.println(tests);
                String all = packId + jss + fn + tests;
                 */
                ObjectMapper objectMapper = new ObjectMapper();
                ExecuteMSG r = objectMapper.readValue(all, ExecuteMSG.class);
                Pair<Integer, FunctionPackage> msg = r.getMsg();
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
                String gfg = objectMapper.writeValueAsString(storageCommand);
                //Отправляем сообщение о команде
                //getSender().tell(storageCommand, ActorRef.noSender());
                ZMsg se = new ZMsg();
                se.add(gfg);

                se.send(socket);
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
}
