package tw.readingclub.newsfeed.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 建立文字貼文的輸入資料；第一階段限制為 500 字元。
 */
public record CreatePostRequest(@NotBlank @Size(max = 500) String content) {}
