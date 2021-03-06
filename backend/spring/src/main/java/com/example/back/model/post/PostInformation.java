package com.example.back.model.post;

import java.time.LocalDateTime;

import javax.persistence.*;

import com.example.back.model.user.Users;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 테이블의 필드와 매핑되는 영역(DTO)
@Entity
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) 
@Table(name="post_information")
public class PostInformation {
    
    @Id
    // 기본키 생성을 DB에 위임하도록 함
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name="post_id")
    private Integer postId;

    @Column(name="user_id")
    private Integer userId;

    @Column(name="title", columnDefinition = "TEXT")
    private String title;
    
    @Column(name="contents", columnDefinition = "TEXT")
    private String contents;

    @Column(name="created_at", columnDefinition = "datetime")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name="updated_at", columnDefinition = "datetime")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name="display_level")
    private String displayLevel;

    @Column(name="price", columnDefinition = "integer")
    private Integer price;

    @Builder
    public PostInformation(String title, String contents, String displayLevel, int userId){
        this.title = title;
        this.contents = contents;
        this.displayLevel = displayLevel;
        this.userId = userId;
    }

    @Builder
    public PostInformation(String title, String contents, String displayLevel, int price, int postId, int userId){
        this.title = title;
        this.contents = contents;
        this.displayLevel = displayLevel;
        this.price = price;
        this.userId = userId;
        this.postId = postId;
    }

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(referencedColumnName = "id", insertable = false, updatable = false)
    Users user;


    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(referencedColumnName="id", insertable = false, updatable = false)
    Posts post;


    public void add(Posts post){
        post.add(this);;
        this.post = post;
    }

    public void setUsers(Users user){
        if(user == null){
            this.user = null;
        }
        else{
            this.user = user;
        }
    }
    
    public void setPosts(Posts post){
        if(post == null){
            this.post = null;
        }
        else{
            this.post = post;
        }
    }


}
