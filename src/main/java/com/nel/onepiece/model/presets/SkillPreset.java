package com.nel.onepiece.model.presets;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SkillPreset {

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("skillMarkdown")
    private String skillMarkdown;

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSkillMarkdown() {
        return skillMarkdown;
    }

    public void setSkillMarkdown(String skillMarkdown) {
        this.skillMarkdown = skillMarkdown;
    }
}

