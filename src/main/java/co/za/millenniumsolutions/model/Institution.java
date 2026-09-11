package co.za.millenniumsolutions.model;

import java.util.Objects;

public final class Institution {
    private final String id;
    private final String name;

    public Institution(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String name() { return name; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Institution other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Institution[id=" + id + ", name=" + name + "]";
    }
}
