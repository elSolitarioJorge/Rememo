/**
 * core:data — 统一数据层模块
 *
 * <ul>
 *   <li>model/entity     - 数据库实体（Room Entity，Parcelable）</li>
 *   <li>model/network    - 网络请求/响应 DTO（Request / Response 模型）</li>
 *   <li>model/summary    - 聚合统计视图模型</li>
 *   <li>local/dao        - Data Access Objects</li>
 *   <li>local/database   - Room Database 配置</li>
 *   <li>local/type       - 类型转换器（Room Gson 序列化）</li>
 *   <li>repository       - 数据聚合层（本地 DAO + 网络 API 整合）</li>
 *   <li>mapper           - Entity ↔ Response 模型转换（MemoryPointMapper / MemoryPostMapper）</li>
 *   <li>util             - 图片存储等工具类</li>
 * </ul>
 */
package com.ggg.rememo.core.data;
