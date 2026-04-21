package com.guardian.circle.sensors;

public final class FallAnalyzer {

    public enum EventType {
        NONE,
        IMPACT_DETECTED,
        FALL_CONFIRMED,
        IMPACT_DISMISSED
    }

    public static final class AnalysisResult {
        private static final AnalysisResult NONE_RESULT = new AnalysisResult(EventType.NONE, 0.0f);

        private final EventType eventType;
        private final float peakGForce;

        private AnalysisResult(EventType eventType, float peakGForce) {
            this.eventType = eventType;
            this.peakGForce = peakGForce;
        }

        public static AnalysisResult none() {
            return NONE_RESULT;
        }

        public static AnalysisResult impact(float peakGForce) {
            return new AnalysisResult(EventType.IMPACT_DETECTED, peakGForce);
        }

        public static AnalysisResult fall(float peakGForce) {
            return new AnalysisResult(EventType.FALL_CONFIRMED, peakGForce);
        }

        public static AnalysisResult dismissed(float peakGForce) {
            return new AnalysisResult(EventType.IMPACT_DISMISSED, peakGForce);
        }

        public EventType getEventType() {
            return eventType;
        }

        public float getPeakGForce() {
            return peakGForce;
        }
    }

    private final float impactThresholdG;
    private final float stillnessDeltaG;
    private final float countdownCancelDeltaG;
    private final long stillnessWindowMs;
    private final long impactWindowMs;

    private boolean impactPending;
    private long impactDetectedAtMs;
    private long stillnessStartedAtMs;
    private float peakGForce;

    public FallAnalyzer(
            float impactThresholdG,
            float stillnessDeltaG,
            float countdownCancelDeltaG,
            long stillnessWindowMs,
            long impactWindowMs
    ) {
        this.impactThresholdG = impactThresholdG;
        this.stillnessDeltaG = stillnessDeltaG;
        this.countdownCancelDeltaG = countdownCancelDeltaG;
        this.stillnessWindowMs = stillnessWindowMs;
        this.impactWindowMs = impactWindowMs;
    }

    public AnalysisResult evaluate(float gForce, long timestampMs) {
        if (!impactPending) {
            if (gForce >= impactThresholdG) {
                impactPending = true;
                impactDetectedAtMs = timestampMs;
                stillnessStartedAtMs = 0L;
                peakGForce = gForce;
                return AnalysisResult.impact(peakGForce);
            }
            return AnalysisResult.none();
        }

        peakGForce = Math.max(peakGForce, gForce);
        float motionDelta = Math.abs(gForce - 1.0f);

        if (motionDelta <= stillnessDeltaG) {
            if (stillnessStartedAtMs == 0L) {
                stillnessStartedAtMs = timestampMs;
            }
            if (timestampMs - stillnessStartedAtMs >= stillnessWindowMs) {
                float resultPeak = peakGForce;
                reset();
                return AnalysisResult.fall(resultPeak);
            }
        } else {
            stillnessStartedAtMs = 0L;
        }

        if (timestampMs - impactDetectedAtMs > impactWindowMs) {
            float resultPeak = peakGForce;
            reset();
            return AnalysisResult.dismissed(resultPeak);
        }

        return AnalysisResult.none();
    }

    public boolean shouldCancelCountdown(float gForce) {
        return Math.abs(gForce - 1.0f) > countdownCancelDeltaG;
    }

    public void reset() {
        impactPending = false;
        impactDetectedAtMs = 0L;
        stillnessStartedAtMs = 0L;
        peakGForce = 0.0f;
    }
}
