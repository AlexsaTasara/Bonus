package last;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FunctionPackage {
    private final static String FUNCTION_NAME = "functionName";
    private final static String PACKAGE_ID = "packageId", JS_SCRIPT = "jsScript", TESTS = "tests";
    private final Test[] tests;
    private final int packageId;
    private final String jsScript, functionName;

    @JsonCreator
    public FunctionPackage(@JsonProperty(PACKAGE_ID) String packageId,
                           @JsonProperty(JS_SCRIPT) String jsScript,
                           @JsonProperty(FUNCTION_NAME) String functionName,
                           @JsonProperty(TESTS) Test[] tests) {
        this.tests = tests;
        this.jsScript = jsScript;
        this.functionName = functionName;
        this.packageId = Integer.parseInt(packageId);
    }
    //Возвращаем массив тестов
    public Test[] getTests() {
        return tests;
    }
    //Возвращаем ID пакета
    public int getPackageId() {
        return packageId;
    }
    //Возвращаем конкретный тест
    public Test getTest(int i) {
        return tests[i];
    }
    //Возвращаем JS код
    public String getJSScript() {
        return jsScript;
    }
    //Возвращаем имя функции
    public String getFunctionName() {
        return functionName;
    }
}
