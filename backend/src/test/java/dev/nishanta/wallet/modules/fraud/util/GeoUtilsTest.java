package dev.nishanta.wallet.modules.fraud.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeoUtilsTest {

    @Test
    public void testHaversineDistance() {
        // Distance between New York (40.7128, -74.0060) and London (51.5074, -0.1278)
        // is approx 5570 km.
        double distance = GeoUtils.haversineDistanceKm(40.7128, -74.0060, 51.5074, -0.1278);
        assertTrue(distance > 5500 && distance < 5650, "Distance should be approx 5570 km, was: " + distance);
    }
}
