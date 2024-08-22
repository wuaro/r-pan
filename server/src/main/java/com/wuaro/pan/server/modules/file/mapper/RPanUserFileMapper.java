package com.wuaro.pan.server.modules.file.mapper;

import com.wuaro.pan.server.modules.file.context.FileSearchContext;
import com.wuaro.pan.server.modules.file.context.QueryFileListContext;
import com.wuaro.pan.server.modules.file.entity.RPanUserFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuaro.pan.server.modules.file.vo.FileSearchResultVO;
import com.wuaro.pan.server.modules.file.vo.RPanUserFileVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 11391
 * @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Mapper
 * @createDate 2024-03-06 08:59:39
 * @Entity com.wuaro.pan.server.modules.file.entity.RPanUserFile
 */
public interface RPanUserFileMapper extends BaseMapper<RPanUserFile> {

    /**
     * 查询用户的文件列表
     *
     * @param context
     * @return
     */
    /*
    参数：
        1. @Param("param") QueryFileListContext context
            这是方法的参数，使用了 MyBatis 的 @Param 注解，
    返回值：
        1. List<RPanUserFileVO>
    注解：
        1. @Param("param")
            指定了参数的名称为 param，并且参数类型为 QueryFileListContext。
            这个参数会传递给对应的 SQL 查询语句，并且可以在 XML 文件中使用 ${param.xxx} 的方式获取参数对象的属性值。
    方法名：
        selectFileList：对应了 Mapper XML 文件中的 <select> 标签的 id 属性。
    注意事项：
        这是一个 MyBatis Mapper 接口中的方法定义，
        用于执行上面提到的 SQL 查询语句，并将查询结果映射为 RPanUserFileVO 类型的对象列表。
    总体来说
        这个方法的作用是执行指定的 SQL 查询语句，根据传入的 QueryFileListContext 对象的属性值构建查询条件，
        并将查询结果映射为 RPanUserFileVO 类型的对象列表返回。
     */
    List<RPanUserFileVO> selectFileList(@Param("param") QueryFileListContext context);

    /**
     * 文件搜索
     *
     * @param context
     * @return
     */
    List<FileSearchResultVO> searchFile(@Param("param") FileSearchContext context);

}