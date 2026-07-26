package com.daytodo.domain.course.dto;

import com.daytodo.domain.course.enums.ParticipantType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CourseRequestValidationTest {
    static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void rejectsBlankAndOverTwentyCharacterCourseNames() {
        CourseRequest.Create blank = request("   ", 0, 1000, ParticipantType.FRIEND);
        CourseRequest.Create tooLong = request("123456789012345678901", 0, 1000, ParticipantType.FRIEND);

        assertThat(validator.validate(blank)).isNotEmpty();
        assertThat(validator.validate(tooLong)).isNotEmpty();
    }

    @Test
    void rejectsNegativeAndReversedPrices() {
        CourseRequest.Create negative = request("코스", -1, 1000, ParticipantType.FRIEND);
        CourseRequest.Create reversed = request("코스", 2000, 1000, ParticipantType.FRIEND);

        assertThat(validator.validate(negative)).isNotEmpty();
        assertThat(validator.validate(reversed)).isNotEmpty();
    }

    @Test
    void rejectsMissingRegionDateAndParticipantType() {
        CourseRequest.Create request = new CourseRequest.Create("코스", null, null, 0, 1000, null);

        assertThat(validator.validate(request)).hasSize(3);
    }

    private CourseRequest.Create request(
            String name,
            Integer minPrice,
            Integer maxPrice,
            ParticipantType participantType
    ) {
        return new CourseRequest.Create(
                name, 1L, LocalDate.of(2026, 7, 23), minPrice, maxPrice, participantType
        );
    }
}
