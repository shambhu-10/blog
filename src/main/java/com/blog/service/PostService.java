package com.blog.service;

import com.blog.dtos.CategoryRequest;
import com.blog.dtos.CategoryResponse;
import com.blog.dtos.PostRequest;
import com.blog.dtos.PostResponse;
import com.blog.entity.Category;
import com.blog.entity.Post;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.CategoryRepository;
import com.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.PrimitiveIterator;
import java.util.Set;

@Service
@RequiredArgsConstructor            // @AllArgsConstructor, @NoArgsConstructor, @RequiredArgsConstructor(only for final & @NonNull fields)
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    /**
     * creates a new post with categories
     */
    public PostResponse createPost(PostRequest request) {
        log.debug("Creating post with title: {}", request.getTitle());

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        // fetch categories with IDs
        Set<Category> categories = fetchCategoriesByIds(request.getCategoryIds());
        post.setCategories(categories);

        Post savedPost = postRepository.save(post);
        log.info("Post created successfully with id: {}", savedPost.getId());

        return convertToResponse(savedPost);
    }


    /**
     * Helper methods
     * 1. fetch categories by Ids
     * 2. Post entity to postResponse dto
     */

    private Set<Category> fetchCategoriesByIds(Set<Long> categoryIds) {
        Set<Category> categories = new HashSet<>();

        for(Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> {
                        log.error("Category not found with id: {}", categoryId);
                        return ResourceNotFoundException.forId("Category", categoryId);
                    });
            categories.add(category);
        }

        log.debug("Fetched {} categories", categories.size());
        return categories;
    }

    private PostResponse convertToResponse(Post savedPost) {

    }
}
