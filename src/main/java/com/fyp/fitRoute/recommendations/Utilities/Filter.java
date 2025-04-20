package com.fyp.fitRoute.recommendations.Utilities;

import com.fyp.fitRoute.posts.Utilities.postResponse;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public interface Filter {
    public List<String> getSentPosts();
    public abstract void addPostIds(List<String> postIds);
    public void setTimeStamp(Date accessTimeStamp);
    public List<postResponse> getPosts(String myId);
}
