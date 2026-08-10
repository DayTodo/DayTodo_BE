package com.daytodo.domain.region.controller;

import com.daytodo.domain.region.dto.RegionResponse;
import com.daytodo.domain.region.enums.RegionLevel;
import com.daytodo.domain.region.service.RegionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegionControllerTest {
    @Mock RegionService regionService;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RegionController(regionService)).build();
    }

    @Test
    void returnsRegionList() throws Exception {
        when(regionService.getRegions()).thenReturn(new RegionResponse.Regions(List.of(
                new RegionResponse.RegionItem(1L, "서울특별시", RegionLevel.SIDO, null, null),
                new RegionResponse.RegionItem(2L, "강남구", RegionLevel.SIGUNGU, 1L, "서울특별시")
        )));

        mockMvc.perform(get("/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regions[0].regionId").value(1L))
                .andExpect(jsonPath("$.regions[0].regionName").value("서울특별시"))
                .andExpect(jsonPath("$.regions[1].regionLevel").value("SIGUNGU"))
                .andExpect(jsonPath("$.regions[1].parentRegionId").value(1L))
                .andExpect(jsonPath("$.regions[1].parentRegionName").value("서울특별시"));

        verify(regionService).getRegions();
    }
}
