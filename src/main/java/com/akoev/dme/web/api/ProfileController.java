package com.akoev.dme.web.api;

import com.akoev.dme.application.service.ProfileUpdateCommand;
import com.akoev.dme.application.service.UserProfileService;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import com.akoev.dme.web.api.dto.ProfileResponse;
import com.akoev.dme.web.api.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ProfileResponse me(@AuthenticationPrincipal CustomUserDetails principal) {
        return ProfileResponse.from(userProfileService.getProfile(principal.getId()));
    }

    @PutMapping("/me")
    public ProfileResponse updateMe(@AuthenticationPrincipal CustomUserDetails principal,
                                     @Valid @RequestBody UpdateProfileRequest request) {
        return ProfileResponse.from(userProfileService.updateProfile(principal.getId(), toCommand(request)));
    }

    // Deliberately kept in the web layer (not a static factory on
    // ProfileUpdateCommand itself) — Command types live in application.service,
    // which must not depend on web.api.dto, so this Request->Command mapping
    // has to live on the outer (web) side of that boundary.
    private static ProfileUpdateCommand toCommand(UpdateProfileRequest request) {
        var limitations = request.limitations() == null ? null : request.limitations().stream()
                .map(l -> new ProfileUpdateCommand.LimitationCommand(l.muscleGroup(), l.note()))
                .toList();

        return new ProfileUpdateCommand(
                request.birthDate(),
                request.sex(),
                request.heightCm(),
                request.weightKg(),
                request.experienceLevel(),
                request.primaryGoal(),
                request.daysPerWeek(),
                request.sessionDurationMinutes(),
                request.notes(),
                request.location(),
                request.equipmentIds(),
                request.favoriteExerciseIds(),
                request.dislikedExerciseIds(),
                request.preferredCategories(),
                request.unwantedCategories(),
                limitations,
                request.restDays()
        );
    }
}
