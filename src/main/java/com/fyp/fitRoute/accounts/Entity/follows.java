package com.fyp.fitRoute.accounts.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "follows")
@Data
public class follows {

    @Id
    private String followId;
    private String following; // user who is following
    private String followed; // user being followed
    private Date createdAt;
}
