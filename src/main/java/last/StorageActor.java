package last;
import java.util.*;
import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
//Актор который хранит результаты тестов после их выполнения
public class StorageActor extends AbstractActor {
    private HashMap<Integer, ArrayList<StorageMSG>> data = new HashMap<>();
    //При получении сообщения
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                //cообщение с результатом одного теста -> кладет его в локальное хранилище.
                .match(
                        GetMSG.class, req ->
                                //отправляет сообщение
                                getSender().tell(
                                        data.get(req.getPackageId()).toArray(),
                                        ActorRef.noSender()
                                )
                )
                //Сообщение с запросом результата теста → отвечает сообщением с результатом всех тестов для заданного packageId
                .match(StorageCommand.class, msg -> {
                            if (data.containsKey(msg.getPackageID())) {
                                ArrayList<StorageMSG> tests = data.get(msg.getPackageID());
                                tests.add(msg.getStorageMSG());
                                data.put(msg.getPackageID(), tests);
                            } else {
                                ArrayList<StorageMSG> tests = new ArrayList<>();
                                tests.add(msg.getStorageMSG());
                                data.put(msg.getPackageID(), tests);
                            }
                        }
                ).build();
    }
}