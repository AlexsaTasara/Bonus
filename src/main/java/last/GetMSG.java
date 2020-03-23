package last;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GetMSG {
    private int packageId;
    private final static String PACKAGE_ID = "packageId";
    //Присваиваем Id пакета
    @JsonCreator
    public GetMSG(@JsonProperty(PACKAGE_ID) int packageId) {
        this.packageId = packageId;
    }
    //Возвращаем Id пакета
    public int getPackageId() {
        return packageId;
    }
}
