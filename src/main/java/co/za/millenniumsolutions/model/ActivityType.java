package co.za.millenniumsolutions.model;

import java.util.Objects;

public final class ActivityType {
    private final String id;
    private final String name;
    private final boolean active;

    public ActivityType(String id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String name() { return name; }
    public String getName() { return name; }

    public boolean active() { return active; }
    public boolean isActive() { return active; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ActivityType other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(name, other.name) && active == other.active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, active);
    }

    @Override
    public String toString() {
        return "ActivityType[id=" + id + ", name=" + name + ", active=" + active + "]";
    }
}
