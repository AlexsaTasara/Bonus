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
        //Открывает сокет DEALER, подключается к mainActor sStorage
        ZMQ.Socket sMain = context.createSocket(SocketType.ROUTER);
        ZMQ.Socket sStorage = context.createSocket(SocketType.DEALER);
        sMain.connect("tcp://localhost:8003"); //Сокет соединения с main Получает от него сообщения
        sStorage.connect("tcp://localhost:8001");//Сокет соединения с хранилищем Отправляет туда сообщения

        //Пишем сообщение где хранилище, если подключились
        System.out.println("Executer start on tcp://localhost:8003");

        //Принимаем сообщения от JSAkkaTester
        poller = context.createPoller(1);
        poller.register(sMain, ZMQ.Poller.POLLIN);
        //Задаем интервал остановки
        timeout = System.currentTimeMillis() + 3000;
        while (poller.poll(3000) != -1){
            //После подключения с определнным интервалом времени высылает сообщение NOTIFY в котором сообщает
            //интервал хранимых значений.
            isTimeout(sMain);
            //Единственный вид сообщений, который может получить исполнитель это тест, результат которого отправляется в хранилище.
            if (poller.pollin(0)){
                System.out.println("Получил сообщение от main");
                ZMsg recv = ZMsg.recvMsg(sMain);
                //Преобразуем сообщение в строку
                String all = new String(recv.getLast().getData(), ZMQ.CHARSET);
                //Второй способ переноса сообщения в строку. Какой лучше пока не понятно.
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


                System.out.println(all);

                all.replaceAll("jsscript", "jsScript");

                //ПОЧЕМУ Я НЕ МОГУ ЕГО ПРОСТО ЗАМЕНИТЬ!


                System.out.println(all);
                ObjectMapper objectMapper = new ObjectMapper();
                //objectMapper.enableDefaultTyping();
                //Преобразуем строку в класс тест.
                //String all2 = all.substring(7,all.length()-1);
                //System.out.println(all2);
                ExecMSG r2 = objectMapper.readValue(all, ExecMSG.class);
                //ExecuteMSG r = objectMapper.readValue(all, ExecuteMSG.class);
                //Начинаем работать над тестом. Если сообщение и его расшифровка прошли удачно, то весь код ниже не вызовет проблем
                //Pair<Integer, FunctionPackage> msg = r.getMsg();
                int index = r2.getKey();
                FunctionPackage functionPackage = r2.getValue();
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

                //Преобразуем полученный ответ в строку и добавляем ее в сообщение
                String gfg = objectMapper.writeValueAsString(storageCommand);
                System.out.println(gfg);
                ZMsg se = new ZMsg();
                se.add(gfg);

                //Отправляем сообщение в Хранилище
                se.send(sStorage);
            }
        }
        //Заканчиваем работу и закрывааем сокеты
        context.destroySocket(sMain);
        context.destroySocket(sStorage);
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
