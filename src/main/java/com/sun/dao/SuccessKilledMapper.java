package com.sun.dao;

import com.sun.entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SuccessKilledMapper {
    /**
     * 插入一条详细的购买信息
     *
     * @param seckillId 秒杀商品的Id
     * @param userPhone 购买用户的手机号码
     * @return 成功插入就返回1，否则就返回0
     */
    public int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * 根据秒杀商品的Id查询<code>SuccessKilled</code>
     *
     * @param seckillId 秒杀商品的Id
     * @param userPhone 购买用户的手机号码
     * @return 秒杀商品的明细信息
     */
    public SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

}
