package com.tripgether.sns.entity;

import com.tripgether.common.entity.BaseEntity;
import com.tripgether.common.entity.Media;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "content_media",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_content_media_pair", columnNames = {"content_id", "media_id"})
        },
        indexes = {
                @Index(name = "idx_content_media_content", columnList = "content_id"),
                @Index(name = "idx_content_media_media", columnList = "media_id")
        }
)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentMedia extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Media media;

}
