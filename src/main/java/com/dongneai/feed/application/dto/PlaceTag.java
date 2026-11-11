package com.dongneai.feed.application.dto;

/** 글에 태그된 장소. category는 장소 카테고리 코드 (예: KIDS_CAFE). */
public record PlaceTag(Long id, String name, String category) {
}
