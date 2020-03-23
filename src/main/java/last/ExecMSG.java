package last;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.util.Pair;
public class ExecMSG {
    private final  static String FUNCTION_PACKAGE = "value";
    private final  static String KEY = "key";
    private final FunctionPackage fp;
    private final int key;
    //Присваиваем новое значение msg
    @JsonCreator
    public ExecMSG(@JsonProperty(KEY) int a,
                      @JsonProperty(FUNCTION_PACKAGE)FunctionPackage functionPackage) {
        this.key = a;
        this.fp = functionPackage;
    }
    //Возвращаем сохраненное значение msg
    public int getKey() {
        return this.key;
    }
    public FunctionPackage getValue() {
        return this.fp;
    }
}
