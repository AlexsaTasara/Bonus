package last;
import javafx.util.Pair;
public class ExecuteMSG {
    private Pair<Integer, FunctionPackage> msg;
    //Возвращаем сохраненное значение msg
    public Pair<Integer, FunctionPackage> getMsg() {
        return this.msg;
    }
    //Присваиваем новое значение msg
    public ExecuteMSG(int a, FunctionPackage functionPackage) {
        this.msg = new Pair<>(a, functionPackage);
    }
}
