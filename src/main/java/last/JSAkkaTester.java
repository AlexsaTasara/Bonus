package last;
import akka.NotUsed;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import scala.concurrent.Future;
import akka.stream.javadsl.Flow;
import akka.stream.ActorMaterializer;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;

import java.util.ArrayList;
import java.util.concurrent.CompletionStage;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.marshallers.jackson.Jackson;
//Подключаем ZeroMQ
import org.zeromq.*;

public class  JSAkkaTester extends AllDirectives {
    //Добавляю коментарий для проверки работы
    static ActorRef mainActor;
    private static ZMQ.Poller poll;
    private static ZContext context;
    private static ZMQ.Socket sClient,sStorage;
    private static final int SERVER_PORT = 8080, TIMEOUT_MILLIS = 5000;
    private static final String POST_MESSAGE = "Message was posted";
    private static final String ROUTES = "routes", LOCALHOST = "localhost", PACKAGE_ID = "packageId";
    private static final String SERVER_INFO = "Server online on localhost:8080/\n PRESS ANY KEY TO STOP";

    public static void main(String[] args) throws Exception {
        /*
        context = new ZContext();
        //Открывает два сокета ROUTER.
        sClient = context.createSocket(SocketType.ROUTER);
        sStorage = context.createSocket(SocketType.ROUTER);
        sClient.bind("tcp://localhost:8001");
        sStorage.bind("tcp://localhost:8002");
        System.out.println("Start");
        poll = context.createPoller(2);
        //От одного принимаются команды от клиентов.
        poll.register(sClient, ZMQ.Poller.POLLIN);
        //От другого принимаются - команды NOTIFY.
        poll.register(sStorage, ZMQ.Poller.POLLIN);
        */
        //Actor system обеспечивает запуск акторов пересылку сообщений и т.д.
        ActorSystem system = ActorSystem.create(ROUTES);
        mainActor = system.actorOf(Props.create(MainActor.class));
        //Все взаимодействие с актором после его создания происходит с помощью ActorRef

        //Инициализируем http систему с помощью high level api
        final Http http = Http.get(system);
        //Создаем ActorMaterializer
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        JSAkkaTester app = new JSAkkaTester();
        //Входящий http flow
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.jsTesterRoute().flow(system, materializer);

        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(LOCALHOST, SERVER_PORT),
                materializer
        );
        /*
        while (poll.poll(3000) != -1) {
            //Сообщения от клиента имеют индекс 0
            if (poll.pollin(0)){
                ZMsg recv = ZMsg.recvMsg(sClient);
                String msg = new String(recv.getLast().getData(), ZMQ.CHARSET);
                String[] msgSplit = msg.split(" ");
                String command = msgSplit[0];
                System.out.println(msgSplit[0] + " " + msgSplit[1]);
                if (command.equals("GET")){

                } else if (command.equals("PUT")){

                }
                //С помощью команд NOTIFY ведется актуальный список подключенных частей кэша.
                //Собщения от хранилища имеют индекс 1
            }
            else if (poll.pollin(1)){
                //Получаем сообщение из Хранилища
                ZMsg recv = ZMsg.recvMsg(sStorage);
                ZFrame frame = recv.unwrap();
                String id = new String(frame.getData(), ZMQ.CHARSET);
                String msg = new String(recv.getFirst().getData(), ZMQ.CHARSET);
                String[] msgSplit = msg.split(" ");
                String command = msgSplit[0];
                if (command.equals("INIT")) {
                }else if (command.equals("TIMEOUT")){
                } else {recv.send(sClient);}
            }
            //Повторяем
            System.out.println("Proxy loop...");
        }
        */

        //Выводим информацию о сервере
        System.out.println(SERVER_INFO);
        System.in.read();
        //Закрываем сокеты
        context.destroySocket(sClient);
        context.destroySocket(sStorage);
        context.destroy();
        binding.thenCompose(ServerBinding::unbind).thenAccept(unbound -> system.terminate());
    }

    private Route jsTesterRoute() {
        return concat(
                get(
                        //В случае запроса на получение информции о тесте — используем Putterns.ask и возвращаем Future с ответом
                        () -> parameter(PACKAGE_ID, (packageId) ->
                                {
                                    //позволяет отправить сообщение и получить Future с ответным сообщением
                                    Future<Object> result = Patterns.ask(mainActor,
                                            new GetMSG(Integer.parseInt(packageId)),
                                            TIMEOUT_MILLIS);
                                    return completeOKWithFuture(result, Jackson.marshaller());
                                }
                        )
                ),
                post(
                        () -> entity(Jackson.unmarshaller(FunctionPackage.class),
                                msg -> {
                                    //отправляет сообщение
                                    mainActor.tell(msg, ActorRef.noSender());
                                    return complete(POST_MESSAGE);
                                })));
    }
}