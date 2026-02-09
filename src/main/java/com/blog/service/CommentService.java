package com.blog.service;

import com.blog.dtos.CommentRequest;
import com.blog.dtos.CommentResponse;
import com.blog.dtos.PostResponse;
import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    /**
     * Create a comment on a post
     */

    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        // fetch the post
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Post", request.getPostId()));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthorName(request.getAuthorName());

        // set up the relationship using the helper method, ensures bidirectional relation is maintained
        post.addComment(comment);

        Comment savedComment = commentRepository.save(comment);

        return convertToResponse(savedComment);
    }


    /**
     *  get all the comments for a specific post
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {

        // verify post exists
        if(!postRepository.existsById(postId)) {
            throw ResourceNotFoundException.forId("Post", postId);
        }

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return comments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * get comments by post id with pagination
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable) {

        if(!postRepository.existsById(postId)) {
            throw ResourceNotFoundException.forId("Post", postId);
        }

        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);

        return commentPage.map(this::convertToResponse);
    }


    /**
     * get comment by ID
     */
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Comment", id));

        return convertToResponse(comment);
    };


    /**
     * update a comment
     */
    @Transactional
    public CommentResponse updateComment(Long id, CommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("comment", id));

        // verify the comment belongs to the post specified in request
        if (!comment.getPost().getId().equals(request.getPostId())) {
            throw new IllegalArgumentException(
                    "comment does not belongs to the specified post"
            );
        }

        comment.setContent(request.getContent());
        comment.setAuthorName(request.getAuthorName());

        Comment savedComment = commentRepository.save(comment);

        return convertToResponse(savedComment);
    }


    /**
     * Delete comment
     */
    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("comment", id);
        }

        commentRepository.deleteById(id);
        log.info("Comment deleted successfully with id: {}", id);
    }


    /**
     * get comment count for a post
     */
    @Transactional(readOnly = true)
    public Long getCommentCountByPostId(Long postId) {
        if(!postRepository.existsById(postId)) {
            throw ResourceNotFoundException.forId("comment", postId);
        }

        return commentRepository.countByPostId(postId);
    }


    private CommentResponse convertToResponse(Comment savedComment) {

        CommentResponse response = new CommentResponse();
        response.setId(savedComment.getId());
        response.setContent(savedComment.getContent());
        response.setAuthorName(savedComment.getAuthorName());
        response.setCreatedAt(savedComment.getCreatedAt());
        response.setPostId(savedComment.getPost().getId());

        return response;
    }

}
