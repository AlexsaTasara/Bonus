package last;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SMSG {
    private String checker;
    private final String result;
    private final Object[] param;
    private final String testName;
    private final String expectedResult;
    private final static String CHECKER = "checker", RESULT = "result", TEST_NAME = "testName", EXPECTED_RESULT = "expectedResult", PARAM = "param";
    //Присваиваем значения
    @JsonCreator
    public SMSG(@JsonProperty(RESULT)String result,
                @JsonProperty(EXPECTED_RESULT)String expectedResult,
                @JsonProperty(CHECKER)String checker,
                @JsonProperty(PARAM)Object[] param,
                @JsonProperty(TEST_NAME)String testName) {
        this.param = param;//Параметры
        this.result = result;//Результат
        this.checker = checker;//Результат проверки
        this.testName = testName;//Имя
        this.expectedResult = expectedResult;//Ожидаемый результат
    }
    //Возвращает параметры
    public Object[] getParam() {
        return param;
    }
    //Возвращает результат
    public String getResult() {
        return result;
    }
    //Возвращает результат проверки
    public String getChecker() {
        return checker;
    }
    //Возвращает имя
    public String getTestName() {
        return testName;
    }
    //Возвращает ожидаемый результат
    public String getExpectedResult() {
        return expectedResult;
    }
}
