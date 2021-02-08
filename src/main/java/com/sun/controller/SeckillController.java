package com.sun.controller;

import com.sun.dto.Exposer;
import com.sun.dto.SeckillExecution;
import com.sun.dto.SeckillResult;
import com.sun.entity.Seckill;
import com.sun.enums.SeckillStatEnum;
import com.sun.exception.RepeatKillException;
import com.sun.exception.SeckillCloseException;
import com.sun.exception.SeckillException;
import com.sun.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController {
    @Autowired
    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    /**
     * 进入秒杀列表
     *
     * @param model 模型数据，里面放置有秒杀商品的信息
     * @return 秒杀列表详情页面
     */
    @RequestMapping(value = {"/list","/","/index","index.html"}, method = RequestMethod.GET)
    public String list(Model model) {
        List<Seckill> seckillList = seckillService.getSeckillList();
        //model.addAttribute往前台传数据
        model.addAttribute("list", seckillList);
        return "list";
    }

    //@PathVariable解决路径里面带参数
    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        if (seckillId == null) {
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getById(seckillId);
        if (seckillId == null) {
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }

    /**
     * 暴露秒杀接口的方法
     *
     * @param seckillId 秒杀商品的Id
     * @return 根据用户秒杀的商品Id进行业务逻辑判断，返回不同的JSON实体结果
     */
    @RequestMapping(value = "/{seckillId}/exposer", method = RequestMethod.GET)
    @ResponseBody
    //@ResponseBody的作用其实是将java对象转为json格式的数据。
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {
        //查询秒杀商品的结果
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<>(true, exposer);
        } catch (Exception e) {
            e.printStackTrace();
            result = new SeckillResult<>(false, e.getMessage());
        }
        return result;
    }

    /**
     * 用户执行秒杀，在页面点击相应的秒杀链接，进入后获取对应的参数进行判断，返回相对应的JSON实体结果，前端再进行处理
     *
     * @param seckillId 秒杀的商品，对应的是秒杀的Id
     * @param md5       一个被混淆的MD5加密值
     * @param userPhone 参与秒杀用户的手机号码，当作账号密码使用
     * @return
     */
    @RequestMapping(value = "/{seckillId}/{md5}/execution", method = RequestMethod.POST)
    @ResponseBody
    //@CookieValue用来获取Cookie中的值
    public SeckillResult<SeckillExecution> execute(@PathVariable("sekillId") long seckillId, @PathVariable("md5") String md5, @CookieValue(value = "userPhone", required = false) Long userPhone) {
        //如果用户的手机号码为空的则说明没有进行填写手机进行秒杀
        if (userPhone == null) {
            return new SeckillResult<>(false, "没有注册");
        }
        //根据用户的手机号码，秒杀商品的Id跟MD5进行秒杀商品，没异常就是秒杀成功
        //换成储蓄过程
        try {
            SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone, md5);
            return new SeckillResult<>(true, seckillExecution);
        } catch (RepeatKillException e1) {
            //重复秒杀
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<>(false, execution);
        } catch (SeckillCloseException e2) {
            //秒杀关闭
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<>(false, execution);
        } catch (SeckillException e) {
            //不能判断的异常
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<>(false, execution);
        }
        //如果有异常就是秒杀失败
    }

    /**
     * 获取服务器时间，防止用户篡改客户端时间提前参与秒杀
     *
     * @return 时间的JSON数据
     */
    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<LocalDateTime> time() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return new SeckillResult<>(true, localDateTime);
    }
}
