package com.blog.repository;

import com.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByTitleContainingIgnoreCase(String keyword);

    List<Post> findAllByOrderByCreatedAtDesc();

    Page<Post> findAll(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))" )
    List<Post> searchPosts(@Param("keyword") String keyword);


    // custom JPQL query with JOIN FETCH
    /**
     * JOIN FETCH loads everything in one query
     *
     * LEFT JOIN FETCH:
     * - loads post even if it has no categories
     * - Also loads all associated categories in the same query
     *
     * this solves the N+1 query problem
     */
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.categories")
    List<Post> findAllWithCategories();



    // find post by category name
    List<Post> findByCategoriesName(String categoryName);


    // Custom JPQL to find posts by category with pagination
    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c.name = :categoryName")
    Page<Post> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

}
