package com.selenium.studio;

import java.util.ArrayList;
import java.util.List;

public class TestStep {

    private int          id;
    private String       name      = "";        // Step ka naam
    private String       action    = "click";
    private List<String> xpaths    = new ArrayList<>();  // Multiple XPath
    private String       xpath     = "";        // backward compat
    private String       value     = "";
    private String       value2    = "";
    private int          wait      = 0;
    private String       status    = "pending";
    private boolean      selected  = false;     // Step group ke liye checkbox

    public TestStep() {
        xpaths.add("");  // default ek empty xpath
    }

    // ── Getters ──
    public int          getId()       { return id; }
    public String       getName()     { return name; }
    public String       getAction()   { return action; }
    public List<String> getXpaths()   { return xpaths; }
    public String       getXpath()    {
        // pehla non-empty xpath return karo
        if (xpaths != null && !xpaths.isEmpty()) return xpaths.get(0);
        return xpath;
    }
    public String       getValue()    { return value; }
    public String       getValue2()   { return value2; }
    public int          getWait()     { return wait; }
    public String       getStatus()   { return status; }
    public boolean      isSelected()  { return selected; }

    // ── Setters ──
    public void setId(int id)               { this.id = id; }
    public void setName(String name)        { this.name = name; }
    public void setAction(String action)    { this.action = action; }
    public void setXpaths(List<String> x)  { this.xpaths = x; }
    public void setXpath(String xpath)      {
        this.xpath = xpath;
        if (xpaths == null) xpaths = new ArrayList<>();
        if (xpaths.isEmpty()) xpaths.add(xpath);
        else xpaths.set(0, xpath);
    }
    public void setValue(String value)      { this.value = value; }
    public void setValue2(String v2)        { this.value2 = v2; }
    public void setWait(int wait)           { this.wait = wait; }
    public void setStatus(String status)    { this.status = status; }
    public void setSelected(boolean s)      { this.selected = s; }
}