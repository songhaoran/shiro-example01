package com.song.service.impl;


import com.song.dao.SysUserMapper;
import com.song.domain.SysUser;
import com.song.service.RoleService;
import com.song.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordHelper passwordHelper;

    @Autowired
    private RoleService roleService;

    /**
     * 创建用户
     *
     * @param user
     */
    @CachePut(value = {"redisCache"}, key = "'SysUser-'+#user.id")
    public SysUser createUser(SysUser user) {
        //加密密码
        passwordHelper.encryptPassword(user);
        userMapper.insertSelective(user);
        return user;
    }

    @Override
    @CachePut(value = {"redisCache"}, key = "'SysUser-'+#user.id")
    public SysUser updateUser(SysUser user) {
        userMapper.updateByPrimaryKeySelective(user);
        return user;
    }

    @Override
    @CacheEvict(value = {"redisCache"},key="'SysUser-'+#userId")
    public void deleteUser(Long userId) {
        userMapper.deleteByPrimaryKey(userId);
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param newPassword
     */
    public void changePassword(Long userId, String newPassword) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(newPassword);
        passwordHelper.encryptPassword(user);
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    @Cacheable(value = {"redisCache"}, key = "'SysUser-'+#userId")
    public SysUser findOne(Long userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    @Override
    public List<SysUser> findAll() {
        return userMapper.selectAll();
    }

    /**
     * 根据用户名查找用户
     *
     * @param user
     * @return
     */
    @Cacheable(value = {"redisCache"}, key = "'SysUser-'+#user")
    public SysUser findByUsername(String user) {
        System.out.println("************find from sql*************");
        return userMapper.selectByUserName(user);
    }

    /**
     * 根据用户名查找其角色
     *
     * @param username
     * @return
     */
    @Cacheable(value = {"redisCache"}, key = "'role-Set<String>-'+#username")
    public Set<String> findRoles(String username) {
        SysUser user = findByUsername(username);
        if (user == null) {
            return Collections.EMPTY_SET;
        }
        String roleIds = user.getRoleIds();
        if (StringUtils.isNotBlank(roleIds)) {
            String[] arr = user.getRoleIds().split(",");
            Long[] ids = new Long[arr.length];
            for (int i = 0; i < arr.length; i++) {
                ids[i] = Long.parseLong(arr[i]);
            }
            return roleService.findRoles(ids);
        }
        return null;
    }

    /**
     * 根据用户名查找其权限
     *
     * @param username
     * @return
     */
    @Cacheable(value = {"redisCache"}, key = "'per-Set<String>-'+#username")
    public Set<String> findPermissions(String username) {
        SysUser user = findByUsername(username);
        if (user == null) {
            return Collections.EMPTY_SET;
        }
        String roleIds = user.getRoleIds();
        if (StringUtils.isNotBlank(roleIds)) {
            String[] arr = user.getRoleIds().split(",");
            Long[] ids = new Long[arr.length];
            for (int i = 0; i < arr.length; i++) {
                ids[i] = Long.parseLong(arr[i]);
            }
            return roleService.findPermissions(ids);
        }
        return null;
    }

}
