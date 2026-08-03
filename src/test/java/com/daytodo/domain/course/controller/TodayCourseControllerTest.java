package com.daytodo.domain.course.controller;

import com.daytodo.domain.course.dto.response.TodayCourseResponse;
import com.daytodo.domain.course.exception.code.CourseErrorCode;
import com.daytodo.domain.course.service.TodayCourseService;
import com.daytodo.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodayCourseController.class)
class TodayCourseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TodayCourseService todayCourseService;

    // DayTodoApplication의 @EnableJpaAuditing 때문에 WebMvc 슬라이스에서도 JPA 메타모델을 요구한다.
    @MockitoBean
    org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("오늘 진행 중인 코스가 없으면 200과 함께 todayCourse가 null로 내려간다")
    void todayCourseNull() throws Exception {
        given(todayCourseService.getTodayCourse(anyLong()))
                .willReturn(new TodayCourseResponse.GetTodayCourse(null));

        mockMvc.perform(get("/courses/today").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayCourse").doesNotExist());
    }

    @Test
    @DisplayName("저장할 이미지가 없으면 400 INVALID_PARAMETER로 응답한다")
    void emptyImageUrls() throws Exception {
        given(todayCourseService.saveMemoryPhotos(anyLong(), anyLong(), any()))
                .willThrow(new ProjectException(CourseErrorCode.EMPTY_MEMORY_PHOTO));

        mockMvc.perform(post("/courses/1/photos")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrls\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.message").value("저장할 이미지가 없습니다."));
    }
}
