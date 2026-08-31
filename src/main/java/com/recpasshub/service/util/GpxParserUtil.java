package com.recpasshub.service.util;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import io.jenetics.jpx.geom.Geoid;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class GpxParserUtil {

    public record GpxMetadata(Double distance, Double elevation, String location) {}

    public static GpxMetadata parseGpxMetadata(InputStream gpxStream) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("map-", ".gpx");
            Files.copy(gpxStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            GPX gpx = GPX.read(tempFile);

            double totalDistance = 0.0;
            double totalElevation = 0.0;
            String location = null;

            for (Track track : gpx.getTracks()) {
                for (TrackSegment segment : track.getSegments()) {
                    if (location == null && !segment.getPoints().isEmpty()) {
                        WayPoint firstPoint = segment.getPoints().get(0);
                        location = firstPoint.getLatitude().doubleValue() + "," + firstPoint.getLongitude().doubleValue();
                    }

                    totalDistance += segment.getPoints().stream().collect(Geoid.WGS84.toPathLength()).doubleValue();

                    // Calculate elevation gain
                    Double prevElevation = null;
                    for (WayPoint wp : segment.getPoints()) {
                        if (wp.getElevation().isPresent()) {
                            double currentElevation = wp.getElevation().orElseThrow().doubleValue();
                            if (prevElevation != null && currentElevation > prevElevation) {
                                totalElevation += (currentElevation - prevElevation);
                            }
                            prevElevation = currentElevation;
                        }
                    }
                }
            }

            return new GpxMetadata(totalDistance, totalElevation, location);
        } catch (Exception e) {
            // Log error, or throw exception. Returning nulls for now.
            return new GpxMetadata(null, null, null);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignored) {}
            }
        }
    }
}
