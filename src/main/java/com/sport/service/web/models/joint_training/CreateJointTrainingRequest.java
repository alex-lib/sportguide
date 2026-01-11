package com.sport.service.web.models.joint_training;

import com.sport.service.annotations.PhoneNumberValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJointTrainingRequest {

    @Size(
            min = 5,
            max = 35,
            message = "Title must contains between {min} and {max} characters"
    )
    private String title;

    @Size(
            min = 10,
            max = 100,
            message = "Description must contains between {min} and {max} characters"
    )
    private String description;

    private String date;
    private String time;

    @NotBlank(message = "Sport type must be pointed")
    private String sportType;

    private String placeName;

    @NotBlank(message = "District must be pointed")
    private String district;

    private String address;

    @PhoneNumberValid
    private String phoneNumber;

    @Size(
            min = 2,
            max = 35,
            message = "Name must be pointed and contains between {min} and {max} characters"
    )
    private String creatorName;
}