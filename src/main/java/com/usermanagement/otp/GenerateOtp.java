package com.usermanagement.otp;

import com.google.common.cache.LoadingCache;
import com.usermanagement.exception.EmailNotFoundException;
import com.usermanagement.exception.OtpExpiredException;
import com.usermanagement.exception.UserAlreadyExistsException;
import com.usermanagement.modelentity.User;
import com.usermanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class GenerateOtp {

    private final LoadingCache<String, String> oneTimePasswordCache;
    private final UserRepository userRepository;

    public JSONObject generateOtp(final String emailId) {
        JSONObject jsonObject = new JSONObject();
            if (Objects.nonNull(oneTimePasswordCache.getIfPresent(emailId)))
                oneTimePasswordCache.invalidate(emailId);

        Integer otp = new Random().ints(1, 100000, 999999).sum();
        oneTimePasswordCache.put(emailId, String.valueOf(otp));

        jsonObject.put(OtpConstants.OTP, otp);
        jsonObject.put(OtpConstants.MESSAGE, OtpConstants.OTP_GENERATION_SUCCESS);
        jsonObject.put(OtpConstants.TIMESTAMP, LocalDateTime.now().toString());
        return jsonObject;
    }

    public boolean validateOtp(String otp,String emailId)  {
        try {
            if(oneTimePasswordCache.get(emailId).equals(otp)) {
                return true;
            }else {
                throw new OtpExpiredException("Otp Expired. Please try again.");
            }
        } catch (ExecutionException e) {
            throw new com.usermanagement.exception.ExecutionException("Otp is not valid. Please try again.");
        }
    }
}
