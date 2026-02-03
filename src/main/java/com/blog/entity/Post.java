package com.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.stream.events.Comment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // relationship 1: one to many | one post can have many comments
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();


    // relationship 2: many to many | one post has many categories and vice versa
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "post_categories",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();


    // LifeCycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    // Helper methods for managing relationships

    /**
    *  Helper method to add a comment
    *  This ensures bidirectional relationship is maintained
    **/

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    /**
     *  Helper method to remove a comment
     */

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    /**
     * Helper method to add a category
     */
    public void addCategory(Category category) {
        categories.add(category);
    }


    /**
     * Helper method to remove a category
     */
    public void removeCategory(Category category) {
        categories.remove(category);
    }
}


















