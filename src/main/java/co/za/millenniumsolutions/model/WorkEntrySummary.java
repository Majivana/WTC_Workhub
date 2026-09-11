package co.za.millenniumsolutions.model;

import java.util.Objects;

public final class WorkEntrySummary {
    private final long loggedMinutes;
    private final long verifiedMinutes;

    public WorkEntrySummary(long loggedMinutes, long verifiedMinutes) {
        this.loggedMinutes = loggedMinutes;
        this.verifiedMinutes = verifiedMinutes;
    }

    public long loggedMinutes() { return loggedMinutes; }
    public long getLoggedMinutes() { return loggedMinutes; }

    public long verifiedMinutes() { return verifiedMinutes; }
    public long getVerifiedMinutes() { return verifiedMinutes; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WorkEntrySummary other)) return false;
        return loggedMinutes == other.loggedMinutes && verifiedMinutes == other.verifiedMinutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(loggedMinutes, verifiedMinutes);
    }

    @Override
    public String toString() {
        return "WorkEntrySummary[loggedMinutes=" + loggedMinutes + ", verifiedMinutes=" + verifiedMinutes + "]";
    }
}
