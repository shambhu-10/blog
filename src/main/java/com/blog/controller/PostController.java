package com.blog.controller;

import com.blog.dtos.PostRequest;
import com.blog.dtos.PostResponse;
import com.blog.entity.Category;
import com.blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(PostRequest request) {
        PostResponse response = postService.createPost(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPost(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(defaultValue = "createdAt") String sortBy,
                                                         @RequestParam(defaultValue = "desc") String sortDirection) {

        // create sort object
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PostResponse> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * alternative: get all post without pagination
     * GET /api/posts/all
     */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPostsWithoutPagination() {
        List<PostResponse> responseList = postService.getAllPosts();
        return ResponseEntity.ok(responseList);
    }

    // get one post by id
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    // update post
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id,
                                                   @Valid @RequestBody PostRequest request) {
        PostResponse response = postService.updatePost(id, request);
        return ResponseEntity.ok(response);
    }

    // delete post by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // Search by keyword
    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> searchPost(@RequestParam(required = false) String keyword) {
        List<PostResponse> posts = postService.searchPosts(keyword != null ? keyword : "");
        return ResponseEntity.ok(posts);
    }

    // filter by category
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<Page<PostResponse>> getPostByCategory(@PathVariable String categoryName,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponse> posts = postService.getPostsByCategory(categoryName, pageable);
        return ResponseEntity.ok(posts);
    }
}
