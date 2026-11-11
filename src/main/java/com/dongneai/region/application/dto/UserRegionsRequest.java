package com.dongneai.region.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserRegionsRequest(@NotNull List<@Valid Item> regions) {

    public record Item(@NotNull Long regionId, boolean primary) {
    }
}
