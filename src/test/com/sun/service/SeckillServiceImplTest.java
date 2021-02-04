package com.sun.service;

import com.sun.dto.Exposer;
import com.sun.dto.SeckillExecution;
import com.sun.entity.Seckill;
import com.sun.exception.RepeatKillException;
import com.sun.exception.SeckillCloseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/applicationContext-dao.xml", "classpath:spring/applicationContext-service.xml"})
public class SeckillServiceImplTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> seckillList = seckillService.getSeckillList();
        for (Seckill s : seckillList) {
            System.out.println(s.toString());
        }
    }

    @Test
    public void getById() throws Exception {
        long seckillId = 1008;
        Seckill byId = seckillService.getById(seckillId);
        System.out.println(byId.toString());
    }

    @Test
    public void exportSeckillUrl() throws Exception {
        long seckillId = 1008;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        System.out.println(exposer.toString());
    }

    @Test
    public void executeSeckill() throws Exception {
        long seckillId = 1004;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            long userPhone = 12222222222L;
            String md5 = "47e82bdf4caa9e089b1a5b4b676c48eb";
            try {
                SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone, md5);
                System.out.println(seckillExecution.toString());
            } catch (SeckillCloseException | RepeatKillException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("秒杀未开启");
        }
    }

//    @Test
//    public void executeSeckillProcedureTest() {
//        long seckillId = 1001;
//        long phone = 1368011101;
//        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
//        if (exposer.isExposed()) {
//            String md5 = exposer.getMd5();
//            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
//            System.out.println(execution.getStateInfo());
//        }
//    }
}
