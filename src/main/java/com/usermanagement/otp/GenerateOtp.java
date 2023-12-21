package com.usermanagement.otp;

import com.google.common.cache.LoadingCache;
import com.usermanagement.exception.EmailNotFoundException;
import com.usermanagement.modelentity.User;
import com.usermanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class GenerateOtp {

    private final LoadingCache<Long, Integer> oneTimePasswordCache;
    private final UserRepository userRepository;

    public ResponseEntity<?> generateOtp(final String emailId) {
        User user = userRepository.findByEmail(emailId)
                .orElseThrow(() -> new EmailNotFoundException("Email id not found"));
        JSONObject jsonObject = new JSONObject();

        try {
            if (Objects.nonNull(oneTimePasswordCache.get(user.getId())))
                oneTimePasswordCache.invalidate(user.getId());
        } catch (ExecutionException e) {
            throw new com.usermanagement.exception.ExecutionException(e.getMessage());
        }

        Integer otp = new Random().ints(1, 100000, 999999).sum();
        oneTimePasswordCache.put(user.getId(), otp);

        jsonObject.put(OtpConstants.OTP, otp);
        jsonObject.put(OtpConstants.MESSAGE, OtpConstants.OTP_GENERATION_SUCCESS);
        jsonObject.put(OtpConstants.TIMESTAMP, LocalDateTime.now().toString());
        return ResponseEntity.ok(jsonObject.toString());
    }

    private boolean validateOtp(final User user, final Integer otp) throws ExecutionException {
        return oneTimePasswordCache.get(user.getId()).equals(otp);
    }

   /* public ResponseEntity<?> changePassword(final ForgotPasswordChangeRequestDto forgotPasswordChangeRequest)
            throws ExecutionException {
        final var user = userRepository.findByEmailId(forgotPasswordChangeRequest.getEmailId())
                .orElseThrow(() -> new InvalidUserIdException());
        final var response = new JSONObject();

        if (validateOtp(user, forgotPasswordChangeRequest.getOtp())) {
            user.setPassword(passwordEncoder.encode(forgotPasswordChangeRequest.getNewPassword()));
            userRepository.save(user);

            oneTimePasswordCache.invalidate(user.getId());

            response.put(OtpConstants.MESSAGE, OtpConstants.PASSWORD_CHANGE_SUCCESS);
            response.put(OtpConstants.TIMESTAMP, LocalDateTime.now().toString());
            return ResponseEntity.ok(response.toString());
        } else
            throw new OneTimePasswordValidationFailureException();
    }*/
}
