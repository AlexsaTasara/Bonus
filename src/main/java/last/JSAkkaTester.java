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

/*
import akka.zeromq.ZeroMQExtension;
import akka.zeromq.Bind;

import akka.zeromq.Connect;
import akka.zeromq.Listener;
import akka.zeromq.Subscribe;
*/

public class  JSAkkaTester extends AllDirectives {
    //Добавляю коментарий для проверки работы
    static ActorRef mainActor, newMainActor;
    private static ZMQ.Poller poll;
    private static ZContext context;
    private static ArrayList<Cache> caches;
    private static ZMQ.Socket sClient,sStorage,sExecuter,pubSocket;
    private static final int SERVER_PORT = 8080, TIMEOUT_MILLIS = 5000;
    private static final String POST_MESSAGE = "Message was posted";
    private static final String ROUTES = "routes", LOCALHOST = "localhost", PACKAGE_ID = "packageId";
    private static final String SERVER_INFO = "Server online on localhost:8080/\n PRESS ANY KEY TO STOP";

    public static void main(String[] args) throws Exception {

        context = new ZContext();
        caches = new ArrayList<>();
        //Открывает два сокета ROUTER.
        sClient = context.createSocket(SocketType.ROUTER);
        sStorage = context.createSocket(SocketType.ROUTER);
        sExecuter = context.createSocket(SocketType.ROUTER);
        sClient.bind("tcp://localhost:8001");
        sStorage.bind("tcp://localhost:8002");
        sExecuter.bind("tcp://localhost:8003");
        System.out.println("Start");
        poll = context.createPoller(2);
        //От одного принимаются команды от клиентов.
        poll.register(sClient, ZMQ.Poller.POLLIN);
        //От другого принимаются - команды NOTIFY.
        poll.register(sStorage, ZMQ.Poller.POLLIN);
        poll.register(sExecuter, ZMQ.Poller.POLLIN);


        //Actor system обеспечивает запуск акторов пересылку сообщений и т.д.
        ActorSystem system = ActorSystem.create(ROUTES);
        //mainActor = system.actorOf(Props.create(MainActor.class));
        newMainActor = system.actorOf(Props.create(NewMainActor.class));
        //Все взаимодействие с актором после его создания происходит с помощью ActorRef
        //newMainActor = ZeroMQExtension.get(system).newSocket();
        /*
        ActorRef pubSocket = ZeroMQExtension.get(system).newPubSocket(
                new Bind("tcp://127.0.0.1:1233"));
        ActorRef listener = system.actorOf(Props.create(NewStorageActor.class));
        ActorRef subSocket = ZeroMQExtension.get(system).newSubSocket(
                new Connect("tcp://127.0.0.1:1233"),
                new Listener(listener), Subscribe.all());
         */

        //Инициализируем http систему с помощью high level api
        final Http http = Http.get(system);
        JSAkkaTester app = new JSAkkaTester();

        //Создаем ActorMaterializer
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        //Входящий http flow
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.jsTesterRoute().flow(system, materializer);

        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow, ConnectHttp.toHost(LOCALHOST, SERVER_PORT), materializer
        );

        //Выводим информацию о сервере
        System.out.println(SERVER_INFO);
        System.in.read();
        //Закрываем сокеты
        binding.thenCompose(ServerBinding::unbind).thenAccept(unbound -> system.terminate());
    }

    private Route jsTesterRoute() { return
        concat(
            get(
                //В случае запроса на получение информции о тесте — используем Putterns.ask и возвращаем Future с ответом
                () -> parameter(PACKAGE_ID, (packageId) -> {
                        //позволяет отправить сообщение и получить Future с ответным сообщением
                        Future<Object> result = Patterns.ask(newMainActor, new GetMSG(Integer.parseInt(packageId)), TIMEOUT_MILLIS);
                        return completeOKWithFuture(result, Jackson.marshaller());
                    }
                )
            ),
            post(
                () -> entity(Jackson.unmarshaller(FunctionPackage.class),
                    msg -> {
                        //отправляет сообщение
                        newMainActor.tell(msg, ActorRef.noSender());
                        return complete(POST_MESSAGE);
                    }
                )
            )
        );
    }
}