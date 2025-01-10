package com.fyp.fitRoute.posts.Services;

import com.fyp.fitRoute.inventory.Services.firebaseService;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Repositories.postRepo;
import com.fyp.fitRoute.posts.Utilities.postRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class postService {
    @Autowired
    private postRepo pRepo;
    @Autowired
    private firebaseService firebaseService;

    public posts addPost(posts newPost) throws IOException, InterruptedException {
        posts post = pRepo.save(newPost);
        List<String> imageUrls = new ArrayList<>();
/*
        for (String image : newPost.getImages()){
            imageUrls.add(
                    firebaseService.uploadImage(
                            image,
                            post.getId()+","+image
                    )
            );
        }
*/
        post.setImages(imageUrls);
        return pRepo.save(post);
    }


    public boolean deletePost(String publisherId, posts post){
        if (post.getAccountId() != publisherId)
            throw new RuntimeException("Unable to delete post");

        pRepo.delete(post);
        Optional<posts> found = pRepo.findById(post.getId());
        return found.isEmpty();
    }
}
