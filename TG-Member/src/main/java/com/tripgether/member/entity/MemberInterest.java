package com.tripgether.member.entity;

import com.tripgether.common.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberInterest extends SoftDeletableBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Interest interest;
}
