package com.sport.service.mappers.joint_training;

import com.sport.service.entities.JointTraining;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.mappers.string.DistrictStringMapper;
import com.sport.service.mappers.string.SportTypeStringMapper;
import com.sport.service.services.SubscriberService;
import com.sport.service.web.models.joint_training.CreateJointTrainingRequest;
import com.sport.service.web.models.joint_training.JointTrainingResponse;
import com.sport.service.web.models.joint_training.ListJointTrainingResponse;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
public abstract class JointTrainingMapperDelegate implements JointTrainingMapper {
    private SubscriberService subscriberService;

    @Autowired
    public JointTrainingMapperDelegate(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @Override
    public ListJointTrainingResponse jointTrainingListToListJointTrainingResponse(List<JointTraining> jointTrainings) {
        List<JointTrainingResponse> jointTrainingResponses = new ArrayList<>();

        for (JointTraining jointTraining : jointTrainings) {
            String sportTypeString = SportTypeStringMapper
                    .listSportTypeEnumToListSportTypeString(List.of(jointTraining.getSportType())).getFirst();
            String districtString = DistrictStringMapper.districtEnumToDistrictString(jointTraining.getDistrict());
            String username = jointTraining.getSubscriber().getUsername();
            String linkToChatWithCreator = String.format("[@%s](%s)", username, "https://t.me/" + username);

            jointTrainingResponses.add(new JointTrainingResponse(
                    jointTraining.getId(),
                    jointTraining.getTitle(),
                    jointTraining.getDescription(),
                    jointTraining.getDate(),
                    jointTraining.getTime(),
                    sportTypeString,
                    jointTraining.getPlaceName(),
                    districtString,
                    jointTraining.getAddress(),
                    jointTraining.getCreatorName(),
                    jointTraining.getPhoneNumber(),
                    linkToChatWithCreator));
        }
        return new ListJointTrainingResponse(jointTrainingResponses);
    }

    @Override
    public JointTraining createJointTrainingRequestToJointTraining(CreateJointTrainingRequest request, Subscriber subscriber) {
        SportType sportType = SportTypeStringMapper
                .listSportTypeStringToListSportTypeEnum(List.of(request.getSportType())).getFirst();
        District district = DistrictStringMapper.districtStringToDistrictEnum(String.valueOf(Objects.requireNonNull(request.getDistrict())));

        return JointTraining.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .date(LocalDate.parse(request.getDate()))
                .time(LocalTime.parse(Objects.requireNonNull(request.getTime())))
                .sportType(sportType)
                .subscriber(subscriber)
                .placeName(request.getPlaceName())
                .district(district)
                .address(request.getAddress())
                .creatorName(request.getCreatorName())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }
}