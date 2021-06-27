package com.untanglechat.chatapp.util;

import com.untanglechat.chatapp.dto.MessageDTO;
import com.untanglechat.chatapp.models.MessageInfoModel;
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
    
}
