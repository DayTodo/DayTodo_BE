package com.daytodo.domain.region.controller;

import com.daytodo.domain.region.dto.RegionResponse;
import com.daytodo.domain.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Region")
@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @Operation(summary = "전체 지역 목록 조회")
    @GetMapping
    public RegionResponse.Regions getRegions() {
        return regionService.getRegions();
    }
}
