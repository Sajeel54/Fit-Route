package com.fyp.fitRoute.posts.Utilities;

import com.fyp.fitRoute.posts.Entity.coordinates;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class postRequest {
    private String title;
    private String description;
    private List<String> tags;
    private List<String> images;
    @NotNull
    private double distance;
    @NotNull
    private double time;
    private List<coordinates> coordinates;
    @NotNull
    private String category;
}
