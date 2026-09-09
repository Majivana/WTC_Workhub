package co.za.millenniumsolutions.model;

import java.math.BigDecimal;

public record CampusGeofence(String id, String campusId, BigDecimal latitude, BigDecimal longitude,
                             int radiusMetres, boolean active) {}
