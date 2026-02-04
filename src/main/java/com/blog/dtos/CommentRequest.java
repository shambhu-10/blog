package com.blog.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {

    @NotBlank(message = "comment content is required")
    @Size(min = 1, max = 1000, message = "comment must be between 1 and 1000 characters")
    private String content;

    @Size(max = 100, message = "Author name cannot exceed 100 characters")
    private String authorName;

    @NotNull(message = "Post Id is required")
    private Long postId;

}
