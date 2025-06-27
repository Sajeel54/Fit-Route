package com.fyp.fitRoute.progress.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "progress")
@Data
public class progress {
    @Id
    private String id;
    private String userId;
    //7 days in a week every day stores distance in that day
    private double[] dailyDistance;
}
