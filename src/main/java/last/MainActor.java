package last;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.RoundRobinPool;

//Актор роутер который в свою очередь создает все дочерние акторы

public class MainActor extends AbstractActor {
    private final static int NUM_ROUND_ROBIN_POOL = 5;
    private final ActorRef storage;//Хранилище
    private final ActorRef executors;//Исполнители
    //Функция подключения акторов
    public MainActor() {
        storage = getContext().actorOf(Props.create(StorageActor.class));
        executors = getContext().actorOf(new RoundRobinPool(NUM_ROUND_ROBIN_POOL).props(Props.create(JSExecActor.class)));
    }
    //При получении сообщения
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create().match(
                FunctionPackage.class, pack -> {
                    int len = pack.getTests().length;
                    for (int idx = 0; idx < len; idx++) {
                        //Отправляем сообщение на иполнение
                        executors.tell(new ExecuteMSG(idx, pack), storage);
                    }
                })
                //Отпревляем сообщение на хранение
                .match(GetMSG.class, req -> storage.tell(req, sender())).build();
    }
}