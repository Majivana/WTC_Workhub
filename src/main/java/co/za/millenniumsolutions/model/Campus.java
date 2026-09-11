package co.za.millenniumsolutions.model;

import java.util.Objects;

public final class Campus {
    private final String id;
    private final String institutionId;
    private final String name;

    public Campus(String id, String institutionId, String name) {
        this.id = id;
        this.institutionId = institutionId;
        this.name = name;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String institutionId() { return institutionId; }
    public String getInstitutionId() { return institutionId; }

    public String name() { return name; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Campus other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(institutionId, other.institutionId) && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, institutionId, name);
    }

    @Override
    public String toString() {
        return "Campus[id=" + id + ", institutionId=" + institutionId + ", name=" + name + "]";
    }
}
