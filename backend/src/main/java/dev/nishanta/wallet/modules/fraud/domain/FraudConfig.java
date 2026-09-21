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

    private int velocityWindowMinutes;
    private int velocityLookbackWindows;
    private int velocityColdStartMax;
    private double velocityMultiplier;

    public FraudConfig() {}

    public FraudConfig(BigDecimal coldStartThreshold, int minHistoryForBaseline, BigDecimal averageMultiplier, double maxGeoDistanceKm,
                       int velocityWindowMinutes, int velocityLookbackWindows, int velocityColdStartMax, double velocityMultiplier) {
        this.coldStartThreshold = coldStartThreshold;
        this.minHistoryForBaseline = minHistoryForBaseline;
        this.averageMultiplier = averageMultiplier;
        this.maxGeoDistanceKm = maxGeoDistanceKm;
        this.velocityWindowMinutes = velocityWindowMinutes;
        this.velocityLookbackWindows = velocityLookbackWindows;
        this.velocityColdStartMax = velocityColdStartMax;
        this.velocityMultiplier = velocityMultiplier;
    }

    public static FraudConfig createDefault() {
        return new FraudConfig(
                new BigDecimal("50000"), // coldStartThreshold
                5, // minHistoryForBaseline
                new BigDecimal("5"), // averageMultiplier
                500.0, // maxGeoDistanceKm
                10, // velocityWindowMinutes
                6, // velocityLookbackWindows
                5, // velocityColdStartMax
                3.0 // velocityMultiplier
        );
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

    public int getVelocityWindowMinutes() {
        return velocityWindowMinutes;
    }

    public void setVelocityWindowMinutes(int velocityWindowMinutes) {
        this.velocityWindowMinutes = velocityWindowMinutes;
    }

    public int getVelocityLookbackWindows() {
        return velocityLookbackWindows;
    }

    public void setVelocityLookbackWindows(int velocityLookbackWindows) {
        this.velocityLookbackWindows = velocityLookbackWindows;
    }

    public int getVelocityColdStartMax() {
        return velocityColdStartMax;
    }

    public void setVelocityColdStartMax(int velocityColdStartMax) {
        this.velocityColdStartMax = velocityColdStartMax;
    }

    public double getVelocityMultiplier() {
        return velocityMultiplier;
    }

    public void setVelocityMultiplier(double velocityMultiplier) {
        this.velocityMultiplier = velocityMultiplier;
    }
}
