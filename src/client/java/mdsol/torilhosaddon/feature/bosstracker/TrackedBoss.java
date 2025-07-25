package mdsol.torilhosaddon.feature.bosstracker;

import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TrackedBoss {

    private final BossData data;
    private @Nullable String calledPlayerName;
    private State state;
    private int portalTimer = 0;
    private double distanceMarkerValue = 0;

    public TrackedBoss(BossData data, State state) {
        this.data = data;
        this.state = state;
    }

    public BossData getData() {
        return data;
    }

    public Optional<String> getCalledPlayerName() {
        return Optional.ofNullable(calledPlayerName);
    }

    public void setCalledPlayerName(@Nullable String calledPlayerName) {
        this.calledPlayerName = calledPlayerName;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        if (!Objects.equals(this.state, state)) {
            this.state = state;
        }
    }

    public int getPortalTimer() {
        return portalTimer;
    }

    public void resetPortalTimer() {
        portalTimer = 30 * 20;
    }

    public int decrementPortalTimer() {
        portalTimer -= 1;
        return portalTimer;
    }

    public double getDistanceMarkerValue() {
        return distanceMarkerValue;
    }

    public void setDistanceMarkerValue(double distanceMarkerValue) {
        if (!Objects.equals(this.distanceMarkerValue, distanceMarkerValue)) {
            this.distanceMarkerValue = distanceMarkerValue;
        }
    }

    public enum State {
        ALIVE,
        DEFEATED_PORTAL_ACTIVE,
        DEFEATED
    }
}
