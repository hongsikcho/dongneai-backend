package com.dongneai.region.api;

import com.dongneai.region.domain.RegionQueryRepository;
import com.dongneai.region.dto.RegionResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private static final int MAX_RESULTS = 20;

    private final RegionQueryRepository regionQueryRepository;

    /** AUTH-05 지역 설정: 지역 검색. */
    @GetMapping
    @Transactional(readOnly = true)
    public List<RegionResponse> search(@RequestParam @NotBlank @Size(max = 20) String q) {
        return regionQueryRepository.searchActiveDongs(q.strip(), MAX_RESULTS).stream()
                .map(RegionResponse::from)
                .toList();
    }
}
