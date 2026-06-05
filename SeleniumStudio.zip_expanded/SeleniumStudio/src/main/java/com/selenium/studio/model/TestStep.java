package com.selenium.studio.model;

public class TestStep {

    private int    id;
    private String action  = "click";
    private String xpath   = "";
    private String value   = "";
    private String value2  = "";
    private int    wait    = 0;
    private String status  = "pending"; // pending | pass | fail | run

    public TestStep() {}

    public int    getId()      { return id; }
    public String getAction()  { return action; }
    public String getXpath()   { return xpath; }
    public String getValue()   { return value; }
    public String getValue2()  { return value2; }
    public int    getWait()    { return wait; }
    public String getStatus()  { return status; }

    public void setId(int id)            { this.id = id; }
    public void setAction(String action) { this.action = action; }
    public void setXpath(String xpath)   { this.xpath = xpath; }
    public void setValue(String value)   { this.value = value; }
    public void setValue2(String v2)     { this.value2 = v2; }
    public void setWait(int wait)        { this.wait = wait; }
    public void setStatus(String status) { this.status = status; }
}
