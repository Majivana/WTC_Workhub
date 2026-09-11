package co.za.millenniumsolutions.storage;

public enum StorageObjectType {
    EVIDENCE("evidence"),
    ATTENDANCE_SELFIE("attendance-selfies");

    private final String namespace;

    StorageObjectType(String namespace) {
        this.namespace = namespace;
    }

    public String namespace() {
        return namespace;
    }
}
