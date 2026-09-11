package co.za.millenniumsolutions.api;

import java.util.Objects;

public class ActivityTypeRequest {
    private String name;
    private Boolean active;

    public ActivityTypeRequest() {
    }

    public ActivityTypeRequest(String name, Boolean active) {
        this.name = name;
        this.active = active;
    }

    public String name() { return name; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean active() { return active; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ActivityTypeRequest other)) return false;
        return Objects.equals(name, other.name) && Objects.equals(active, other.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, active);
    }

    @Override
    public String toString() {
        return "ActivityTypeRequest[name=" + name + ", active=" + active + "]";
    }
}
