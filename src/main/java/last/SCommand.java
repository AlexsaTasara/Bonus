package last;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SCommand {
    private final int packageID;
    private final SMSG storageMSG;
    private final static String PACKAGE_ID = "packageId", STORAGE_MSG = "storageMSG";

    //Сохраняет Id и сообщение
    @JsonCreator
    public SCommand(@JsonProperty(PACKAGE_ID)int idx, @JsonProperty(STORAGE_MSG)SMSG storageMsg) {
        this.packageID = idx;
        this.storageMSG = storageMsg;
    }
    //Возвращает Id пакета
    public int getPackageID() {
        return packageID;
    }
    //Возвращает сохраненное сообщение
    public SMSG getStorageMSG() {
        return storageMSG;
    }
}