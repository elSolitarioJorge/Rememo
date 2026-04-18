package com.ggg.rememo.core.network;

import com.ggg.rememo.core.data.model.network.request.CreateCommentRequest;
import com.ggg.rememo.core.data.model.network.request.CreateMemoryPostRequest;
import com.ggg.rememo.core.data.model.network.request.LoginRequest;
import com.ggg.rememo.core.data.model.network.request.RegisterRequest;
import com.ggg.rememo.core.data.model.network.request.UpdateUserRequest;
import com.ggg.rememo.core.data.model.network.response.AuthResponse;
import com.ggg.rememo.core.data.model.network.response.CollectResponse;
import com.ggg.rememo.core.data.model.network.response.CommentResponse;
import com.ggg.rememo.core.data.model.network.response.ImageUploadResponse;
import com.ggg.rememo.core.data.model.network.response.LikeResponse;
import com.ggg.rememo.core.data.model.network.response.MemoryPointResponse;
import com.ggg.rememo.core.data.model.network.response.MemoryPostDetailResponse;
import com.ggg.rememo.core.data.model.network.response.MemoryPostListItemResponse;
import com.ggg.rememo.core.data.model.network.response.MemoryPostResponse;
import com.ggg.rememo.core.data.model.network.response.UserInfo;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API 服务接口定义。
 * 定义所有业务接口的网络请求方法。
 */
public interface ApiService {

    // ==================== 认证模块 ====================

    /**
     * 密码登录
     */
    @POST("/api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    /**
     * 用户注册
     */
    @POST("/api/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    // ==================== 用户模块 ====================

    /**
     * 获取当前用户信息
     */
    @GET("/api/user/info")
    Call<ApiResponse<UserInfo>> getUserInfo();

    /**
     * 更新用户信息（昵称、头像）
     */
    @PUT("/api/user/info")
    Call<ApiResponse<UserInfo>> updateUserInfo(@Body UpdateUserRequest request);

    // ==================== 评论模块 ====================

    /**
     * 发布评论
     */
    @POST("/api/comments")
    Call<ApiResponse<CommentResponse>> createComment(@Body CreateCommentRequest request);

    /**
     * 获取某记忆的所有评论
     * @param postId 记忆ID
     * @param page 页码（从 1 开始）
     * @param size 每页数量
     */
    @GET("/api/posts/{postId}/comments")
    Call<ApiResponse<List<CommentResponse>>> getCommentsByPostId(
            @Path("postId") String postId,
            @Query("page") int page,
            @Query("size") int size
    );

    // ==================== 互动模块 ====================

    /**
     * 点赞/取消点赞（切换操作）
     * @param postId 记忆帖子ID
     */
    @POST("/api/posts/{postId}/like")
    Call<ApiResponse<LikeResponse>> toggleLike(@Path("postId") String postId);

    /**
     * 获取点赞状态
     * @param postId 记忆帖子ID
     */
    @GET("/api/posts/{postId}/like")
    Call<ApiResponse<LikeResponse>> getLikeStatus(@Path("postId") String postId);

    /**
     * 收藏/取消收藏（切换操作）
     * @param postId 记忆帖子ID
     */
    @POST("/api/posts/{postId}/collect")
    Call<ApiResponse<CollectResponse>> toggleCollect(@Path("postId") String postId);

    /**
     * 获取收藏状态
     * @param postId 记忆帖子ID
     */
    @GET("/api/posts/{postId}/collect")
    Call<ApiResponse<CollectResponse>> getCollectStatus(@Path("postId") String postId);

    // ==================== 记忆模块 ====================

    /**
     * 获取所有记忆点（首页地图）
     */
    @GET("/api/memory-points")
    Call<ApiResponse<List<MemoryPointResponse>>> getAllMemoryPoints();

    /**
     * 发布记忆
     * 首次发布（不传 pointId）时后端自动创建关联记忆点
     */
    @POST("/api/posts")
    Call<ApiResponse<MemoryPostResponse>> createMemoryPost(@Body CreateMemoryPostRequest request);

    /**
     * 获取记忆列表（按记忆点）
     * @param pointId 记忆点ID
     */
    @GET("/api/memory-points/{pointId}/posts")
    Call<ApiResponse<List<MemoryPostListItemResponse>>> getPostsByPointId(
            @Path("pointId") String pointId
    );

    /**
     * 获取记忆列表（按用户）
     * @param userId 用户ID
     */
    @GET("/api/users/{userId}/posts")
    Call<ApiResponse<List<MemoryPostListItemResponse>>> getPostsByUserId(
            @Path("userId") String userId
    );

    /**
     * 获取随机记忆列表（发现页）
     * @param limit 获取数量
     */
    @GET("/api/posts/random")
    Call<ApiResponse<List<MemoryPostListItemResponse>>> getRandomPosts(@Query("limit") int limit);

    /**
     * 获取记忆详情
     * @param postId 记忆ID
     */
    @GET("/api/posts/{postId}")
    Call<ApiResponse<MemoryPostDetailResponse>> getPostDetail(@Path("postId") String postId);

    // ==================== 文件模块 ====================

    /**
     * 上传图片
     * @param file 图片文件
     * @param type 图片用途类型（avatar/memory/common）
     */
    @Multipart
    @POST("/api/upload/image")
    Call<ApiResponse<ImageUploadResponse>> uploadImage(
            @Part MultipartBody.Part file,
            @Part("type") RequestBody type
    );
}
