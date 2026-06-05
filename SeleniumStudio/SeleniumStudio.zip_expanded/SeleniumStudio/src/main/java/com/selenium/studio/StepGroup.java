package com.selenium.studio;

import java.util.ArrayList;
import java.util.List;

public class StepGroup {

    private String       id;
    private String       name;
    private String       projectId;
    private List<TestStep> steps = new ArrayList<>();
    private String       createdAt;

    public StepGroup() {}

    public String         getId()        { return id; }
    public String         getName()      { return name; }
    public String         getProjectId() { return projectId; }
    public List<TestStep> getSteps()     { return steps; }
    public String         getCreatedAt() { return createdAt; }

    public void setId(String id)               { this.id = id; }
    public void setName(String name)           { this.name = name; }
    public void setProjectId(String pid)       { this.projectId = pid; }
    public void setSteps(List<TestStep> steps) { this.steps = steps; }
    public void setCreatedAt(String c)         { this.createdAt = c; }
}