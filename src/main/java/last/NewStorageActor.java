package last;
import java.util.HashMap;
import java.util.ArrayList;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class NewStorageActor extends UntypedActor {
    private HashMap<Integer, ArrayList<StorageMSG>> data = new HashMap<>();
    public void onReceive(Object message) throws Exception {
        if (message instanceof GetMSG) {
            GetMSG msg = (GetMSG)message;
            //Настроить!

            getSender().tell(
                    data.get(msg.getPackageId()).toArray(),
                    ActorRef.noSender()
            );
        }
        else{
            if(message instanceof StorageCommand){
                StorageCommand com = (StorageCommand)message;
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
            else{
                unhandled(message);
            }
        }
    }
}