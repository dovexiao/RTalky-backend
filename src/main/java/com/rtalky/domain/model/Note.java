package com.rtalky.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 笔记聚合根
 */
@AggregateRoot
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "notes")
public class Note {

    /**
     * 技术主键，用于数据库自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 业务主键，笔记唯一标识符
     */
    @Column(name = "note_id", unique = true, nullable = false, length = 50)
    private String noteId;

    /**
     * 显示ID，格式：N+自增ID（不足三位前补零）
     * 在首次插入后由JPA回调基于自增id生成并更新。
     * 注意：列允许为空以便首次INSERT成功，然后在PostPersist中补齐。
     */
    @Column(name = "display_id", unique = true, nullable = true, length = 10)
    private String displayId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    /**
     * 笔记标题
     */
    @Column(name = "title", nullable = false, length = 20)
    private String title;

    /**
     * 笔记简介
     */
    @Column(name = "description", length = 50)
    private String description;

    /**
     * 笔记内容
     */
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 标签列表（JSON格式存储）
     */
    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 是否删除
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    // 工厂方法 - 创建新笔记
    public static Note newNote(String userId, String title, String description, String content, List<String> tags) {
        Note note = new Note();
        note.setNoteId("N" + UUID.randomUUID().toString().replace("-", ""));
        note.setUserId(userId);
        note.setTitle(title);
        note.setDescription(description);
        note.setContent(content);
        note.setTags(convertTagsToJson(tags));
        note.setCreatedTime(LocalDateTime.now());
        note.setUpdatedTime(LocalDateTime.now());
        note.setDeleted(false);
        // displayId由数据库生成列自动计算，不需要手动设置
        return note;
    }

    // 领域行为：更新标题
    public void updateTitle(String newTitle) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("笔记标题不能为空");
        }
        if (newTitle.length() > 20) {
            throw new IllegalArgumentException("笔记标题不能超过20个字符");
        }
        this.title = newTitle;
        this.updatedTime = LocalDateTime.now();
    }

    // 领域行为：更新简介
    public void updateDescription(String newDescription) {
        if (newDescription != null && newDescription.length() > 50) {
            throw new IllegalArgumentException("笔记简介不能超过50个字符");
        }
        this.description = newDescription;
        this.updatedTime = LocalDateTime.now();
    }

    // 领域行为：更新内容
    public void updateContent(String newContent) {
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("笔记内容不能为空");
        }
        this.content = newContent;
        this.updatedTime = LocalDateTime.now();
    }

    // 领域行为：更新标签
    public void updateTags(List<String> newTags) {
        if (newTags != null) {
            if (newTags.size() > 20) {
                throw new IllegalArgumentException("标签数量不能超过20个");
            }
            for (String tag : newTags) {
                if (tag != null && tag.length() > 20) {
                    throw new IllegalArgumentException("标签长度不能超过20个字符");
                }
            }
        }
        this.tags = convertTagsToJson(newTags);
        this.updatedTime = LocalDateTime.now();
    }

    // 领域行为：软删除
    public void markAsDeleted() {
        this.deleted = true;
        this.updatedTime = LocalDateTime.now();
    }

    // 领域行为：检查笔记是否属于指定用户
    public boolean belongsToUser(String userId) {
        return this.userId.equals(userId);
    }

    // 领域行为：检查笔记是否已删除
    public boolean isDeleted() {
        return this.deleted;
    }

    // 领域行为：获取标签列表
    public List<String> getTagsList() {
        return convertJsonToTags(this.tags);
    }

    // 工具方法：将标签列表转换为JSON字符串
    private static String convertTagsToJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            // 如果序列化失败，返回空数组
            return "[]";
        }
    }

    // 工具方法：将JSON字符串转换为标签列表
    private static List<String> convertJsonToTags(String tagsJson) {
        if (tagsJson == null || tagsJson.trim().isEmpty()) {
            return List.of();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            // 如果反序列化失败，返回空列表
            return List.of();
        }
    }

    @PostPersist
    private void populateDisplayIdAfterInsert() {
        if (this.displayId == null && this.id != null) {
            this.displayId = "N" + String.format("%03d", this.id);
            // 修改托管实体字段，JPA 将在当前事务内发出 UPDATE 以持久化该变化
        }
    }
}
