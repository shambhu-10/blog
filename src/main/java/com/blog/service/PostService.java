package com.blog.service;

import com.blog.dtos.*;
import com.blog.entity.Category;
import com.blog.entity.Post;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.CategoryRepository;
import com.blog.dtos.CategoryResponse;
import com.blog.dtos.CommentResponse;
import com.blog.repository.PostRepository;
import com.blog.dtos.PostRequest;
import com.blog.dtos.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Create a new post with categories
     *
     * This demonstrates:
     * 1. Fetching related entities (categories)
     * 2. Setting up Many-to-Many relationship
     * 3. Cascade save (categories are saved with post)
     */
    @Transactional
    public PostResponse createPost(PostRequest request) {
        log.debug("Creating post with title: {}", request.getTitle());

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        // Fetch categories by IDs
        Set<Category> categories = fetchCategoriesByIds(request.getCategoryIds());
        post.setCategories(categories);

        Post savedPost = postRepository.save(post);
        log.info("Post created successfully with id: {}", savedPost.getId());

        return convertToResponse(savedPost);
    }

    /**
     * Get all posts with pagination
     *
     * Returns Page<PostResponse> which contains:
     * - List of posts for current page
     * - Total number of posts
     * - Total number of pages
     * - Current page number
     * - etc.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        log.debug("Fetching posts with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Post> postPage = postRepository.findAll(pageable);
        log.info("Found {} posts in total, {} pages",
                postPage.getTotalElements(), postPage.getTotalPages());

        // Convert Page<Post> to Page<PostResponse>
        return postPage.map(this::convertToResponse);
    }

    /**
     * Get all posts without pagination (for simple listing)
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        log.debug("Fetching all posts without pagination");

        // Use findAllWithCategories to avoid N+1 problem
        List<Post> posts = postRepository.findAllWithCategories();
        log.info("Found {} posts", posts.size());

        return posts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get post by ID
     */
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        log.debug("Fetching post with id: {}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Post not found with id: {}", id);
                    return ResourceNotFoundException.forId("Post", id);
                });

        return convertToResponse(post);
    }

    /**
     * Update post
     *
     * Note: We update categories too - remove old, add new
     */
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        log.debug("Updating post with id: {}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Post", id));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        // Update categories
        // Clear existing categories and add new ones
        post.getCategories().clear();
        Set<Category> newCategories = fetchCategoriesByIds(request.getCategoryIds());
        post.getCategories().addAll(newCategories);

        Post updatedPost = postRepository.save(post);
        log.info("Post updated successfully with id: {}", id);

        return convertToResponse(updatedPost);
    }

    /**
     * Delete post
     *
     * Due to cascade = CascadeType.ALL on comments relationship,
     * all comments are automatically deleted when post is deleted
     */
    @Transactional
    public void deletePost(Long id) {
        log.debug("Deleting post with id: {}", id);

        if (!postRepository.existsById(id)) {
            log.error("Cannot delete: Post not found with id: {}", id);
            throw ResourceNotFoundException.forId("Post", id);
        }

        postRepository.deleteById(id);
        log.info("Post deleted successfully with id: {} (including all comments)", id);
    }

    /**
     * Search posts by keyword
     */
    @Transactional(readOnly = true)
    public List<PostResponse> searchPosts(String keyword) {
        log.debug("Searching posts with keyword: {}", keyword);

        List<Post> posts = postRepository.searchPosts(keyword);
        log.info("Found {} posts matching keyword '{}'", posts.size(), keyword);

        return posts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get posts by category name with pagination
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByCategory(String categoryName, Pageable pageable) {
        log.debug("Fetching posts for category: {}", categoryName);

        Page<Post> postPage = postRepository.findByCategoryName(categoryName, pageable);
        log.info("Found {} posts in category '{}'", postPage.getTotalElements(), categoryName);

        return postPage.map(this::convertToResponse);
    }

    /**
     * Helper method: Fetch categories by IDs
     *
     * This validates that all category IDs exist
     * If any ID doesn't exist, throws ResourceNotFoundException
     */
    private Set<Category> fetchCategoriesByIds(Set<Long> categoryIds) {
        Set<Category> categories = new HashSet<>();

        for (Long categoryId : categoryIds) {
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

    /**
     * Convert Post entity to PostResponse DTO
     */
    private PostResponse convertToResponse(Post post) {
        // Convert categories to CategoryResponse
        Set<CategoryResponse> categoryResponses = post.getCategories().stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .collect(Collectors.toSet());

        // Convert comments to CommentResponse
        List<CommentResponse> commentResponses = post.getComments().stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getContent(),
                        comment.getAuthorName(),
                        comment.getCreatedAt(),
                        post.getId()  // postId
                ))
                .collect(Collectors.toList());

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                categoryResponses,
                commentResponses
        );
    }
}