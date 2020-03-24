package last;

import akka.actor.ActorRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BonusGMSG {
    private ActorRef sender;
    private GetMSG msg;
    private final static String ACTORREF = "ActorRef", GET_MSG = "GetMSG";

    //Присваиваем значения
    @JsonCreator
    public BonusGMSG(@JsonProperty(ACTORREF) ActorRef actor,
                     @JsonProperty(GET_MSG) GetMSG msg) {
        this.sender = actor; //ActorRef
        this.msg = msg;//Ожидаемый результат
    }
    //Возвращает сообщение
    public GetMSG getMSG() {
        return msg;
    }
    //Возвращает отправителя
    public ActorRef getActor() {
        return sender;
    }
}
