package com.dreamcast.automation.model;

public class TestStep {

    private String name;
    private String action;
    private String xpath;
    private String value;
    private int    waitSeconds;

    public TestStep() {}

    public TestStep(String name, String action, String xpath, String value, int waitSeconds) {
        this.name        = name;
        this.action      = action;
        this.xpath       = xpath;
        this.value       = value;
        this.waitSeconds = waitSeconds;
    }

    public String getName()        { return name; }
    public String getAction()      { return action; }
    public String getXpath()       { return xpath; }
    public String getValue()       { return value; }
    public int    getWaitSeconds() { return waitSeconds; }

    public void setName(String name)              { this.name = name; }
    public void setAction(String action)          { this.action = action; }
    public void setXpath(String xpath)            { this.xpath = xpath; }
    public void setValue(String value)            { this.value = value; }
    public void setWaitSeconds(int waitSeconds)   { this.waitSeconds = waitSeconds; }

    @Override
    public String toString() {
        return "[" + action + "] " + name;
    }
}
