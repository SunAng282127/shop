package com.sun.dto;

import com.sun.entity.SuccessKilled;
import com.sun.enums.SeckillStatEnum;

public class SeckillExecution {
    //秒杀商品Id
    private long seckillId;
    //执行秒杀结果的状态
    private SeckillStatEnum statEnum;
    //当秒杀成功时，需要传递秒杀结果的对象回去
    private SuccessKilled successKilled;

    public SeckillExecution() {
    }

    //秒杀成功返回的实体
    public SeckillExecution(long seckillId, SeckillStatEnum statEnum, SuccessKilled successKilled) {
        this.seckillId = seckillId;
        this.statEnum = statEnum;
        this.successKilled = successKilled;
    }

    //秒杀失败返回的实体
    public SeckillExecution(long seckillId, SeckillStatEnum statEnum) {
        this.seckillId = seckillId;
        this.statEnum = statEnum;
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public SeckillStatEnum getStatEnum() {
        return statEnum;
    }

    public void setStatEnum(SeckillStatEnum statEnum) {
        this.statEnum = statEnum;
    }

    public SuccessKilled getSuccessKilled() {
        return successKilled;
    }

    public void setSuccessKilled(SuccessKilled successKilled) {
        this.successKilled = successKilled;
    }

    @Override
    public String toString() {
        return "SeckillExecution{" +
                "秒杀的商品Id=" + seckillId +
                ", 秒杀状态=" + statEnum +
                ", 秒杀成功商品信息=" + successKilled +
                '}';
    }
}
