package com._project._project.ReportGenerator;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public record ReportGraphData(
        LocalDate date,
        long income,
        long expenses,
        long revenue) {}
