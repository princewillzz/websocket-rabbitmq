package com.untanglechat.chatapp.util;

import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.untanglechat.chatapp.dto.MessageDTO;
import com.untanglechat.chatapp.dto.ProfileDTO;
import com.untanglechat.chatapp.exceptions.UnAcceptableFormDataException;
import com.untanglechat.chatapp.models.MessageInfoModel;
import com.untanglechat.chatapp.models.Profile;
import com.untanglechat.chatapp.security.JwtTokenProvider;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.HandshakeInfo;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UtilityService {

    private final JwtTokenProvider jwtTokenProvider;

    private Validator validator;

    @PostConstruct
    void postConstruct() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Generate an object from messageDTO object of MessageInfoModel
     * @param messageDTO
     * @return MessageInfoModel
     */
    public MessageInfoModel messageDtoToMessageInfoModel(final MessageDTO messageDTO) {
        final MessageInfoModel messageInfoModel = new MessageInfoModel();

        messageInfoModel.setId(messageDTO.getId());

        messageInfoModel.setSentBy(messageDTO.getSentBy());
        messageInfoModel.setMessage(messageDTO.getMessage());
        messageInfoModel.setType(messageDTO.getType());

        messageInfoModel.setSentTime(messageDTO.getSentTime());
        messageInfoModel.setSentTo(messageDTO.getSentTo());     

        return messageInfoModel;
    }


    public String getTokenFromHandshakeInfo(final HandshakeInfo handshakeInfo){

        return (handshakeInfo.getUri().getQuery()).replace("token=", "");

    }

    public Claims extractAllClaimsFromRequest(final HttpHeaders headers) {
        final String bearer = headers.get("Authorization").get(0);
        final String token = bearer.replace("Bearer ", "");

        return jwtTokenProvider.extractAllClaims(token);
    }
    

    public String generateOTP(final int size) {
        String randomString = UUID.randomUUID().toString();
        if(size > randomString.length()) throw new IllegalArgumentException("OTP size to large");
        
        return randomString.substring(randomString.length()-size, randomString.length());
    }


    public boolean isProfileDTOAndModelValid(final ProfileDTO profileDTO, final Profile profile) throws UnAcceptableFormDataException {
        
        Set<ConstraintViolation<ProfileDTO>> profileDTOErrors = validator.validate(profileDTO);
        Set<ConstraintViolation<Profile>> profileErrors = validator.validate(profile);

        if(profileDTOErrors.size() > 0 || profileErrors.size() > 0) {
            throw new UnAcceptableFormDataException("Invalid Details!!");
        }

        if(profile.getId() != null || !profileDTO.getPassword().equals(profileDTO.getRePassword())) 
            throw new UnAcceptableFormDataException("Illegal Data!!");

        if(!profileDTO.getPassword().equals(profileDTO.getRePassword())) 
            throw new UnAcceptableFormDataException("Password does not match");

        if(profileDTO.getVerificationOTP() == null || profileDTO.getVerificationOTP().isBlank()) 
            throw new UnAcceptableFormDataException("Invalid OTP!!");

        return true;
    }

}
