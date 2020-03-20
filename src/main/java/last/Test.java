package last;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Test {
    private static final String PARAMS = "params", TEST_NAME = "name", EMPTY_RESULT = "NONE";
    private static final String EMPTY_CHECKER = "NOT READY YET", EXPECTED_RESULT = "expectedResult";
    private final Object[] params;
    private String result, checker;
    private final String testName, expectedResult;

    //Присваиваем параметры для тела тест
    @JsonCreator
    public Test(@JsonProperty(TEST_NAME) String testname,//Имя
                @JsonProperty(EXPECTED_RESULT) String expectedResult,//Ожидаемый результат
                @JsonProperty(PARAMS) Object[] params) {//Параметры
        this.params = params; //Параметры
        this.testName = testname;//Имя
        this.result = EMPTY_RESULT; //Полученный результат
        this.checker = EMPTY_CHECKER; //Результат проверки
        this.expectedResult = expectedResult;//Ожидаемый результат
    }
    //Возвращает результат
    public String getResult() {
        return result;
    }
    //Возвращает параметры
    public Object[] getParams() {
        return params;
    }
    //Возвращает результат проверки
    public String getChecker() {
        return checker;
    }
    //Возвращает имя тетса
    public String getTestName() {
        return testName;
    }
    //Возвращает ожидаемый ркзультат
    public String getExpectedResult() {
        return expectedResult;
    }
}