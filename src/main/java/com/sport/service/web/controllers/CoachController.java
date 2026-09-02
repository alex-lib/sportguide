package com.sport.service.web.controllers;

import com.sport.service.services.CoachService;
import com.sport.service.web.models.coach.CoachRequest;
import com.sport.service.web.models.coach.ListCoachResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/coaches")
@RequiredArgsConstructor
public class CoachController {
    private final CoachService coachService;

    @PreAuthorize("hasAnyRole('SUBSCRIBER','ADMIN')")
    @GetMapping
      public ListCoachResponse getAllCoaches(
          @RequestParam(required = false) List<String> sportTypes,
          @RequestParam(required = false) Integer age,
          @RequestParam(required = false) String sex,
          @RequestParam(required = false) Integer yearsOfExperience,
          @RequestParam(required = false) String search
      ) {
          return coachService.findAllCoaches(sportTypes, age, sex, yearsOfExperience, search);
      }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return coachService.uploadPhoto(id, file);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        coachService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> getPhoto(@RequestParam String photoUrl) {
        byte[] photo = coachService.getPhoto(photoUrl);
        if (photo == null || photo.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photo);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createCoach(@RequestPart("data") @Valid CoachRequest request,
                            @RequestPart("photo") MultipartFile photo
    ) {
        coachService.createCoach(request, photo);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public void updateCoach(
            @PathVariable Long id,
            @RequestPart("data") @Valid CoachRequest request,
            @RequestPart("photo") MultipartFile photo
    ) {
        coachService.updateCoachById(id, request, photo);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCoach(@PathVariable Long id) {
        coachService.deleteCoachById(id);
    }
}
