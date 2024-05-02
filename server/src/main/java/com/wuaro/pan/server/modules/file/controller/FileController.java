package com.wuaro.pan.server.modules.file.controller;

import com.google.common.base.Splitter;
import com.wuaro.pan.core.constants.RPanConstants;
import com.wuaro.pan.core.response.R;
import com.wuaro.pan.core.utils.IdUtil;
import com.wuaro.pan.server.common.utils.UserIdUtil;
import com.wuaro.pan.server.modules.file.constants.FileConstants;
//import com.wuaro.pan.server.modules.file.context.*;
//import com.wuaro.pan.server.modules.file.converter.FileConverter;
import com.wuaro.pan.server.modules.file.context.*;
import com.wuaro.pan.server.modules.file.converter.FileConverter;
import com.wuaro.pan.server.modules.file.enums.DelFlagEnum;
//import com.wuaro.pan.server.modules.file.po.*;
import com.wuaro.pan.server.modules.file.po.*;
import com.wuaro.pan.server.modules.file.service.IUserFileService;
//import com.wuaro.pan.server.modules.file.vo.*;
import com.wuaro.pan.server.modules.file.vo.RPanUserFileVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
//import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

//import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件模块控制器
 */
@RestController
@Validated
@Api(tags = "文件模块")
public class FileController {

    @Autowired
    private IUserFileService iUserFileService;

    @Autowired
    private FileConverter fileConverter;

    /**
     * 查询文件列表
     *
     * @param parentId
     * @param fileTypes
     * @return
     */
    /*
    注解：
        1. @ApiOperation：
            这个注解来自于 Swagger API 文档工具，用于描述接口的作用和用法。在这里，接口的作用是查询文件列表，接受的参数是父文件夹ID和文件类型，返回的结果是一个文件列表。
        2. @GetMapping("files")：
            这个注解表示该接口处理的是 HTTP GET 请求，并且请求的路径是 "/files"。
        3. @NotBlank(message = "父文件夹ID不能为空")
            @NotBlank 是一个来自于 Hibernate Validator 的注解，用于校验字符串类型的参数是否为空或者只包含空格。
            如果传入的父文件夹ID参数为空或者只包含空格，那么会抛出一个带有指定消息的异常，消息内容为 "父文件夹ID不能为空"，这个消息可以用来提示调用方传入的参数有误。
        4. @RequestParam(value = "parentId", required = false)
            @RequestParam 注解用于从请求中获取参数值，并将其绑定到方法的参数上。
            在这个代码中，@RequestParam(value = "parentId", required = false) 表示从请求中获取名为 "parentId" 的参数的值，
            如果该参数不存在或者为空，则使用默认值为 null。required = false 表示该参数是可选的，可以不传递。
        5. @RequestParam(value = "fileTypes", required = false, defaultValue = FileConstants.ALL_FILE_TYPE)
            @RequestParam 注解用于从请求中获取参数值，并将其绑定到方法的参数上。
            这段代码表示从请求中获取名为 "fileTypes" 的参数的值，如果该参数不存在或者为空，则使用默认值 FileConstants.ALL_FILE_TYPE。
    参数，
        两个，分别是父文件夹ID和文件类型。
        @NotBlank 注解表示 parentId 参数不能为空，@RequestParam 注解表示这两个参数是通过请求参数传递的，
        其中 parentId 是必需的，而 fileTypes 则是可选的，默认值为 "ALL_FILE_TYPE"。
    返回值：
        返回的结果是一个 R 对象，包含一个文件列表。
    执行逻辑：
        1. Long realParentId = -1L;
            if (!FileConstants.ALL_FILE_TYPE.equals(parentId)) {
                realParentId = IdUtil.decrypt(parentId);
            }
            这段代码的作用是将接收到的父文件夹ID进行解密，如果解密成功，则将解密后的值赋给 realParentId 变量，否则使用默认值 -1L。
            首先将 realParentId 初始化为默认值 -1L。
            检查接收到的父文件夹ID是否等于 FileConstants.ALL_FILE_TYPE，如果不相等，则执行以下代码块。
            调用 IdUtil.decrypt(parentId) 方法对接收到的父文件夹ID进行解密，并将解密后的值赋给 realParentId 变量。
        2. List<Integer> fileTypeArray = null;
            if (!Objects.equals(FileConstants.ALL_FILE_TYPE, fileTypes)) {
                fileTypeArray = Splitter.on(RPanConstants.COMMON_SEPARATOR)
                                        .splitToList(fileTypes)
                                        .stream()
                                        .map(Integer::valueOf)
                                        .collect(Collectors.toList());
            }
            首先将 fileTypeArray 初始化为 null。
            检查接收到的文件类型字符串是否等于 FileConstants.ALL_FILE_TYPE，如果不相等，则执行以下代码块。
            Splitter.on(RPanConstants.COMMON_SEPARATOR)
                使用指定的分隔符 RPanConstants.COMMON_SEPARATOR 创建一个Splitter对象，用于将字符串拆分成多个子串。
            .splitToList(fileTypes)
                将文件类型字符串 fileTypes 按照指定的分隔符拆分成一个字符串列表。
            .stream()
                将列表转换为流，以便后续进行流式操作。
            .map(Integer::valueOf)
                对流中的每个字符串元素应用 Integer.valueOf 方法，将其转换为整数。
            .collect(Collectors.toList())
                将流中的元素收集到一个新的列表中，最终得到整数类型的文件类型列表 fileTypeArray。
            这段代码的作用是将接收到的文件类型字符串按照指定的分隔符拆分成多个子串，并将这些子串转换为整数，然后将这些整数放入列表中。
        3. QueryFileListContext context = new QueryFileListContext();
            context.setParentId(realParentId);
            context.setFileTypeArray(fileTypeArray);
            context.setUserId(UserIdUtil.get());
            context.setDelFlag(DelFlagEnum.NO.getCode());
            List<RPanUserFileVO> result = iUserFileService.getFileList(context);
            return R.data(result);
            创建 查询文件上下文QueryFileListContext对象context，并设置属性值：
            设置父文件夹ID，即指定要查询的文件夹ID。
            设置文件类型数组，即指定要查询的文件类型。
            设置用户ID，即当前用户的ID。
            设置删除标志，即指定查询未删除的文件。
            调用 iUserFileService.getFileList(context) 方法获取文件列表，传入上面设置好的 context 对象，进行文件查询操作。
            将文件列表结果 result 包装成 R 类型的响应对象，使用 R.data(result) 创建响应对象并返回。
     */
    @ApiOperation(
            value = "查询文件列表",
            notes = "该接口提供了用户插叙某文件夹下面某些文件类型的文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("files")
    public R<List<RPanUserFileVO>> list(@NotBlank(message = "父文件夹ID不能为空") @RequestParam(value = "parentId", required = false) String parentId,
                                        @RequestParam(value = "fileTypes", required = false, defaultValue = FileConstants.ALL_FILE_TYPE) String fileTypes) {
        Long realParentId = -1L;
        if (!FileConstants.ALL_FILE_TYPE.equals(parentId)) {
            realParentId = IdUtil.decrypt(parentId);
        }

        List<Integer> fileTypeArray = null;
        if (!Objects.equals(FileConstants.ALL_FILE_TYPE, fileTypes)) {
            fileTypeArray = Splitter.on(RPanConstants.COMMON_SEPARATOR)
                                    .splitToList(fileTypes)
                                    .stream()
                                    .map(Integer::valueOf)
                                    .collect(Collectors.toList());
        }

        QueryFileListContext context = new QueryFileListContext();
        context.setParentId(realParentId);
        context.setFileTypeArray(fileTypeArray);
        context.setUserId(UserIdUtil.get());
        context.setDelFlag(DelFlagEnum.NO.getCode());

        List<RPanUserFileVO> result = iUserFileService.getFileList(context);
        return R.data(result);
    }

    /**
     * 创建文件夹
     *
     * @param createFolderPO
     * @return
     */
    /*
    注解：
        1. @ApiOperation：
            这个注解来自于 Swagger API 文档工具，用于描述接口的作用和用法。
            在这里，接口的作用是查询文件列表，接受的参数是父文件夹ID和文件类型，返回的结果是一个文件列表。
            value = "创建文件夹"：接口的名称，表示这个接口的作用是创建文件夹。
            notes = "该接口提供了创建文件夹的功能"：接口的详细说明，描述了这个接口的功能。
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE：指定请求的数据格式为 JSON 格式。
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE：指定响应的数据格式为 JSON 格式。
        2. @GetMapping("file/folder")：
            这个注解表示该接口处理的是 HTTP GET 请求，并且请求的路径是 "/file/folder"。
        3. @Validated：
            @Validated 是 Spring 框架中用来进行参数校验的注解。
            在上下文中，它通常与 Spring MVC 的 @RequestBody 注解一起使用，用于对请求体中的参数进行校验。
            具体来说，@Validated 可以放在 Controller 方法的参数上，表示对该参数进行校验。
            在方法参数上使用 @Validated 注解后，Spring 框架会根据对象中的注解（如 @NotNull、@NotBlank、@Min、@Max 等）进行参数校验。
            在 Spring MVC 中，如果一个类中的字段包含了校验注解（例如 @NotBlank、@NotNull 等），并且该类作为方法的参数，
            需要进行参数校验，那么该方法的参数 必须使用 @Validated 或 @Valid 注解来标记！！
            在这里，@Validated 注解用于验证 CreateFolderPO 对象中的字段是否符合规定的校验条件。
            注解进行请求参数的验证，
        4. @RequestBody：
            @RequestBody 注解用于将 HTTP 请求体中的数据绑定到方法的参数上，通常用于处理 POST 请求中的 JSON 数据。
            当客户端发送 POST 请求时，请求体中的 JSON 数据会被映射到被 @RequestBody 注解标记的方法参数上，从而可以在方法中直接使用这些数据。
            说白了就是讲JSON格式的数据映射到PO类中
            这里@RequestBody注解将请求的 JSON 数据映射为 CreateFolderPO 对象。
    参数，
        1. CreateFolderPO createFolderPO
            创建文件夹的实体类
    返回值：
        返回的结果是一个 R 对象，包含加密后的文件夹ID。
    执行逻辑：
        1. CreateFolderContext context = fileConverter.createFolderPO2CreateFolderContext(createFolderPO);
            调用 fileConverter 的方法将 CreateFolderPO 对象转换为 CreateFolderContext 对象。
        2. Long fileId = iUserFileService.createFolder(context);
            调用 iUserFileService 中的 createFolder方法 创建文件夹，并获取返回的文件夹 ID。
        3. return R.data(IdUtil.encrypt(fileId));
            将文件夹 ID 加密后返回给前端。
     */
    @ApiOperation(
            value = "创建文件夹",
            notes = "该接口提供了创建文件夹的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/folder")
    public R<String> createFolder(@Validated @RequestBody CreateFolderPO createFolderPO) {
        CreateFolderContext context = fileConverter.createFolderPO2CreateFolderContext(createFolderPO);
        Long fileId = iUserFileService.createFolder(context);
        return R.data(IdUtil.encrypt(fileId));
    }

    /**
     * 文件重命名
     *
     * @param updateFilenamePO
     * @return
     */
    /*
    注解：
        1. @ApiOperation：
            这个注解来自于 Swagger API 文档工具，用于描述接口的作用和用法。
            在这里，接口的作用是查询文件列表，接受的参数是父文件夹ID和文件类型，返回的结果是一个文件列表。
            value = "创建文件夹"：接口的名称，表示这个接口的作用是创建文件夹。
            notes = "该接口提供了创建文件夹的功能"：接口的详细说明，描述了这个接口的功能。
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE：指定请求的数据格式为 JSON 格式。
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE：指定响应的数据格式为 JSON 格式。
        2. @GetMapping("file")：
            这个注解表示该接口处理的是 HTTP GET 请求，并且请求的路径是 "/file"。
        3. @Validated：
            @Validated 是 Spring 框架中用来进行参数校验的注解。
            在上下文中，它通常与 Spring MVC 的 @RequestBody 注解一起使用，用于对请求体中的参数进行校验。
            具体来说，@Validated 可以放在 Controller 方法的参数上，表示对该参数进行校验。
            在方法参数上使用 @Validated 注解后，Spring 框架会根据对象中的注解（如 @NotNull、@NotBlank、@Min、@Max 等）进行参数校验。
            在 Spring MVC 中，如果一个类中的字段包含了校验注解（例如 @NotBlank、@NotNull 等），并且该类作为方法的参数，
            需要进行参数校验，那么该方法的参数 必须使用 @Validated 或 @Valid 注解来标记！！
            在这里，@Validated 注解用于验证 UpdateFilenamePO 对象中的字段是否符合规定的校验条件。
            注解进行请求参数的验证，
        4. @RequestBody：
            @RequestBody 注解用于将 HTTP 请求体中的数据绑定到方法的参数上，通常用于处理 POST 请求中的 JSON 数据。
            当客户端发送 POST 请求时，请求体中的 JSON 数据会被映射到被 @RequestBody 注解标记的方法参数上，从而可以在方法中直接使用这些数据。
            说白了就是讲JSON格式的数据映射到PO类中
            这里@RequestBody注解将请求的 JSON 数据映射为 UpdateFilenamePO 对象。
    参数，
        1. UpdateFilenamePO updateFilenamePO
            文件重命名的实体类
    返回值：
        R.success()
    执行逻辑：
        1. UpdateFilenameContext context = fileConverter.updateFilenamePO2UpdateFilenameContext(updateFilenamePO);
            调用 fileConverter 的方法将 UpdateFilenamePO 对象转换为 UpdateFilenameContext 对象。
        2. iUserFileService.updateFilename(context);
            调用 iUserFileService 中的 updateFilename 给文件重命名。
     */
    @ApiOperation(
            value = "文件重命名",
            notes = "该接口提供了文件重命名的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PutMapping("file")
    public R updateFilename(@Validated @RequestBody UpdateFilenamePO updateFilenamePO) {
        UpdateFilenameContext context = fileConverter.updateFilenamePO2UpdateFilenameContext(updateFilenamePO);
        iUserFileService.updateFilename(context);
        return R.success();
    }

    /**
     * 批量删除文件
     *
     * @param deleteFilePO
     * @return
     */
    /*
    注解：
        1. @ApiOperation：
            这个注解来自于 Swagger API 文档工具，用于描述接口的作用和用法。
            在这里，接口的作用是查询文件列表，接受的参数是父文件夹ID和文件类型，返回的结果是一个文件列表。
            value = "创建文件夹"：接口的名称，表示这个接口的作用是创建文件夹。
            notes = "该接口提供了创建文件夹的功能"：接口的详细说明，描述了这个接口的功能。
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE：指定请求的数据格式为 JSON 格式。
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE：指定响应的数据格式为 JSON 格式。
        2. @GetMapping("file")：
            这个注解表示该接口处理的是 HTTP GET 请求，并且请求的路径是 "/file"。
        3. @Validated：
            @Validated 是 Spring 框架中用来进行参数校验的注解。
            在上下文中，它通常与 Spring MVC 的 @RequestBody 注解一起使用，用于对请求体中的参数进行校验。
            具体来说，@Validated 可以放在 Controller 方法的参数上，表示对该参数进行校验。
            在方法参数上使用 @Validated 注解后，Spring 框架会根据对象中的注解（如 @NotNull、@NotBlank、@Min、@Max 等）进行参数校验。
            在 Spring MVC 中，如果一个类中的字段包含了校验注解（例如 @NotBlank、@NotNull 等），并且该类作为方法的参数，
            需要进行参数校验，那么该方法的参数 必须使用 @Validated 或 @Valid 注解来标记！！
            在这里，@Validated 注解用于验证 DeleteFilePO 对象中的字段是否符合规定的校验条件。
            注解进行请求参数的验证，
        4. @RequestBody：
            @RequestBody 注解用于将 HTTP 请求体中的数据绑定到方法的参数上，通常用于处理 POST 请求中的 JSON 数据。
            当客户端发送 POST 请求时，请求体中的 JSON 数据会被映射到被 @RequestBody 注解标记的方法参数上，从而可以在方法中直接使用这些数据。
            说白了就是讲JSON格式的数据映射到PO类中
            这里@RequestBody注解将请求的 JSON 数据映射为 DeleteFilePO 对象。
    参数，
        1. DeleteFilePO deleteFilePO
            删除文件的实体类
    返回值：
        R.success()
    执行逻辑：
        1. DeleteFileContext context = fileConverter.deleteFilePO2DeleteFileContext(deleteFilePO);
            调用 fileConverter 的方法将 DeleteFilePO 对象转换为 DeleteFileContext 对象。
        2. String fileIds = deleteFilePO.getFileIds();
            从 DeleteFilePO 对象中获取待删除文件的 ID 字符串。
        3. List<Long> fileIdList = Splitter.on(RPanConstants.COMMON_SEPARATOR)
                                            .splitToList(fileIds)
                                            .stream()
                                            .map(IdUtil::decrypt)
                                            .collect(Collectors.toList());
            逐行解释：
                Splitter.on(RPanConstants.COMMON_SEPARATOR)
                    使用 RPanConstants.COMMON_SEPARATOR（也就是"__,__"） 分隔符创建一个分隔器 Splitter 对象。
                .splitToList(fileIds)
                    将 fileIds 字符串按照分隔符分割为一个字符串列表。
                .stream()
                    将字符串列表转换为流，以便进行后续操作。
                .map(IdUtil::decrypt)
                    对流中的每个元素（即分割出来的子字符串）应用 IdUtil::decrypt 方法进行解密操作。
                .collect(Collectors.toList())
                    将解密后的长整型值收集到一个新的列表中，最终得到 fileIdList，其中包含了解密后的文件 ID 的长整型值。
            总而言之：这段代码是使用 Java 8 的 Stream API 将分割后的文件 ID 列表转换为 Long 类型的文件 ID 列表，
            并且通过 IdUtil::decrypt 方法解密每个文件 ID。最后得到一个解密后的文件 ID 列表。
        3. context.setFileIdList(fileIdList);
            将解密后的文件 ID 列表设置到上下文对象 context 中，表示待删除的文件 ID 列表。
        4. iUserFileService.deleteFile(context);
            调用 iUserFileService 的 deleteFile 方法，传入上下文对象 context，执行实际的文件删除操作。
     */
    @ApiOperation(
            value = "批量删除文件",
            notes = "该接口提供了批量删除文件的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @DeleteMapping("file")
    public R deleteFile(@Validated @RequestBody DeleteFilePO deleteFilePO) {
        DeleteFileContext context = fileConverter.deleteFilePO2DeleteFileContext(deleteFilePO);

        String fileIds = deleteFilePO.getFileIds();
        List<Long> fileIdList = Splitter.on(RPanConstants.COMMON_SEPARATOR)
                                        .splitToList(fileIds)
                                        .stream()
                                        .map(IdUtil::decrypt)
                                        .collect(Collectors.toList());

        context.setFileIdList(fileIdList);
        iUserFileService.deleteFile(context);
        return R.success();
    }

    /**
     * 文件秒传
     *
     * @param secUploadFilePO
     * @return
     */
    /*
    注解：
        1. @ApiOperation：
            这个注解来自于 Swagger API 文档工具，用于描述接口的作用和用法。
            在这里，接口的作用是查询文件列表，接受的参数是父文件夹ID和文件类型，返回的结果是一个文件列表。
            value = "创建文件夹"：接口的名称，表示这个接口的作用是创建文件夹。
            notes = "该接口提供了创建文件夹的功能"：接口的详细说明，描述了这个接口的功能。
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE：指定请求的数据格式为 JSON 格式。
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE：指定响应的数据格式为 JSON 格式。
        2. @GetMapping("file/sec-upload")：
            这个注解表示该接口处理的是 HTTP GET 请求，并且请求的路径是 "/file/sec-upload"。
        3. @Validated：
            @Validated 是 Spring 框架中用来进行参数校验的注解。
            在上下文中，它通常与 Spring MVC 的 @RequestBody 注解一起使用，用于对请求体中的参数进行校验。
            具体来说，@Validated 可以放在 Controller 方法的参数上，表示对该参数进行校验。
            在方法参数上使用 @Validated 注解后，Spring 框架会根据对象中的注解（如 @NotNull、@NotBlank、@Min、@Max 等）进行参数校验。
            在 Spring MVC 中，如果一个类中的字段包含了校验注解（例如 @NotBlank、@NotNull 等），并且该类作为方法的参数，
            需要进行参数校验，那么该方法的参数 必须使用 @Validated 或 @Valid 注解来标记！！
            在这里，@Validated 注解用于验证 SecUploadFilePO 对象中的字段是否符合规定的校验条件。
            注解进行请求参数的验证，
        4. @RequestBody：
            @RequestBody 注解用于将 HTTP 请求体中的数据绑定到方法的参数上，通常用于处理 POST 请求中的 JSON 数据。
            当客户端发送 POST 请求时，请求体中的 JSON 数据会被映射到被 @RequestBody 注解标记的方法参数上，从而可以在方法中直接使用这些数据。
            说白了就是讲JSON格式的数据映射到PO类中
            这里@RequestBody注解将请求的 JSON 数据映射为 SecUploadFilePO 对象。
    参数，
        1. SecUploadFilePO secUploadFilePO
            秒传文件的实体类
    返回值：
        R.success()
    执行逻辑：
        1. SecUploadFileContext context = fileConverter.secUploadFilePO2SecUploadFileContext(secUploadFilePO);
            调用 fileConverter 的方法将 SecUploadFilePO 对象转换为 SecUploadFileContext 对象。
        2. boolean result = iUserFileService.secUpload(context);
            执行秒传操作
        3. 如果有返回值则说明操作成功，否则说明操作失败
     */
    @ApiOperation(
            value = "文件秒传",
            notes = "该接口提供了文件秒传的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/sec-upload")
    public R secUpload(@Validated @RequestBody SecUploadFilePO secUploadFilePO) {
        SecUploadFileContext context = fileConverter.secUploadFilePO2SecUploadFileContext(secUploadFilePO);
        boolean result = iUserFileService.secUpload(context);
        if (result) {
            return R.success();
        }
        return R.fail("文件唯一标识不存在，请手动执行文件上传");
    }

    /**
     * 单文件上传
     *
     * @param fileUploadPO
     * @return
     */
    /*
    注解：
        1. @ApiOperation：
            这个注解来自于 Swagger API 文档工具，用于描述接口的作用和用法。
            在这里，接口的作用是查询文件列表，接受的参数是父文件夹ID和文件类型，返回的结果是一个文件列表。
            value = "单文件上传"：接口的名称，表示这个接口的作用是单文件上传。
            notes = "该接口提供了单文件上传的功能"：接口的详细说明，描述了这个接口的功能。
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE：指定请求的数据格式为 JSON 格式。
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE：指定响应的数据格式为 JSON 格式。
        2. @GetMapping("file/upload")：
            这个注解表示该接口处理的是 HTTP GET 请求，并且请求的路径是 "file/upload"。
        3. @Validated：
            @Validated 是 Spring 框架中用来进行参数校验的注解。
            在上下文中，它通常与 Spring MVC 的 @RequestBody 注解一起使用，用于对请求体中的参数进行校验。
            具体来说，@Validated 可以放在 Controller 方法的参数上，表示对该参数进行校验。
            在方法参数上使用 @Validated 注解后，Spring 框架会根据对象中的注解（如 @NotNull、@NotBlank、@Min、@Max 等）进行参数校验。
            在 Spring MVC 中，如果一个类中的字段包含了校验注解（例如 @NotBlank、@NotNull 等），并且该类作为方法的参数，
            需要进行参数校验，那么该方法的参数 必须使用 @Validated 或 @Valid 注解来标记！！
            在这里，@Validated 注解用于验证 SecUploadFilePO 对象中的字段是否符合规定的校验条件。
            注解进行请求参数的验证
    参数，
        1. FileUploadPO fileUploadPO
            单文件上传的实体类
    返回值：
        R.success()
    执行逻辑：
        1. FileUploadContext context = fileConverter.fileUploadPO2FileUploadContext(fileUploadPO);
            调用 fileConverter 的方法将 FileUploadPO 对象转换为 FileUploadContext 对象。
        2. iUserFileService.upload(context);
            执行单文件上传
        3. 如果有返回值则说明操作成功，否则说明操作失败
     */
    @ApiOperation(
            value = "单文件上传",
            notes = "该接口提供了单文件上传的功能",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/upload")
    public R upload(@Validated FileUploadPO fileUploadPO) {
        FileUploadContext context = fileConverter.fileUploadPO2FileUploadContext(fileUploadPO);
        iUserFileService.upload(context);
        return R.success();
    }

//    @ApiOperation(
//            value = "文件分片上传",
//            notes = "该接口提供了文件分片上传的功能",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
//    )
//    @PostMapping("file/chunk-upload")
//    public R<FileChunkUploadVO> chunkUpload(@Validated FileChunkUploadPO fileChunkUploadPO) {
//        FileChunkUploadContext context = fileConverter.fileChunkUploadPO2FileChunkUploadContext(fileChunkUploadPO);
//        FileChunkUploadVO vo = iUserFileService.chunkUpload(context);
//        return R.data(vo);
//    }
//
//    @ApiOperation(
//            value = "查询已经上传的文件分片列表",
//            notes = "该接口提供了查询已经上传的文件分片列表的功能",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
//    )
//    @GetMapping("file/chunk-upload")
//    public R<UploadedChunksVO> getUploadedChunks(@Validated QueryUploadedChunksPO queryUploadedChunksPO) {
//        QueryUploadedChunksContext context = fileConverter.queryUploadedChunksPO2QueryUploadedChunksContext(queryUploadedChunksPO);
//        UploadedChunksVO vo = iUserFileService.getUploadedChunks(context);
//        return R.data(vo);
//    }
//
//    @ApiOperation(
//            value = "文件分片合并",
//            notes = "该接口提供了文件分片合并的功能",
//            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
//    )
//    @PostMapping("file/merge")
//    public R mergeFile(@Validated @RequestBody FileChunkMergePO fileChunkMergePO) {
//        FileChunkMergeContext context = fileConverter.fileChunkMergePO2FileChunkMergeContext(fileChunkMergePO);
//        iUserFileService.mergeFile(context);
//        return R.success();
//    }
//
//    @ApiOperation(
//            value = "文件下载",
//            notes = "该接口提供了文件下载的功能",
//            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
//            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
//    )
//    @GetMapping("file/download")
//    public void download(@NotBlank(message = "文件ID不能为空") @RequestParam(value = "fileId", required = false) String fileId,
//                         HttpServletResponse response) {
//        FileDownloadContext context = new FileDownloadContext();
//        context.setFileId(IdUtil.decrypt(fileId));
//        context.setResponse(response);
//        context.setUserId(UserIdUtil.get());
//        iUserFileService.download(context);
//    }
//
//    @ApiOperation(
//            value = "文件预览",
//            notes = "该接口提供了文件预览的功能",
//            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
//            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
//    )
//    @GetMapping("file/preview")
//    public void preview(@NotBlank(message = "文件ID不能为空") @RequestParam(value = "fileId", required = false) String fileId,
//                        HttpServletResponse response) {
//        FilePreviewContext context = new FilePreviewContext();
//        context.setFileId(IdUtil.decrypt(fileId));
//        context.setResponse(response);
//        context.setUserId(UserIdUtil.get());
//        iUserFileService.preview(context);
//    }
//
//    @ApiOperation(
//            value = "查询文件夹树",
//            notes = "该接口提供了查询文件夹树的功能",
//            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
//    )
//    @GetMapping("file/folder/tree")
//    public R<List<FolderTreeNodeVO>> getFolderTree() {
//        QueryFolderTreeContext context = new QueryFolderTreeContext();
//        context.setUserId(UserIdUtil.get());
//        List<FolderTreeNodeVO> result = iUserFileService.getFolderTree(context);
//        return R.data(result);
//    }
//
//    @ApiOperation(
//            value = "文件转移",
//            notes = "该接口提供了文件转移的功能",
//            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
//    )
//    @PostMapping("file/transfer")
//    public R transfer(@Validated @RequestBody TransferFilePO transferFilePO) {
//        String fileIds = transferFilePO.getFileIds();
//        String targetParentId = transferFilePO.getTargetParentId();
//        List<Long> fileIdList = Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(fileIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
//        TransferFileContext context = new TransferFileContext();
//        context.setFileIdList(fileIdList);
//        context.setTargetParentId(IdUtil.decrypt(targetParentId));
//        context.setUserId(UserIdUtil.get());
//        iUserFileService.transfer(context);
//        return R.success();
//    }
//
//    @ApiOperation(
//            value = "文件复制",
//            notes = "该接口提供了文件复制的功能",
//            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
//    )
//    @PostMapping("file/copy")
//    public R copy(@Validated @RequestBody CopyFilePO copyFilePO) {
//        String fileIds = copyFilePO.getFileIds();
//        String targetParentId = copyFilePO.getTargetParentId();
//        List<Long> fileIdList = Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(fileIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
//        CopyFileContext context = new CopyFileContext();
//        context.setFileIdList(fileIdList);
//        context.setTargetParentId(IdUtil.decrypt(targetParentId));
//        context.setUserId(UserIdUtil.get());
//        iUserFileService.copy(context);
//        return R.success();
//    }
//
//    @ApiOperation(
//            value = "文件搜索",
//            notes = "该接口提供了文件搜索的功能",
//            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
//    )
//    @GetMapping("file/search")
//    public R<List<FileSearchResultVO>> search(@Validated FileSearchPO fileSearchPO) {
//        FileSearchContext context = new FileSearchContext();
//        context.setKeyword(fileSearchPO.getKeyword());
//        context.setUserId(UserIdUtil.get());
//        String fileTypes = fileSearchPO.getFileTypes();
//        if (StringUtils.isNotBlank(fileTypes) && !Objects.equals(FileConstants.ALL_FILE_TYPE, fileTypes)) {
//            List<Integer> fileTypeArray = Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(fileTypes).stream().map(Integer::valueOf).collect(Collectors.toList());
//            context.setFileTypeArray(fileTypeArray);
//        }
//        List<FileSearchResultVO> result = iUserFileService.search(context);
//        return R.data(result);
//    }
//
//    @ApiOperation(
//            value = "查询面包屑列表",
//            notes = "该接口提供了查询面包屑列表的功能",
//            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
//    )
//    @GetMapping("file/breadcrumbs")
//    public R<List<BreadcrumbVO>> getBreadcrumbs(@NotBlank(message = "文件ID不能为空") @RequestParam(value = "fileId", required = false) String fileId) {
//        QueryBreadcrumbsContext context = new QueryBreadcrumbsContext();
//        context.setFileId(IdUtil.decrypt(fileId));
//        context.setUserId(UserIdUtil.get());
//        List<BreadcrumbVO> result = iUserFileService.getBreadcrumbs(context);
//        return R.data(result);
//    }


}
