package io.blaze.server.model;

public final class EndpointStatistics {

    private int numberOfRequests;
    private int numberOfSuccess;
    private int numberOfFailures;
    private double averageResponseTimeInMilliseconds;
    private double minResponseTimeInMilliseconds = Double.MAX_VALUE;
    private double maxResponseTimeInMilliseconds = Double.MIN_VALUE;

    public int getNumberOfRequests() {
        return numberOfRequests;
    }

    private EndpointStatistics addNumberOfRequests(final int numberOfRequests) {
        this.numberOfRequests += numberOfRequests;
        return this;
    }

    public int getNumberOfSuccess() {
        return numberOfSuccess;
    }

    public EndpointStatistics addNumberOfSuccess(final int numberOfSuccess) {
        this.numberOfSuccess += numberOfSuccess;
        return addNumberOfRequests(numberOfSuccess);
    }

    public int getNumberOfFailures() {
        return numberOfFailures;
    }

    public EndpointStatistics addNumberOfFailures(final int numberOfFailures) {
        this.numberOfFailures += numberOfFailures;
        return addNumberOfRequests(numberOfFailures);
    }

    public double getAverageResponseTimeInMilliseconds() {
        return averageResponseTimeInMilliseconds;
    }

    public EndpointStatistics addResponseTime(final long responseTime) {
        averageResponseTimeInMilliseconds = (averageResponseTimeInMilliseconds + responseTime) / 2;
        if (getMinResponseTimeInMilliseconds() > responseTime) {
            setMinResponseTimeInMilliseconds(responseTime);
        }
        if (getMaxResponseTimeInMilliseconds() < responseTime) {
            setMaxResponseTimeInMilliseconds(responseTime);
        }
        return this;
    }

    public double getMinResponseTimeInMilliseconds() {
        return minResponseTimeInMilliseconds;
    }

    private void setMinResponseTimeInMilliseconds(double minResponseTimeInMilliseconds) {
        this.minResponseTimeInMilliseconds = minResponseTimeInMilliseconds;
    }

    public double getMaxResponseTimeInMilliseconds() {
        return maxResponseTimeInMilliseconds;
    }

    private void setMaxResponseTimeInMilliseconds(double maxResponseTimeInMilliseconds) {
        this.maxResponseTimeInMilliseconds = maxResponseTimeInMilliseconds;
    }
}
