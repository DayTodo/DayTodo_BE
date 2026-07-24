package com.daytodo.domain.course.controller;

import com.daytodo.domain.course.dto.response.CourseResDTO;
import com.daytodo.domain.course.service.TodayCourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .willReturn(new CourseResDTO.GetTodayCourse(null));

        mockMvc.perform(get("/courses/today").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayCourse").doesNotExist());
    }
}
