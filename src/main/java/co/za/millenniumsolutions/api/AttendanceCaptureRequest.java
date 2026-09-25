package co.za.millenniumsolutions.api;

import java.math.BigDecimal;

public class AttendanceCaptureRequest {
    private String userId;
    private String campusId;
    private String workPeriodId;
    private String mediaType;
    private long sizeBytes;
    private String checksum;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public AttendanceCaptureRequest() {}
    public String userId(){return userId;} public void setUserId(String x){userId=x;}
    public String campusId(){return campusId;} public void setCampusId(String x){campusId=x;}
    public String workPeriodId(){return workPeriodId;} public void setWorkPeriodId(String x){workPeriodId=x;}
    public String mediaType(){return mediaType;} public void setMediaType(String x){mediaType=x;}
    public long sizeBytes(){return sizeBytes;} public void setSizeBytes(long x){sizeBytes=x;}
    public String checksum(){return checksum;} public void setChecksum(String x){checksum=x;}
    public BigDecimal latitude(){return latitude;} public void setLatitude(BigDecimal x){latitude=x;}
    public BigDecimal longitude(){return longitude;} public void setLongitude(BigDecimal x){longitude=x;}
}
