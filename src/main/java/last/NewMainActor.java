package last;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.actor.UntypedActor;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.RoundRobinPool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.ArrayList;

public class NewMainActor extends UntypedActor {

    private static ZMQ.Poller poll;
    private static ZContext context;
    private static ZMQ.Socket sStorage,sExecuter;
    private final ActorRef storage;//Хранилище
    private final ActorRef executer;//Исполнители
    //Функция подключения акторов

    public NewMainActor() {

        context = new ZContext();
        //Открывает два сокета ROUTER.
        sStorage = context.createSocket(SocketType.REQ);
        sExecuter = context.createSocket(SocketType.DEALER);
        sStorage.bind("tcp://localhost:8002");
        sExecuter.bind("tcp://localhost:8003");
        System.out.println("Start");
        poll = context.createPoller(2);
        //От одного принимаются команды от клиентов.
        poll.register(sStorage, ZMQ.Poller.POLLIN);
        poll.register(sExecuter, ZMQ.Poller.POLLIN);


        storage = getContext().actorOf(Props.create(NewStorageActor.class));
        executer = getContext().actorOf(Props.create(NewExecuterActor.class));
    }
    @Override
    public void onReceive(Object message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        if(message instanceof FunctionPackage){
            FunctionPackage fp = (FunctionPackage)message;
            int len = fp.getTests().length;
            for (int idx = 0; idx < len; idx++) {
                //Отправляем сообщение на иполнение
                //Нужно настроить
                ExecuteMSG ex = new ExecuteMSG(idx, fp);
                ExecMSG ex1 = new ExecMSG(idx,fp);
                System.out.println(fp.getJSScript());
                executer.tell(ex, storage);

                String gfg = objectMapper.writeValueAsString(ex1);


                //gfg уже имеет jsscript вместо jsScript

                System.out.println(gfg);
                ZMsg se = new ZMsg();
                se.add(gfg);
                //Отправляем сообщение в Исполнителю
                se.send(sExecuter);
            }
        }
        else{
            //Отправляем запрос в хранилище
            if(message instanceof GetMSG){
                GetMSG msg = (GetMSG)message;

                //Настроить
                storage.tell(msg, sender());

                String gfg = objectMapper.writeValueAsString(msg);
                ZMsg se = new ZMsg();
                se.add(gfg);
                //Отправляем сообщение в Исполнителю
                se.send(sStorage);
            }
            else{
                unhandled(message);
            }
        }
    }
}