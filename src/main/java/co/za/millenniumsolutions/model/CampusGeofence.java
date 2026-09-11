package co.za.millenniumsolutions.model;

import java.math.BigDecimal;

import java.util.Objects;

public final class CampusGeofence {
    private final String id;
    private final String campusId;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final int radiusMetres;
    private final boolean active;

    public CampusGeofence(String id, String campusId, BigDecimal latitude, BigDecimal longitude, int radiusMetres, boolean active) {
        this.id = id;
        this.campusId = campusId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMetres = radiusMetres;
        this.active = active;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String campusId() { return campusId; }
    public String getCampusId() { return campusId; }

    public BigDecimal latitude() { return latitude; }
    public BigDecimal getLatitude() { return latitude; }

    public BigDecimal longitude() { return longitude; }
    public BigDecimal getLongitude() { return longitude; }

    public int radiusMetres() { return radiusMetres; }
    public int getRadiusMetres() { return radiusMetres; }

    public boolean active() { return active; }
    public boolean isActive() { return active; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CampusGeofence other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(campusId, other.campusId) && Objects.equals(latitude, other.latitude) && Objects.equals(longitude, other.longitude) && radiusMetres == other.radiusMetres && active == other.active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, campusId, latitude, longitude, radiusMetres, active);
    }

    @Override
    public String toString() {
        return "CampusGeofence[id=" + id + ", campusId=" + campusId + ", latitude=" + latitude + ", longitude=" + longitude + ", radiusMetres=" + radiusMetres + ", active=" + active + "]";
    }
}
