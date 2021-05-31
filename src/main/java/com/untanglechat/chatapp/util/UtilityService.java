package com.untanglechat.chatapp.util;

import com.untanglechat.chatapp.dto.MessageDTO;
import com.untanglechat.chatapp.models.MessageInfoModel;

import org.springframework.stereotype.Service;

@Service
public class UtilityService {

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
    
}
