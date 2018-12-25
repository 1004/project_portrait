package com.rat.controller;

import com.alibaba.fastjson.JSONObject;
import com.rat.common.Constant;
import com.rat.common.RequestCode;
import com.rat.entity.network.request.*;
import com.rat.entity.network.request.base.RequestInfo;
import com.rat.entity.network.response.base.ResponseInfo;
import com.rat.service.*;
import com.rat.utils.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 请求回调接口
 *
 * @author L.jinzhu 2017/3/30
 */
@Controller
@RequestMapping("/projectportrait")
public class RequestController {

    private static Logger logger = LoggerFactory.getLogger(RequestController.class);
    @Resource
    private FileService fileService;
    @Resource
    private ResourceService resourceService;
    @Resource
    private ReferenceService referenceService;
    @Resource
    private NewsService newsService;
    @Resource
    private SystemService systemService;

    public RequestController() {
        logger.info("rat server start");
        initSystemProfiles();
    }

    @RequestMapping(value = "/request", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String run(@RequestBody(required = false) String json) {
        logger.info(Constant.LOG_REQUEST + json);

        RequestInfo requestInfo = GsonUtil.fromJson(json, RequestInfo.class);
        ResponseInfo response = null;
        // 请求解析异常
        if (null == requestInfo || null == requestInfo.getActionInfo()) {
            response = new ResponseInfo();
            response.initError4Param(0);
            logger.info(Constant.LOG_RESPONSE + ": " + response.toString());
            return GsonUtil.toJson(response);
        }
        // 请求解析正常
        JSONObject jsonObject = JSONObject.parseObject(json);
        String actionInfoStr = jsonObject.getString("actionInfo");
        switch (requestInfo.getActionInfo().getActionId()) {
            // 文件获取全部
            case RequestCode.FILE_FIND_ALL:
                FileFindAllActionInfo fileFindAllActionInfo = GsonUtil.fromJson(actionInfoStr, FileFindAllActionInfo.class);
                response = fileService.findAll(fileFindAllActionInfo);
                break;


            // 注册登录
            case RequestCode.USER_REGISTER_LOGIN:
                UserRegisterLoginActionInfo userRegisterLoginActionInfo = GsonUtil.fromJson(actionInfoStr, UserRegisterLoginActionInfo.class);
                response = fileService.login(userRegisterLoginActionInfo);
                break;
            // 用户更新
            case RequestCode.USER_UPDATE:
                UserUpdateActionInfo userUpdateActionInfo = GsonUtil.fromJson(actionInfoStr, UserUpdateActionInfo.class);
                response = fileService.update(userUpdateActionInfo);
                break;
            // 获取所有用户
            case RequestCode.USER_FIND_ALL:
                UserFindAllActionInfo userFindAllActionInfo = GsonUtil.fromJson(actionInfoStr, UserFindAllActionInfo.class);
//                response = fileService.findAll(fileFindAllActionInfo);
                break;
            // 获取用户详情
            case RequestCode.USER_FIND_DETAIL:
                UserFindDetailActionInfo userFindDetailActionInfo = GsonUtil.fromJson(actionInfoStr, UserFindDetailActionInfo.class);
                response = fileService.findDetail(userFindDetailActionInfo);
                break;
            // 创建视频
            case RequestCode.VIDEO_CREATE:
                ResourceCreateActionInfo resourceCreateActionInfo = GsonUtil.fromJson(actionInfoStr, ResourceCreateActionInfo.class);
                response = resourceService.create(resourceCreateActionInfo);
                break;
            // 删除视频
            case RequestCode.VIDEO_DELETE:
                ResourceDeleteActionInfo resourceDeleteActionInfo = GsonUtil.fromJson(actionInfoStr, ResourceDeleteActionInfo.class);
                response = resourceService.delete(resourceDeleteActionInfo);
                break;
            // 关注、取消关注
            case RequestCode.FOLLOW:
                ReferenceActionInfo referenceActionInfo = GsonUtil.fromJson(actionInfoStr, ReferenceActionInfo.class);
                response = referenceService.reference(referenceActionInfo);
                break;
            // 获取user主动关注的人
            case RequestCode.FOLLOW_FIND_BY_USER:
                ReferenceFindAllActionInfo referenceFindAllByUserActionInfo = GsonUtil.fromJson(actionInfoStr, ReferenceFindAllActionInfo.class);
                response = referenceService.findAllByUserId(referenceFindAllByUserActionInfo);
                break;
            // 获取关注user的人
            case RequestCode.FOLLOW_FIND_BY_FOLLOWED_USER:
                ReferenceFindAllActionInfo referenceFindAllByReferenceedUserActionInfo = GsonUtil.fromJson(actionInfoStr, ReferenceFindAllActionInfo.class);
                response = referenceService.findAllByReferenceedUserId(referenceFindAllByReferenceedUserActionInfo);
                break;

            // 最新版本
            case RequestCode.SYSTEM_NEW_VERSION:
                NewVersionActionInfo newVersionActionInfo = GsonUtil.fromJson(actionInfoStr, NewVersionActionInfo.class);
                response = systemService.getNewVersion(newVersionActionInfo);
                break;
            // 视频名称列表
            case RequestCode.SYSTEM_VIDEO_NAMES:
                ResourceNamesActionInfo resourceNamesActionInfo = GsonUtil.fromJson(actionInfoStr, ResourceNamesActionInfo.class);
                response = systemService.getResourceNames(resourceNamesActionInfo);
                break;
            default:
                response = new ResponseInfo();
                response.initError4Param(requestInfo.getActionInfo().getActionId());
                break;
        }
        logger.info(Constant.LOG_RESPONSE + ": " + response.toString());
        return GsonUtil.toJson(response);
    }

    /**
     * 初始化系统配置文件
     */
    private void initSystemProfiles() {
        Properties prop;
        String propFile;
        // 系统配置
        prop = new Properties();
        propFile = getClass().getResource("/").getPath() + File.separator + "system" + File.separator + "system.properties";
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(propFile), "UTF-8");
            prop.load(in);
            Constant.DATA_COUNT_OF_PAGE = Integer.parseInt(prop.getProperty("dataCountOfPage"));
            Constant.userDefaultBigImage = prop.getProperty("userDefaultBigImage");
            Constant.resourceNames = prop.getProperty("resourceNames");
            in.close();
            logger.info("rat init system profiles success: dataCountOfPage: " + Constant.DATA_COUNT_OF_PAGE + " | userDefaultBigImage: " + Constant.userDefaultBigImage + " | resourceNames: " + Constant.resourceNames);
        } catch (Throwable e) {
            logger.error("rat init system profiles error", e);
        }
        // 新版本配置
        prop = new Properties();
        propFile = getClass().getResource("/").getPath() + File.separator + "system" + File.separator + "newVersion.properties";
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(propFile), "UTF-8");
            prop.load(in);
            Constant.versionCode = prop.getProperty("versionCode");
            Constant.versionName = prop.getProperty("versionName");
            Constant.isForced = prop.getProperty("isForced");
            Constant.downloadUrl = prop.getProperty("downloadUrl");
            Constant.des = prop.getProperty("des");
            in.close();
            logger.info("rat init new version profiles success: versionCode: " + Constant.versionCode + " | versionName: " + Constant.versionName + " | downloadUrl: " + Constant.downloadUrl);
        } catch (Throwable e) {
            logger.error("rat init new version profiles error", e);
        }
    }
}