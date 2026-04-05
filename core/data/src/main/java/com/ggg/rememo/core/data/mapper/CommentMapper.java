package com.ggg.rememo.core.data.mapper;

import com.ggg.rememo.core.data.model.entity.Comment;
import com.ggg.rememo.core.data.model.network.response.CommentResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * CommentResponse → Comment Entity 转换器。
 */
public class CommentMapper {

    private CommentMapper() {}

    public static Comment fromResponse(CommentResponse r) {
        if (r == null) return null;
        Comment comment = new Comment();
        comment.setCommentId(r.getCommentId());
        comment.setPostId(r.getPostId());
        comment.setAuthorId(r.getAuthorId());
        comment.setAuthorNickname(r.getAuthorNickname());
        comment.setAuthorAvatar(r.getAuthorAvatar());
        comment.setContent(r.getContent());
        comment.setCreatedTime(r.getCreatedTime());
        return comment;
    }

    public static List<Comment> fromResponseList(List<CommentResponse> responses) {
        if (responses == null || responses.isEmpty()) return new ArrayList<>();
        List<Comment> comments = new ArrayList<>(responses.size());
        for (CommentResponse r : responses) {
            Comment comment = fromResponse(r);
            if (comment != null) {
                comments.add(comment);
            }
        }
        return comments;
    }
}
