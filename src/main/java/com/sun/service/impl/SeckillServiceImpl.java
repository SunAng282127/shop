package com.sun.service.impl;

import com.sun.dao.SeckillMapper;
import com.sun.dao.SuccessKilledMapper;
import com.sun.dto.Exposer;
import com.sun.dto.SeckillExecution;
import com.sun.entity.Seckill;
import com.sun.entity.SuccessKilled;
import com.sun.enums.SeckillStatEnum;
import com.sun.exception.RepeatKillException;
import com.sun.exception.SeckillCloseException;
import com.sun.exception.SeckillException;
import com.sun.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //加入一个盐值,用于混淆
    private final String salt = "thisIsASaltValue";

    @Autowired
    private SeckillMapper seckillMapper;
    @Autowired
    private SuccessKilledMapper successKilledMapper;

    /**
     * 查询全部的秒杀记录
     *
     * @return 数据库中所有的秒杀记录
     */
    @Override
    public List<Seckill> getSeckillList() {
        return seckillMapper.queryAll(0, 8);
    }

    /**
     * 查询单个秒杀记录
     *
     * @param seckillId 秒杀记录的Id
     * @return 根据Id查询出来的记录信息
     */
    @Override
    public Seckill getById(long seckillId) {
        return seckillMapper.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        // 根据秒杀的ID去查询是否存在这个商品
        Seckill seckill = seckillMapper.queryById(seckillId);
        if (seckill == null) {
            logger.warn("查询不到这个秒杀产品的记录");
            return new Exposer(false, seckillId);
        }

        // 判断是否还没到秒杀时间或者是过了秒杀时间
        LocalDateTime startTime = seckill.getStartTime();
        LocalDateTime endTime = seckill.getEndTime();
        LocalDateTime nowTime = LocalDateTime.now();
        //   开始时间大于现在的时候说明没有开始秒杀活动    秒杀活动结束时间小于现在的时间说明秒杀已经结束了
       /* if (!nowTime.isAfter(startTime)) {
            logger.info("现在的时间不在开始时间后面，未开启秒杀");
            return new Exposer(false, seckillId, nowTime, startTime, endTime);
        }
        if (!nowTime.isBefore(endTime)) {
            logger.info("现在的时间不在结束的时间之前，可以进行秒杀");
            return new Exposer(false, seckillId, nowTime, startTime, endTime);
        }*/
        if (nowTime.isAfter(startTime) && nowTime.isBefore(endTime)) {
            //秒杀开启，返回秒杀商品的id,用给接口加密的md5
            String md5 = getMd5(seckillId);
            return new Exposer(true, md5, seckillId);
        }
        return new Exposer(false, seckillId, nowTime, startTime, endTime);
    }

    private String getMd5(long seckillId) {
        String base = seckillId + "/" + salt;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    /**
     * 执行秒杀操作，失败的，失败的我们就抛出异常
     *
     * @param seckillId 秒杀的商品Id
     * @param userPhone 手机号码
     * @param md5       MD5加密值
     * @return 根据不同的结果返回不同的实体信息
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    @Transactional
    @Override
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getMd5(seckillId))) {
            logger.error("秒杀数据被篡改");
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀业务逻辑
        LocalDateTime nowTime = LocalDateTime.now();
        try {
            //执行减库存操作
            int reduceNumber = seckillMapper.reduceNumber(seckillId, nowTime);
            if (reduceNumber <= 0) {
                logger.warn("没有更新数据库记录，说明秒杀结束");
                throw new SeckillCloseException("seckill is closed");
            } else {
                //这里至少减少的数量不为0了，秒杀成功了就增加一个秒杀成功详细
                int insertCount = successKilledMapper.insertSuccessKilled(seckillId, userPhone);
                //查看是否被重复插入，即用户是否重复秒杀
                if (insertCount <= 0) {
                    throw new RepeatKillException("seckill repeated");
                } else {
                    //秒杀成功了，返回那条插入成功秒杀的信息
                    SuccessKilled successKilled = successKilledMapper.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        } catch (SeckillCloseException | RepeatKillException e1) {
            throw e1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //把编译期异常转换为运行时异常
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }

//    @Override
//    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
//        if (md5 == null || !md5.equals(getMd5(seckillId))) {
//            return new SeckillExecution(seckillId, SeckillStatEnum.DATE_REWRITE);
//        }
//        LocalDateTime killTime = LocalDateTime.now();
//        Map<String, Object> map = new HashMap<>();
//        map.put("seckillId", seckillId);
//        map.put("phone", userPhone);
//        map.put("killTime", killTime);
//        map.put("result", null);
//        // 执行储存过程，result被复制
//        try {
//            seckillMapper.killByProcedure(map);
//            // 获取result
//            int result = MapUtils.getInteger(map, "result", -2);
//            if (result == 1) {
//                SuccessKilled successKilled = successKilledMapper.queryByIdWithSeckill(seckillId, userPhone);
//                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
//            } else {
//                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
//        }
//    }
}
