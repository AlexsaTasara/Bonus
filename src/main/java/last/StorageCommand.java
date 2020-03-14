package last;

public class StorageCommand {
    private final int packageID;
    private final StorageMSG storageMSG;
    //Сохраняет Id и сообщение
    public StorageCommand(int idx, StorageMSG storageMsg) {
        this.packageID = idx;
        this.storageMSG = storageMsg;
    }
    //Возвращает Id пакета
    public int getPackageID() {
        return packageID;
    }
    //Возвращает сохраненное сообщение
    public StorageMSG getStorageMSG() {
        return storageMSG;
    }
}