package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.CampusGeofence; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;
@Repository public class CampusGeofenceRepository extends RepositorySupport {
    public CampusGeofenceRepository(JdbcTemplate j){super(j);}
    public CampusGeofence save(CampusGeofence x){update("INSERT INTO campus_geofence(id,campus_id,latitude,longitude,radius_metres,active) VALUES (?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET latitude=excluded.latitude,longitude=excluded.longitude,radius_metres=excluded.radius_metres,active=excluded.active",x.id(),x.campusId(),x.latitude(),x.longitude(),x.radiusMetres(),x.active()?1:0); return x;}
    public Optional<CampusGeofence> findActiveByCampusId(String campusId) {
        return jdbc.query("SELECT id,campus_id,latitude,longitude,radius_metres,active FROM campus_geofence WHERE campus_id = ? AND active = 1",
                (rs,n)->new CampusGeofence(rs.getString("id"),rs.getString("campus_id"),
                        new BigDecimal(rs.getString("latitude")),new BigDecimal(rs.getString("longitude")),
                        rs.getInt("radius_metres"),rs.getInt("active")==1),campusId).stream().findFirst();
    }
}
