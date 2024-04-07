package dfd;

import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import dfd.common.model.dto.BaseDTO;
import dfd.common.utils.IdUtil;
import dfd.rbac.common.model.enums.AccountState;
import dfd.rbac.common.model.enums.PermissionType;
import dfd.rbac.model.dto.*;
import dfd.rbac.service.RbacPermissionService;
import dfd.rbac.service.RbacRoleService;
import dfd.rbac.service.RbacTicketService;
import dfd.rbac.service.RbacUserService;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单元测试启动器
 * @author: binchao
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AppBootstrapTest {

    protected static final Long STATIC_ID = 123456L;

    protected static final String ACCOUNT = "junit_test";

    protected static final String PASSWORD = "junit_test";

    protected static final String COL_TITLE = "Junit Test";

    protected static final String EMAIL = "junit_test@hotmail.com";

    protected static final String PHONE_NUMBER = "13800138000";

    protected static final String SYSTEM_ADMIN_ROLE_CODE = "system_admin";

    /**
     * 根菜单节点ID
     * 注意：数据库初始化脚本已附带初始化数据，请先初始化数据库，也可按需更改ID
     */
    protected static final Long ROOT_MENU_ID = 1447415378427310082L;

    @Resource
    protected RbacUserService userService;

    @Resource
    protected RbacRoleService roleService;

    @Resource
    protected RbacPermissionService permissionService;

    @Resource
    protected RbacTicketService ticketService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void loadTest() {
        log.info("JUnit Test Start...");
        initAdminUser();
    }

    /**
     * 初始化管理员
     */
    protected void initAdminUser() {

        RbacUserDTO userDTO = RbacUserDTO.builder()
                .account("admin")
                .password(SecureUtil.md5("Ram2021"))
                .nickName("BeanShell.More")
                .email("mobinchao@hotmail.com")
                .accountState(AccountState.NORMAL)
                .build();
        if (userService.checkUserIfAvailable(userDTO)) {
            Long adminUserId = IdUtil.getId();
            userDTO.setId(adminUserId);
            boolean result = userService.saveEntity(userDTO);
            // 默认分配一个系统管理员的角色、以及所有系统权限
            RbacRoleDTO adminRole = roleService.getByCode(SYSTEM_ADMIN_ROLE_CODE);
            if (null == adminRole) {
                adminRole = RbacRoleDTO.builder()
                        .roleCode(SYSTEM_ADMIN_ROLE_CODE)
                        .roleName("系统管理员")
                        .roleDefault(false)
                        .roleDesc("拥有管理系统基础数据的权限")
                        .build();
                Long adminRoleId = IdUtil.getId();
                adminRole.setId(adminRoleId);
                boolean saveAdminRole = roleService.saveEntity(adminRole);

                // 给系统管理员授权所有权限，以免无法使用系统功能
                if (saveAdminRole) {
                    List<RbacRolePermissionCheckedDTO> apiList = roleService.findRolePermission(adminRoleId, PermissionType.API);
                    List<Long> apiIds = apiList.stream().map(BaseDTO::getId).collect(Collectors.toList());

                    List<RbacRolePermissionCheckedDTO> menuList = roleService.findRolePermission(adminRoleId, PermissionType.MENU);
                    List<Long> menuIds = menuList.stream().map(BaseDTO::getId).collect(Collectors.toList());

                    List<RbacRolePermissionCheckedDTO> funList = roleService.findRolePermission(adminRoleId, PermissionType.FUNCTION);
                    List<Long> funIds = funList.stream().map(BaseDTO::getId).collect(Collectors.toList());

                    // 授权所有api、menu、function给角色
                    boolean grantApiResult = roleService.grantPermission(adminRoleId, apiIds, PermissionType.API);
                    boolean grantMenuResult = roleService.grantPermission(adminRoleId, menuIds, PermissionType.MENU);
                    boolean grantFunResult = roleService.grantPermission(adminRoleId, funIds, PermissionType.FUNCTION);

                    Assert.assertTrue(grantApiResult);
                    Assert.assertTrue(grantMenuResult);
                    Assert.assertTrue(grantFunResult);

                }
            }
            // 授权角色给用户
            boolean grantRoleResult = roleService.grantUser(adminRole.getId(), adminUserId);
            Assert.assertTrue(grantRoleResult);
            Assert.assertTrue(result);
        }
    }

    /**
     * 获取用户DTO
     * @return user info for unit test
     */
    protected RbacUserDTO getUserDTO() {
        RbacUserDTO userDTO = RbacUserDTO.builder()
                .account(ACCOUNT)
                .password(getPassword())
                .nickName(COL_TITLE)
                .email(EMAIL)
                .phoneNumber(PHONE_NUMBER)
                .accountState(AccountState.NORMAL)
                .build();
        userDTO.setId(STATIC_ID);
        return userDTO;
    }


    /**
     * 获取RoleDTO
     * @return  role info for unit test
     */
    protected RbacRoleDTO getRoleDTO() {
        RbacRoleDTO roleDTO = RbacRoleDTO.builder()
                .roleCode(ACCOUNT)
                .roleName(COL_TITLE)
                .roleDefault(false)
                .build();
        roleDTO.setId(STATIC_ID);
        return roleDTO;
    }

    /**
     * 获取permissionDTO
     * @return permission info for unit test
     */
    protected RbacPermissionDTO getPermissionDTO() {
        RbacPermissionDTO permissionDTO = RbacPermissionDTO.builder()
                .permissionCode(ACCOUNT)
                .permissionName(COL_TITLE)
                .permissionType(PermissionType.MENU)
                .pid(ROOT_MENU_ID)
                .build();
        permissionDTO.setId(STATIC_ID);
        return permissionDTO;
    }

    /**
     * 获取单元测试用户密码
     * @return md5 password
     */
    protected String getPassword() {
        return SecureUtil.md5(PASSWORD);
    }

    /**
     * 获取图形校验码信息
     * @return      校验码信息
     */
    protected RbacCaptchaDTO getCaptcha() {
        RbacCaptchaDTO captchaDTO = ticketService.captchaCreate();
        return captchaDTO;
    }

    /**
     * 获取图形校验码文本
     * @param id  校验码ID
     * @return    校验码文本
     */
    protected String getCaptchaCode(String id) {
        String captchaKey = "captcha:valid:" + id;
        return  (String) redisTemplate.opsForValue().get(captchaKey);
    }

}
