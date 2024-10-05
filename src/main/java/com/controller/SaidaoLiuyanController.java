
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 赛道留言
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/saidaoLiuyan")
public class SaidaoLiuyanController {
    private static final Logger logger = LoggerFactory.getLogger(SaidaoLiuyanController.class);

    private static final String TABLE_NAME = "saidaoLiuyan";

    @Autowired
    private SaidaoLiuyanService saidaoLiuyanService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private GonggaoService gonggaoService;//公告
    @Autowired
    private JiaolianService jiaolianService;//教练
    @Autowired
    private JiaolianYuyueService jiaolianYuyueService;//教练预约
    @Autowired
    private LiuyanService liuyanService;//留言板
    @Autowired
    private QijuService qijuService;//滑雪器具
    @Autowired
    private QijuCollectionService qijuCollectionService;//器具收藏
    @Autowired
    private QijuCommentbackService qijuCommentbackService;//器具评价
    @Autowired
    private QijuOrderService qijuOrderService;//器具订单
    @Autowired
    private SaidaoService saidaoService;//赛道
    @Autowired
    private SaidaoCollectionService saidaoCollectionService;//赛道收藏
    @Autowired
    private SaidaoYuyueService saidaoYuyueService;//赛道预约
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("教练".equals(role))
            params.put("jiaolianId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = saidaoLiuyanService.queryPage(params);

        //字典表数据转换
        List<SaidaoLiuyanView> list =(List<SaidaoLiuyanView>)page.getList();
        for(SaidaoLiuyanView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        SaidaoLiuyanEntity saidaoLiuyan = saidaoLiuyanService.selectById(id);
        if(saidaoLiuyan !=null){
            //entity转view
            SaidaoLiuyanView view = new SaidaoLiuyanView();
            BeanUtils.copyProperties( saidaoLiuyan , view );//把实体数据重构到view中
            //级联表 赛道
            //级联表
            SaidaoEntity saidao = saidaoService.selectById(saidaoLiuyan.getSaidaoId());
            if(saidao != null){
            BeanUtils.copyProperties( saidao , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setSaidaoId(saidao.getId());
            }
            //级联表 用户
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(saidaoLiuyan.getYonghuId());
            if(yonghu != null){
            BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody SaidaoLiuyanEntity saidaoLiuyan, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,saidaoLiuyan:{}",this.getClass().getName(),saidaoLiuyan.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            saidaoLiuyan.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        saidaoLiuyan.setCreateTime(new Date());
        saidaoLiuyan.setInsertTime(new Date());
        saidaoLiuyanService.insert(saidaoLiuyan);

        return R.ok();
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody SaidaoLiuyanEntity saidaoLiuyan, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,saidaoLiuyan:{}",this.getClass().getName(),saidaoLiuyan.toString());
        SaidaoLiuyanEntity oldSaidaoLiuyanEntity = saidaoLiuyanService.selectById(saidaoLiuyan.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            saidaoLiuyan.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        if("".equals(saidaoLiuyan.getSaidaoLiuyanText()) || "null".equals(saidaoLiuyan.getSaidaoLiuyanText())){
                saidaoLiuyan.setSaidaoLiuyanText(null);
        }
        if("".equals(saidaoLiuyan.getReplyText()) || "null".equals(saidaoLiuyan.getReplyText())){
                saidaoLiuyan.setReplyText(null);
        }
        saidaoLiuyan.setUpdateTime(new Date());

            saidaoLiuyanService.updateById(saidaoLiuyan);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<SaidaoLiuyanEntity> oldSaidaoLiuyanList =saidaoLiuyanService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        saidaoLiuyanService.deleteBatchIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //.eq("time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        try {
            List<SaidaoLiuyanEntity> saidaoLiuyanList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            SaidaoLiuyanEntity saidaoLiuyanEntity = new SaidaoLiuyanEntity();
//                            saidaoLiuyanEntity.setSaidaoId(Integer.valueOf(data.get(0)));   //赛道 要改的
//                            saidaoLiuyanEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            saidaoLiuyanEntity.setSaidaoLiuyanText(data.get(0));                    //留言内容 要改的
//                            saidaoLiuyanEntity.setInsertTime(date);//时间
//                            saidaoLiuyanEntity.setReplyText(data.get(0));                    //回复内容 要改的
//                            saidaoLiuyanEntity.setUpdateTime(sdf.parse(data.get(0)));          //回复时间 要改的
//                            saidaoLiuyanEntity.setCreateTime(date);//时间
                            saidaoLiuyanList.add(saidaoLiuyanEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        saidaoLiuyanService.insertBatch(saidaoLiuyanList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = saidaoLiuyanService.queryPage(params);

        //字典表数据转换
        List<SaidaoLiuyanView> list =(List<SaidaoLiuyanView>)page.getList();
        for(SaidaoLiuyanView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Integer id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        SaidaoLiuyanEntity saidaoLiuyan = saidaoLiuyanService.selectById(id);
            if(saidaoLiuyan !=null){


                //entity转view
                SaidaoLiuyanView view = new SaidaoLiuyanView();
                BeanUtils.copyProperties( saidaoLiuyan , view );//把实体数据重构到view中

                //级联表
                    SaidaoEntity saidao = saidaoService.selectById(saidaoLiuyan.getSaidaoId());
                if(saidao != null){
                    BeanUtils.copyProperties( saidao , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setSaidaoId(saidao.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(saidaoLiuyan.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody SaidaoLiuyanEntity saidaoLiuyan, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,saidaoLiuyan:{}",this.getClass().getName(),saidaoLiuyan.toString());
        saidaoLiuyan.setCreateTime(new Date());
        saidaoLiuyan.setInsertTime(new Date());
        saidaoLiuyanService.insert(saidaoLiuyan);

            return R.ok();
        }

}

