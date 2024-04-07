package dfd.rbac.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import dfd.AppBootstrapTest;
import dfd.common.model.dto.PageQueryDTO;
import dfd.common.model.dto.PageResultDTO;
import dfd.common.model.vo.AntTreeNodeVO;
import dfd.rbac.common.model.enums.PermissionType;
import dfd.rbac.model.dto.RbacRoleDTO;
import dfd.rbac.model.dto.RbacRoleUserCheckedDTO;
import dfd.rbac.model.query.RbacRolePermissionQuery;
import dfd.rbac.model.query.RbacRoleQuery;
import dfd.rbac.model.query.RbacRoleUserQuery;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 角色管理单元测试
 * @author binchao
 */
public class RbacRoleServiceTest extends AppBootstrapTest {

    @Resource
    private RbacRoleUserService roleUserService;

    @Before
    public void init() {
        boolean uResult = userService.saveEntity(getUserDTO());
        Assert.assertTrue(uResult);

        boolean result = roleService.saveEntity(getRoleDTO());
        Assert.assertTrue(result);

        boolean pResult = permissionService.saveEntity(getPermissionDTO());
        Assert.assertTrue(pResult);
    }


    @Test
    public void roleAuth() {

        // 权限授权给角色
        List<Long> permissionIds = Arrays.asList(STATIC_ID);

        grantPermission(permissionIds);

        List<String> rolePermissionList = roleService.findRoleGrantedPermission(STATIC_ID, PermissionType.MENU);
        Assert.assertEquals(1, rolePermissionList.size());

        // 角色授权给用户
        boolean result = roleService.grantUser(STATIC_ID, STATIC_ID);
        Assert.assertTrue(result);

        // 统计角色用户授权数量
        long authCount = roleUserService.countRoleUserAuth(STATIC_ID);
        Assert.assertEquals(1, authCount);

        // 查询用户角色编码列表
        List<String> userRoleCodeList = roleService.findUserRoleCode(STATIC_ID);
        Assert.assertEquals(1, userRoleCodeList.size());

        // 查询用户权限编码列表
        List<String> userPermissionCodeList = roleService.findUserPermissionCode(STATIC_ID);
        Assert.assertEquals(1, userPermissionCodeList.size());

        // 刷新用户权限
        boolean updateCacheResult = roleService.updateUserPermissionCacheByUserId(STATIC_ID);
        Assert.assertTrue(updateCacheResult);

        // 移除用户角色授权
        boolean removeResult = roleService.revokeUser(STATIC_ID, STATIC_ID);
        Assert.assertTrue(removeResult);


        // 移除角色权限授权
        permissionIds = new ArrayList<>(0);
        grantPermission(permissionIds);
    }

    @Test
    public void getByCode() {
        RbacRoleDTO roleDTO = roleService.getByCode(ACCOUNT);
        Assert.assertNotNull(roleDTO);
    }

    @Test
    public void page() {
        PageQueryDTO<RbacRoleQuery> pageQuery = new PageQueryDTO<>();
        RbacRoleQuery query = RbacRoleQuery.builder().roleCode(ACCOUNT).build();
        pageQuery.setParams(query);
        PageResultDTO<RbacRoleDTO> pageResultDTO =  roleService.page(pageQuery);
        Assert.assertTrue(1 == pageResultDTO.getTotal());
    }

    @Test
    public void findRolePermissionTree() {
        RbacRolePermissionQuery query = RbacRolePermissionQuery.builder()
                .roleId(STATIC_ID)
                .permissionType(PermissionType.MENU)
                .build();
        // 权限授权给角色
        List<Long> permissionIds = Arrays.asList(STATIC_ID);
        grantPermission(permissionIds);
        AntTreeNodeVO treeNodeVO = roleService.findRolePermissionTree(query);
        Assert.assertNotNull(treeNodeVO);

        try {
            Optional<AntTreeNodeVO> tNode = treeNodeVO.getChildren().stream().filter(childrenNode -> STATIC_ID.toString().equals(childrenNode.getKey())).findFirst();
            Assert.assertNotNull(tNode);
            Assert.assertEquals(true, tNode.get().getChecked());
        } finally {
            grantPermission(new ArrayList<>(0));
        }
    }

    @Test
    public void authRolePage() {
        PageQueryDTO<RbacRoleUserQuery> pageQuery = new PageQueryDTO<>();
        RbacRoleUserQuery params = RbacRoleUserQuery.builder().userId(STATIC_ID).roleCode(ACCOUNT).roleName(COL_TITLE).build();
        pageQuery.setParams(params);
        PageResultDTO<RbacRoleUserCheckedDTO> pageResultDTO = roleService.authRolePage(pageQuery);
        Assert.assertTrue(pageResultDTO.getTotal() >= 1);
    }

    /**
     * 授权角色权限
     * @param permissionIds
     */
    private void grantPermission(List<Long> permissionIds) {
        boolean authPermissionResultBefore = roleService.grantPermission(STATIC_ID, permissionIds, PermissionType.MENU);
        boolean authPermissionResult = roleService.refreshRoleUserPermission(STATIC_ID);

        Assert.assertTrue(authPermissionResultBefore);
        Assert.assertTrue(authPermissionResult);
    }

    @After
    public void done() {
        boolean uResult = userService.removeById(STATIC_ID);
        Assert.assertTrue(uResult);
        boolean result = roleService.removeById(STATIC_ID);
        Assert.assertTrue(result);
        boolean pResult = permissionService.removeById(STATIC_ID);
        Assert.assertTrue(pResult);
    }
}
