package last;
import org.zeromq.*;
import java.util.Scanner;

public class Client {
    private static ZMQ.Socket socket;

    public static void main(String[] args) {
        ZContext context = new ZContext();
        socket = context.createSocket(SocketType.REQ);
        //Подключается к центральному прокси.
        socket.connect("tcp://localhost:8001");
        System.out.println("Client start on tcp://localhost:8001");
        //Читает команды из консоли и отправляет их в прокси.
        Scanner in = new Scanner(System.in);
        //Пока не будет подана команда Stop отправляем все написанные сообщения в JSAkkaTester
        while (true) {
            String message = in.nextLine();
            if (message.equals("STOP")){
                break;
            }
            sendAndListen(message);
        }
        context.destroySocket(socket);
        context.destroy();
    }
    //Отправляем сообщение в JSAkkaTester и ждем ответ от него. Ответ выводим на экран.
    private static void sendAndListen(String message) {
        ZFrame frame = new ZFrame(message);
        frame.send(socket, 0);
        ZMsg recv = ZMsg.recvMsg(socket);
        String response = new String(recv.getFirst().getData(), ZMQ.CHARSET);
        System.out.println(response);
    }
}
