package co.za.millenniumsolutions.model;

import java.util.Objects;

public final class WorkRole {
    private final String id;
    private final String code;
    private final String name;

    public WorkRole(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String code() { return code; }
    public String getCode() { return code; }

    public String name() { return name; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WorkRole other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(code, other.code) && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name);
    }

    @Override
    public String toString() {
        return "WorkRole[id=" + id + ", code=" + code + ", name=" + name + "]";
    }
}
