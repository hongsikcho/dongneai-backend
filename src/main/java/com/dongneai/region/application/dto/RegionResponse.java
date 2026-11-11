package com.dongneai.region.application.dto;

import com.dongneai.region.domain.Region;

public record RegionResponse(Long id, String name, String fullName) {

    public static RegionResponse from(Region region) {
        return new RegionResponse(region.getId(), region.getName(), region.getFullName());
    }
}
