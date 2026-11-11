package com.dongneai.feed.domain;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReactionId implements Serializable {
    private Long userId;
    private ReactionTarget targetType;
    private Long targetId;
    private String type;
}
