package com.wuaro.pan.core.utils;

import cn.hutool.core.date.DateUtil;
import com.wuaro.pan.core.constants.RPanConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

/**
 * 公用的文件工具类
 */
public class FileUtils {

    /**
     * 获取文件的后缀
     *
     * @param filename
     * @return
     */
    public static String getFileSuffix(String filename) {
        if (StringUtils.isBlank(filename) || filename.lastIndexOf(RPanConstants.POINT_STR) == RPanConstants.MINUS_ONE_INT) {
            return RPanConstants.EMPTY_STR;
        }
        return filename.substring(filename.lastIndexOf(RPanConstants.POINT_STR)).toLowerCase();
    }

    /**
     * 获取文件的类型
     *
     * @param filename
     * @return
     */
    public static String getFileExtName(String filename) {
        if (StringUtils.isBlank(filename) || filename.lastIndexOf(RPanConstants.POINT_STR) == RPanConstants.MINUS_ONE_INT) {
            return RPanConstants.EMPTY_STR;
        }
        return filename.substring(filename.lastIndexOf(RPanConstants.POINT_STR) + RPanConstants.ONE_INT).toLowerCase();
    }

    /**
     * 通过文件大小转化文件大小的展示名称
     *
     * @param totalSize
     * @return
     */
    public static String byteCountToDisplaySize(Long totalSize) {
        if (Objects.isNull(totalSize)) {
            return RPanConstants.EMPTY_STR;
        }
        return org.apache.commons.io.FileUtils.byteCountToDisplaySize(totalSize);
    }

    /**
     * 批量删除物理文件
     *
     * @param realFilePathList
     */
    /*
    参数：
        1. 要删除的文件列表
    执行逻辑：
        1. 如果要删除的文件列表为空，则直接返回
        2. 如果不为空，则遍历每个文件的路径，并使用org.apache.commons.io.FileUtils.forceDelete方法强制删除
            这里之所以使用org.apache.commons.io.FileUtils.forceDelete方法
            是因为这是删除本地的（注意是本地的！！！）物理文件，该文件就存储在当前计算机上！
     */
    public static void deleteFiles(List<String> realFilePathList) throws IOException {
        if (CollectionUtils.isEmpty(realFilePathList)) {
            return;
        }
        for (String realFilePath : realFilePathList) {
            org.apache.commons.io.FileUtils.forceDelete(new File(realFilePath));
        }
    }

    /**
     * 生成文件的存储路径
     *
     * 生成规则：基础路径 + 年 + 月 + 日 + 随机的文件名称
     *
     * @param basePath
     * @param filename
     * @return
     */
    /*
    注意：
        1. StringBuffer是一个操作字符串的工具类，append方法用于拼接字符串
        2. 关于File.separator：
            File.separator 是一个 Java 中的常量，它表示当前操作系统的文件分隔符。
            在不同的操作系统中，文件路径的分隔符是不同的：
                1. 在 Windows 系统中，文件路径的分隔符是反斜杠 \。
                2. 在 Unix/Linux 系统中，文件路径的分隔符是正斜杠 /。
        3. 关于UUIDUtil.getUUID()：
            会生成一个类似550e8400-e29b-41d4-a716-446655440000的随机数
        4. 文件路径：
            基础路径\年\月\日\随机数+文件名称
     */
    public static String generateStoreFileRealPath(String basePath, String filename) {
        return new StringBuffer(basePath)
                .append(File.separator)
                .append(DateUtil.thisYear())
                .append(File.separator)
                .append(DateUtil.thisMonth() + 1)
                .append(File.separator)
                .append(DateUtil.thisDayOfMonth())
                .append(File.separator)
                .append(UUIDUtil.getUUID())
                .append(getFileSuffix(filename))
                .toString();
    }

    /**
     * 将文件的输入流写入到文件中(文件上传的最核心方法，使用零拷贝)
     * 使用底层的sendfile零拷贝来提高传输效率
     *
     * @param inputStream
     * @param targetFile
     * @param totalSize
     */
    /*
    执行逻辑：
        1. 创建文件
        2. 使用 RandomAccessFile 类打开目标文件以进行读写操作
            RandomAccessFile 允许对文件进行读取和写入操作，并且可以从文件的任意位置开始
        3. 获取与 RandomAccessFile 关联的 FileChannel
            FileChannel 是 Java NIO（New I/O）中的一个类，用于对文件进行高效的 I/O 操作
        4. 使用 Channels 工具类的 newChannel 方法将 InputStream 转换为 ReadableByteChannel
            ReadableByteChannel 是 Java NIO 中的一个接口，用于从通道中读取字节
        5. 使用 FileChannel 的 transferFrom 方法将输入通道中的数据传输到输出通道
            这个方法尝试从源通道（inputChannel）读取 totalSize 字节的数据，并写入到目标通道（outputChannel）
            第二个参数 0L 是输出通道中的偏移量，表示从文件的哪个位置开始写入
            如果 totalSize 为 null 或未知，则可能需要使用其他逻辑来确定何时停止传输
        6. 关闭输入通道
            关闭通道会释放与之关联的资源
        7. 关闭输出通道
        8. 关闭 RandomAccessFile
            注意：在关闭 FileChannel 后，通常不需要再显式关闭 RandomAccessFile，因为 FileChannel 的关闭会关闭它
        9. 关闭输入流
            注意：这里的 inputStream.close() 是冗余的，因为 inputChannel 在关闭时已经关闭了其底层的流
            但为了代码的完整性或明确性，有时开发者会显式关闭它
     */
    public static void writeStream2File(InputStream inputStream, File targetFile, Long totalSize) throws IOException {
        createFile(targetFile);

        RandomAccessFile randomAccessFile = new RandomAccessFile(targetFile, "rw");
        FileChannel outputChannel = randomAccessFile.getChannel();
        ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
        outputChannel.transferFrom(inputChannel, 0L, totalSize);
        inputChannel.close();
        outputChannel.close();
        randomAccessFile.close();
        inputStream.close();
    }

    /**
     * 创建文件
     * 包含父文件一起视情况去创建
     *
     * @param targetFile
     */
    public static void createFile(File targetFile) throws IOException {
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        targetFile.createNewFile();
    }

    /**
     * 生成默认的文件存储路径
     *
     * 生成规则：当前登录用户的文件目录 + rpan
     *
     * @return
     */
    /*
    注意：
        1. 关于System.getProperty("user.home")：
            是一个Java系统属性，用于获取当前用户的主目录路径。
            例如，
                1. 在Windows系统上，System.getProperty("user.home") 可能返回
                    类似 "C:\Users username" 的路径，其中 username 是当前用户的用户名。
                2. 而在类Unix/Linux系统上，可能返回类似 /home/username 的路径。
     */
    public static String generateDefaultStoreFileRealPath() {
        return new StringBuffer(System.getProperty("user.home"))
                .append(File.separator)
                .append("rpan")
                .toString();
    }

    /**
     * 生成默认的文件分片的存储路径前缀
     *
     * @return
     */
    public static String generateDefaultStoreFileChunkRealPath() {
        return new StringBuffer(System.getProperty("user.home"))
                .append(File.separator)
                .append("rpan")
                .append(File.separator)
                .append("chunks")
                .toString();
    }

    /**
     * 生成文件分片的存储路径
     *
     * 生成规则：基础路径 + 年 + 月 + 日 + 唯一标识 + 随机的文件名称 + __,__ + 文件分片的下标
     *
     * @param basePath
     * @param identifier
     * @param chunkNumber
     * @return
     */
    public static String generateStoreFileChunkRealPath(String basePath, String identifier, Integer chunkNumber) {
        return new StringBuffer(basePath)
                .append(File.separator)
                .append(DateUtil.thisYear())
                .append(File.separator)
                .append(DateUtil.thisMonth() + 1)
                .append(File.separator)
                .append(DateUtil.thisDayOfMonth())
                .append(File.separator)
                .append(identifier)
                .append(File.separator)
                .append(UUIDUtil.getUUID())
                .append(RPanConstants.COMMON_SEPARATOR)
                .append(chunkNumber)
                .toString();
    }

    /**
     * 追加写文件
     *
     * @param target
     * @param source
     */
    public static void appendWrite(Path target, Path source) throws IOException {
        Files.write(target, Files.readAllBytes(source), StandardOpenOption.APPEND);
    }

    /**
     * 利用零拷贝技术读取文件内容并写入到文件的输出流中
     *
     * @param fileInputStream
     * @param outputStream
     * @param length
     * @throws IOException
     */
    public static void writeFile2OutputStream(FileInputStream fileInputStream, OutputStream outputStream, long length) throws IOException {
        FileChannel fileChannel = fileInputStream.getChannel();
        WritableByteChannel writableByteChannel = Channels.newChannel(outputStream);
        fileChannel.transferTo(RPanConstants.ZERO_LONG, length, writableByteChannel);
        outputStream.flush();
        fileInputStream.close();
        outputStream.close();
        fileChannel.close();
        writableByteChannel.close();
    }

    /**
     * 普通的流对流数据传输
     *
     * @param inputStream
     * @param outputStream
     */
    public static void writeStream2StreamNormal(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != RPanConstants.MINUS_ONE_INT) {
            outputStream.write(buffer, RPanConstants.ZERO_INT, len);
        }
        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }

    /**
     * 获取文件的content-type
     *
     * @param filePath
     * @return
     */
    public static String getContentType(String filePath) {
        //利用nio提供的类判断文件ContentType
        File file = new File(filePath);
        String contentType = null;
        try {
            contentType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //若失败则调用另一个方法进行判断
        if (StringUtils.isBlank(contentType)) {
            contentType = new MimetypesFileTypeMap().getContentType(file);
        }
        return contentType;
    }

}
