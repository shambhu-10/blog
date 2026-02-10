package com.blog.controller;

import com.blog.dtos.CommentRequest;
import com.blog.dtos.CommentResponse;
import com.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Creat a comment on a post
     */
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.createComment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    /**
     * Get comments by post id
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);

        return ResponseEntity.ok(comments);
    }


    /**
     * Get comments by post id with pagination
     */
    @GetMapping("/post/{postId}/paged")
    public ResponseEntity<Page<CommentResponse>> getCommentsByPostPaged(@PathVariable Long postId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<CommentResponse> comments = commentService.getCommentsByPostId(postId, pageable);

        return ResponseEntity.ok(comments);
    }


    // get comment by id
    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentbyId(@PathVariable Long id) {
        CommentResponse comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }


    // update a comment
    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long id,
                                                         @RequestBody CommentRequest request) {
        CommentResponse response = commentService.updateComment(id, request);
        return ResponseEntity.ok(response);
    }


    // delete a comment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }


    // get comment count for a post
    @GetMapping("/{id}")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long id) {
        Long count = commentService.getCommentCountByPostId(id);
        return ResponseEntity.ok(count);

    }
}
