package co.za.millenniumsolutions.service;

import java.math.BigDecimal;

import java.util.Objects;

public final class WeeklyProgress {
    private final BigDecimal targetHours;
    private final BigDecimal loggedHours;
    private final BigDecimal verifiedHours;
    private final BigDecimal pendingHours;
    private final BigDecimal remainingHours;
    private final BigDecimal percentage;
    private final ProgressStatus status;

    public WeeklyProgress(BigDecimal targetHours, BigDecimal loggedHours, BigDecimal verifiedHours, BigDecimal pendingHours, BigDecimal remainingHours, BigDecimal percentage, ProgressStatus status) {
        this.targetHours = targetHours;
        this.loggedHours = loggedHours;
        this.verifiedHours = verifiedHours;
        this.pendingHours = pendingHours;
        this.remainingHours = remainingHours;
        this.percentage = percentage;
        this.status = status;
    }

    public BigDecimal targetHours() { return targetHours; }
    public BigDecimal getTargetHours() { return targetHours; }

    public BigDecimal loggedHours() { return loggedHours; }
    public BigDecimal getLoggedHours() { return loggedHours; }

    public BigDecimal verifiedHours() { return verifiedHours; }
    public BigDecimal getVerifiedHours() { return verifiedHours; }

    public BigDecimal pendingHours() { return pendingHours; }
    public BigDecimal getPendingHours() { return pendingHours; }

    public BigDecimal remainingHours() { return remainingHours; }
    public BigDecimal getRemainingHours() { return remainingHours; }

    public BigDecimal percentage() { return percentage; }
    public BigDecimal getPercentage() { return percentage; }

    public ProgressStatus status() { return status; }
    public ProgressStatus getStatus() { return status; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WeeklyProgress other)) return false;
        return Objects.equals(targetHours, other.targetHours) && Objects.equals(loggedHours, other.loggedHours) && Objects.equals(verifiedHours, other.verifiedHours) && Objects.equals(pendingHours, other.pendingHours) && Objects.equals(remainingHours, other.remainingHours) && Objects.equals(percentage, other.percentage) && Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetHours, loggedHours, verifiedHours, pendingHours, remainingHours, percentage, status);
    }

    @Override
    public String toString() {
        return "WeeklyProgress[targetHours=" + targetHours + ", loggedHours=" + loggedHours + ", verifiedHours=" + verifiedHours + ", pendingHours=" + pendingHours + ", remainingHours=" + remainingHours + ", percentage=" + percentage + ", status=" + status + "]";
    }
}
