package com.wuaro.pan.core.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ArrayUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.wuaro.pan.core.constants.RPanConstants;
import com.wuaro.pan.core.exception.RPanBusinessException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 雪花算法id生成器
 */
public class IdUtil {

    /**
     * 工作id 也就是机器id
     */
    private static long workerId;

    /**
     * 数据中心id
     */
    private static long dataCenterId;

    /**
     * 序列号
     */
    private static long sequence;

    /**
     * 初始时间戳
     */
    private static long startTimestamp = 1288834974657L;

    /**
     * 工作id长度为5位
     */
    private static long workerIdBits = 5L;

    /**
     * 数据中心id长度为5位
     */
    private static long dataCenterIdBits = 5L;

    /**
     * 工作id最大值
     */
    private static long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /**
     * 数据中心id最大值
     */
    private static long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);

    /**
     * 序列号长度
     */
    private static long sequenceBits = 12L;

    /**
     * 序列号最大值
     */
    private static long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 工作id需要左移的位数，12位
     */
    private static long workerIdShift = sequenceBits;

    /**
     * 数据id需要左移位数 12+5=17位
     */
    private static long dataCenterIdShift = sequenceBits + workerIdBits;

    /**
     * 时间戳需要左移位数 12+5+5=22位
     */
    private static long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;

    /**
     * 上次时间戳，初始值为负数
     */
    private static long lastTimestamp = -1L;

    static {
        workerId = getMachineNum() & maxWorkerId;
        dataCenterId = getMachineNum() & maxDataCenterId;
        sequence = 0L;
    }

    /**
     * 获取机器编号
     *
     * @return
     */
    private static long getMachineNum() {
        long machinePiece;
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> e = null;
        try {
            e = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        while (e.hasMoreElements()) {
            NetworkInterface ni = e.nextElement();
            sb.append(ni.toString());
        }
        machinePiece = sb.toString().hashCode();
        return machinePiece;
    }

    /**
     * 获取时间戳，并与上次时间戳比较
     *
     * @param lastTimestamp
     * @return
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取系统时间戳
     *
     * @return
     */
    private static long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 生成ID
     *
     * @return
     */
    public synchronized static Long get() {
        long timestamp = timeGen();
        // 获取当前时间戳如果小于上次时间戳，则表示时间戳获取出现异常
        if (timestamp < lastTimestamp) {
            System.err.printf("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp);
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        // 获取当前时间戳如果等于上次时间戳
        // 说明：还处在同一毫秒内，则在序列号加1；否则序列号赋值为0，从0开始。
        // 0 - 4095
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        //将上次时间戳值刷新
        lastTimestamp = timestamp;

        /**
         * 返回结果：
         * (timestamp - twepoch) << timestampLeftShift) 表示将时间戳减去初始时间戳，再左移相应位数
         * (datacenterId << datacenterIdShift) 表示将数据id左移相应位数
         * (workerId << workerIdShift) 表示将工作id左移相应位数
         * | 是按位或运算符，例如：x | y，只有当x，y都为0的时候结果才为0，其它情况结果都为1。
         * 因为个部分只有相应位上的值有意义，其它位上都是0，所以将各部分的值进行 | 运算就能得到最终拼接好的id
         */
        return ((timestamp - startTimestamp) << timestampLeftShift) |
                (dataCenterId << dataCenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    /**
     * 加密ID（AES-128加密）
     * Long类型的ID经过AES-128加密，返回加密后的字符串
     * Long -> 字符数组 -> AES-128加密后的字符串
     *
     * @return
     */
    /*
    参数：
        1. Long id
            注册生成的用户id
    执行逻辑：
        1. 检查输入的 id 是否不为空。
        2. ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            分配一个大小为8字节的 ByteBuffer。
            关于ByteBuffer.allocate(8)：
                是一个Java NIO中的方法，用于分配一个指定大小的字节缓冲区。
                在这里，allocate(8) 分配了一个容量为8字节的字节缓冲区，用于存储数据。
        3. byteBuffer.putLong(0, id);
            将 id 作为Long值放入 ByteBuffer字节缓冲区 中的位置0（从0开始依次存放）。
        4. byte[] content = byteBuffer.array();
            将 ByteBuffer 转换为字节数组（content）。
        5. byte[] encrypt = AES128Util.aesEncrypt(content);
            使用AES-128加密对字节数组进行加密（一个进行AES-128加密的方法）。
        6. return Base64.encode(encrypt);
            使用Base64编码对加密后的字节数组进行编码（Base64.encode）。
            Base64.encode(encrypt) 是将给定的字节数组使用 Base64 编码转换为字符串。
            返回编码后的字符串。
     */
    public static String encrypt(Long id) {
        if (Objects.nonNull(id)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            byteBuffer.putLong(0, id);
            byte[] content = byteBuffer.array();
            byte[] encrypt = AES128Util.aesEncrypt(content);
            return Base64.encode(encrypt);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 解密ID（AES-128解密）
     * 加密后的字符串经过AES-128解密，返回Long类型的ID
     * AES-128加密后的字符串 -> 字符数组 -> Long
     *
     * @param decryptId
     * @return
     */
    /*
    参数：
        1. String decryptId
            加密过的Id字符串
    执行逻辑：
        1. if (StringUtils.isNotBlank(decryptId))
            判断decryptId字符串是否不为空 且 不全是空白字符。如果满足条件则进行下面的解密操作。如果decryptId为空，则抛出异常。
        2.byte[] encrypt = Base64.decode(decryptId);
            是一个 Base64 解码的操作，它将 Base64 编码的字符串decryptId解码成字节数组
            说白了就是从字符串变成字符数组
        3. AES128Util.aesDecode(encrypt)
            是一个 AES 解密操作，它将加密的字节数组解密成原始数据。
        4. if (ArrayUtil.isNotEmpty(content)) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(content);
                return byteBuffer.getLong();
            }
            ArrayUtil.isNotEmpty(content) 检查 content 不为空，即解密后的字节数组有数据。
            ByteBuffer.wrap(content) 将 content 包装为 ByteBuffer，ByteBuffer 是 Java NIO 中用于处理字节数据的类。
            byteBuffer.getLong() 从 ByteBuffer 中获取一个长整型数据。
            这段代码的作用是将 AES 解密后的字节数组转换为长整型数据。
            如果解密后的内容不为空，则将其转换为 ByteBuffer，并从中读取 Long 类型的值，然后返回该值。
            如果解密后的内容为空，则抛出异常。
     */
    public static Long decrypt(String decryptId) {
        if (StringUtils.isNotBlank(decryptId)) {
            byte[] encrypt = Base64.decode(decryptId);
            byte[] content = AES128Util.aesDecode(encrypt);
            if (ArrayUtil.isNotEmpty(content)) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(content);
                return byteBuffer.getLong();
            }
            throw new RPanBusinessException("AES128Util.aesDecode fail");
        }
        throw new RPanBusinessException("the decryptId can not be empty");
    }

    /**
     * 解密多个加密ID拼接的字符串
     *
     * @param decryptIdStr
     * @return
     */
    public static List<Long> decryptIdList(String decryptIdStr) {
        if (StringUtils.isBlank(decryptIdStr)) {
            return Lists.newArrayList();
        }
        List<String> decryptIdList = Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(decryptIdStr);
        if (CollectionUtils.isEmpty(decryptIdList)) {
            return Lists.newArrayList();
        }
        List<Long> result = decryptIdList.stream().map(IdUtil::decrypt).collect(Collectors.toList());
        return result;
    }

    public static void main(String[] args) {
        System.out.println(encrypt(1664952265857654784L));
        System.out.println(encrypt(get()));
    }

}
