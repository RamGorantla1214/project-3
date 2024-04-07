package dfd.rbac.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import dfd.AppBootstrapTest;
import dfd.common.model.dto.PageQueryDTO;
import dfd.common.model.dto.PageResultDTO;
import dfd.rbac.model.dto.RbacConfigDTO;
import dfd.rbac.model.query.RbacConfigQuery;

import javax.annotation.Resource;

/**
 * 系统kv字典单元测试
 * @author binchao
 */
public class RbacConfigServiceTest extends AppBootstrapTest {

    @Resource
    private RbacConfigService configService;


    @Before
    public void init() {
        RbacConfigDTO configDTO = RbacConfigDTO.builder()
                .keyCode(ACCOUNT)
                .keyValue(COL_TITLE)
                .keyDesc(COL_TITLE)
                .build();
        configDTO.setId(STATIC_ID);
        boolean result = configService.saveEntity(configDTO);
        Assert.assertTrue(result);
    }

    @Test
    public void getByCode() {
        RbacConfigDTO configDTO = configService.getByKeyCode(ACCOUNT);
        Assert.assertEquals(COL_TITLE, configDTO.getKeyValue());
    }

    @Test
    public void page() {
        PageQueryDTO<RbacConfigQuery> pageQuery = new PageQueryDTO<>();
        RbacConfigQuery query = RbacConfigQuery.builder().keyCode(ACCOUNT).build();
        pageQuery.setParams(query);
        PageResultDTO<RbacConfigDTO> pageResultDTO = configService.page(pageQuery);
        Assert.assertTrue(pageResultDTO.getTotal() >= 1L);
    }

    @After
    public void done() {
        boolean result = configService.removeById(STATIC_ID);
        Assert.assertTrue(result);
    }
}
