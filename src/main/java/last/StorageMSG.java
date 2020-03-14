package last;

public class StorageMSG {
    private String checker;
    private final String result;
    private final Object[] param;
    private final String testName;
    private final String expectedResult;
    //Присваиваем значения
    public StorageMSG(String result, String expectedResult, String checker, Object[] param, String testName) {
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
