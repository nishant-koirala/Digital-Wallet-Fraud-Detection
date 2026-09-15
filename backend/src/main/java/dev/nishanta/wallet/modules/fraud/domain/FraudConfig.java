package dev.nishanta.wallet.modules.fraud.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "fraud_config")
public class FraudConfig {

    @Id
    private Integer id = 1; // Singleton

    private BigDecimal coldStartThreshold;
    private int minHistoryForBaseline;
    private BigDecimal averageMultiplier;
    private double maxGeoDistanceKm;

    public FraudConfig() {}

    public FraudConfig(BigDecimal coldStartThreshold, int minHistoryForBaseline, BigDecimal averageMultiplier, double maxGeoDistanceKm) {
        this.coldStartThreshold = coldStartThreshold;
        this.minHistoryForBaseline = minHistoryForBaseline;
        this.averageMultiplier = averageMultiplier;
        this.maxGeoDistanceKm = maxGeoDistanceKm;
    }

    public Integer getId() {
        return id;
    }

    public BigDecimal getColdStartThreshold() {
        return coldStartThreshold;
    }

    public void setColdStartThreshold(BigDecimal coldStartThreshold) {
        this.coldStartThreshold = coldStartThreshold;
    }

    public int getMinHistoryForBaseline() {
        return minHistoryForBaseline;
    }

    public void setMinHistoryForBaseline(int minHistoryForBaseline) {
        this.minHistoryForBaseline = minHistoryForBaseline;
    }

    public BigDecimal getAverageMultiplier() {
        return averageMultiplier;
    }

    public void setAverageMultiplier(BigDecimal averageMultiplier) {
        this.averageMultiplier = averageMultiplier;
    }

    public double getMaxGeoDistanceKm() {
        return maxGeoDistanceKm;
    }

    public void setMaxGeoDistanceKm(double maxGeoDistanceKm) {
        this.maxGeoDistanceKm = maxGeoDistanceKm;
    }
}
