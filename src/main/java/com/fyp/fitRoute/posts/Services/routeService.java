package com.fyp.fitRoute.posts.Services;

import com.fyp.fitRoute.posts.Entity.route;
import com.fyp.fitRoute.posts.Repositories.routeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class routeService {
    @Autowired
    private routeRepo rRepo;

    public route add(route newRoute){
        return rRepo.save(newRoute);
    }


    public boolean deletePost(route route){
        rRepo.delete(route);
        Optional<route> found = rRepo.findById(route.getId());
        return found.isPresent();
    }
}
