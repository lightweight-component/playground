package com.ajaxjs.im.service;

import com.ajaxjs.im.controller.ContractController;
import com.ajaxjs.im.model.Community;
import com.ajaxjs.im.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService implements ContractController {
    @Override
    public boolean addFriend(String userName) {
        return false;
    }

    @Override
    public List<User> loadFriendList() {
        return null;
    }

    @Override
    public boolean CreateCommunity(Community community) {
        return false;
    }

    @Override
    public boolean joinCommunity() {
        return false;
    }

    @Override
    public List<Community> loadCommunityList() {
        return null;
    }
}
