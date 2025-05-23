package com._project._project.TimeCard;

import java.time.LocalDateTime;

public class DayTimeCardRequest {
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;

    public LocalDateTime getClockIn() {
        return clockIn;
    }

    public void setClockIn(LocalDateTime clockIn) {
        this.clockIn = clockIn;
    }

    public LocalDateTime getClockOut() {
        return clockOut;
    }

    public void setClockOut(LocalDateTime clockOut) {
        this.clockOut = clockOut;
    }
} 