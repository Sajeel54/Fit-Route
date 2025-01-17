package com.fyp.fitRoute.accounts.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "follows")
@Data
public class follows {

    @Id
    private String followId;
    private String following;
    private String followed;
}
