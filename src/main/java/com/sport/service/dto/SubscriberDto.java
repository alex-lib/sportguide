package com.sport.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriberDto {
    public Long id;
    public String username;
    @JsonProperty("first_name")
    public String firstName;
    @JsonProperty("last_name")
    public String lastName;
    @JsonProperty("language_code")
    private String languageCode;
    @JsonProperty("is_premium")
    private Boolean isPremium;
    @JsonProperty("allows_write_to_pm")
    private Boolean allowsWriteToPm;
    @JsonProperty("photo_url")
    private String photoUrl;
}