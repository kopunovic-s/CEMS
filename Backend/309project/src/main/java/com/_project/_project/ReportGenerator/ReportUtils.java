package com._project._project.ReportGenerator;

import com._project._project.Project.SalesData.SalesData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ReportUtils {

    public static JFreeChart createSalesChart(List<SalesData> salesData) {
        TimeSeries income = new TimeSeries("Income");
        TimeSeries expense = new TimeSeries("Expense");
        TimeSeries revenue = new TimeSeries("Revenue");

        for (SalesData data : salesData) {
            Day day = new Day(Date.from(data.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            income.add(day, data.getIncome());
            expense.add(day, data.getExpenses());
            revenue.add(day, data.getRevenue());
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(income);
        dataset.addSeries(expense);
        dataset.addSeries(revenue);

        return ChartFactory.createTimeSeriesChart(
                "Sales Report",
                "Date",
                "Amount",
                dataset,
                true,
                true,
                false);
    }

    public static JFreeChart createRevenueChart(List<SalesData> salesData) {
        TimeSeries revenue = new TimeSeries("Revenue");

        for (SalesData data : salesData) {
            Day day = new Day(Date.from(data.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            revenue.add(day, data.getRevenue());
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(revenue);

        return ChartFactory.createTimeSeriesChart(
                "Revenue Report",
                "Date",
                "Amount",
                dataset,
                true,
                true,
                false);
    }

    public static JFreeChart createGraphDataChart(List<ReportGraphData> graphData) {
        TimeSeries income = new TimeSeries("Income");
        TimeSeries expense = new TimeSeries("Expense");
        TimeSeries revenue = new TimeSeries("Revenue");

        for (ReportGraphData data : graphData) {
            Day day = new Day(Date.from(data.date().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            income.add(day, data.income());
            expense.add(day, data.expenses());
            revenue.add(day, data.revenue());
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(income);
        dataset.addSeries(expense);
        dataset.addSeries(revenue);

        return ChartFactory.createTimeSeriesChart(
                "Sales Report",
                "Date",
                "Amount",
                dataset,
                true,
                true,
                false);
    }

    public static JFreeChart createGraphDataRevenueChart(List<ReportGraphData> graphData) {
        TimeSeries revenue = new TimeSeries("Revenue");

        for (ReportGraphData data : graphData) {
            Day day = new Day(Date.from(data.date().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            revenue.add(day, data.revenue());
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(revenue);

        return ChartFactory.createTimeSeriesChart(
                "Revenue Report",
                "Date",
                "Amount",
                dataset,
                true,
                true,
                false);
    }
}
