package last;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.actor.UntypedActor;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.RoundRobinPool;

public class NewMainActor extends UntypedActor {
    private final ActorRef storage;//Хранилище
    private final ActorRef executer;//Исполнители
    //Функция подключения акторов
    // Нужно ли исправлять? Скорее всего да.
    public NewMainActor() {
        storage = getContext().actorOf(Props.create(NewStorageActor.class));
        executer = getContext().actorOf(Props.create(NewExecuterActor.class));
    }
    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof FunctionPackage){
            FunctionPackage fp = (FunctionPackage)message;
            int len = fp.getTests().length;
            for (int idx = 0; idx < len; idx++) {
                //Отправляем сообщение на иполнение
                //Нужно настроить
                executer.tell(new ExecuteMSG(idx, fp), storage);
            }
        }
        else{
            //Отправляем запрос в хранилище
            if(message instanceof GetMSG){
                GetMSG msg = (GetMSG)message;

                //Настроить
                storage.tell(msg, sender());
            }
            else{
                unhandled(message);
            }
        }
    }
}
