package com.ajaxjs.im.controller;

import com.ajaxjs.im.model.Community;
import com.ajaxjs.im.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 */
@RestController
@RequestMapping("/contract")
public interface ContractController {
    /**
     * 添加好友
     *
     * @param userName 用户名，可以是部分的
     * @return 是否成功
     */
    @PostMapping("/{userName}")
    boolean addFriend(String userName);

    /**
     * 加载好友列表
     *
     * @return 好友列表
     */
    @GetMapping("/friend_list")
    List<User> loadFriendList();

    /**
     * 创建群
     *
     * @param community 群实体
     * @return 是否成功
     */
    @PostMapping("/create_community")
    boolean CreateCommunity(Community community);

    /**
     * 用户加群
     *
     * @return 是否成功
     */
    @PostMapping("/join_community")
    boolean joinCommunity();

    /**
     * 加载群列表
     *
     * @return 群列表
     */
    @GetMapping("/community_list")
    List<Community> loadCommunityList();
}
