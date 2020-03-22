package last;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import javafx.util.Pair;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;

public class NewExecuterActor extends UntypedActor {
    private static final String JS_ENGINE = "nashorn";
    private static final String WRONG_ANSWER = "WRONG ANSWER!", CORRECT_ANSWER = "CORRECT ANSWER!";
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ExecuteMSG) {
            ExecuteMSG m = (ExecuteMSG)message;
            Pair<Integer, FunctionPackage> msg = m.getMsg();
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


            //Настроить!
            getSender().tell(storageCommand, ActorRef.noSender());
        }
        else{
            unhandled(message);
        }
    }
}
