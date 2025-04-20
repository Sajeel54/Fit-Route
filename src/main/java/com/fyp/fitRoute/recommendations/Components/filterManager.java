package com.fyp.fitRoute.recommendations.Components;

import com.fyp.fitRoute.posts.Utilities.postResponse;
import com.fyp.fitRoute.recommendations.Utilities.Filter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class filterManager {
    private final List<Filter> filters = new ArrayList<>();
    private final List<postResponse> myPosts = new ArrayList<>();
    private String userId;
    private Date accessTimeStamp;

    public void addFilter(Filter filter){ filters.add(filter); }

    public void setTimeStamp(Date accessTimeStamp){ this.accessTimeStamp = accessTimeStamp;  }

    public void clearFilters(){ filters.clear(); }

    public void setMyId(String myId){
        this.userId = myId;
    }

    public void start(){
        List<String> ids = new ArrayList<>();
        myPosts.clear();
        for (Filter filter: filters){
            filter.setTimeStamp(accessTimeStamp);
            filter.addPostIds(ids);
            myPosts.addAll( filter.getPosts(userId) );
            ids = filter.getSentPosts();
        }
    }

    public List<postResponse> getPosts(){ return this.myPosts; }
}
