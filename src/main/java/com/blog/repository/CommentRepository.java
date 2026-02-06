package com.blog.repository;

import com.blog.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    Page<Comment> findByPostId(Long postId, Pageable pageable);

    List<Comment> findByAuthorNameIgnoreCase(String authorName);

    Long countByPostId(Long postId);

    Long deleteByPostId(Long postId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.post WHERE c.post.id = :postId")
    List<Comment> findByPostIdWithPost(@Param("postId") Long postId);
}
