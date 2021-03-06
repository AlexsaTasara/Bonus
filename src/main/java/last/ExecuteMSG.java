package last;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.util.Pair;
public class ExecuteMSG {
    private final  static String FUNCTION_PACKAGE = "value";
    private final  static String KEY = "key";
    private final  static String MSG = "msg";
    Pair<Integer, FunctionPackage> msg;
    private final FunctionPackage fp;
    private final int key;
    //Присваиваем новое значение msg
    @JsonCreator
    public ExecuteMSG(int a,
                      FunctionPackage functionPackage) {
        this.key = a;
        this.fp = functionPackage;
        this.msg = new Pair<>(key, fp);
    }
    //Возвращаем сохраненное значение msg
    public Pair<Integer, FunctionPackage> getMsg() {
        return this.msg;
    }
}
