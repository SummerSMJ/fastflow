package com.pingpongx.node;

import com.alibaba.fastjson.JSON;
import com.pingpongx.meta.open.manage.beans.kyc.kyctemplate.response.KycTemplateDetailVO;
import com.pingpongx.meta.open.manage.beans.kyc.kyctemplate.response.KycTemplateModuleDetailVO;
import com.pingpongx.ocean.exception.BusinessException;
import com.pingpongx.ocean.exception.SystemException;
import com.pingpongx.ocean.util.CollectionUtils;
import com.pingpongx.ocean.util.JsonUtil;
import com.pingpongx.ocean.vo.Response;
import com.pingpongx.risk.api.req.risk.rule.RiskConfigReq;
import com.pingpongx.risk.api.req.risk.rule.RiskEventModuleConfig;
import com.pingpongx.risk.data.api.enums.BizExceptionEnum;
import com.pingpongx.risk.data.api.enums.FillDataModuleEnum;
import com.pingpongx.risk.data.api.enums.FillDataTypeEnum;
import com.pingpongx.risk.data.api.meta.model.Client;
import com.pingpongx.risk.data.api.meta.model.TradeOrderInfo;
import com.pingpongx.risk.data.api.meta.model.UserMaterials;
import com.pingpongx.risk.data.api.req.FillDataReq;
import com.pingpongx.risk.data.api.resp.FillDataResponse;
import com.pingpongx.risk.data.biz.context.FillDataContext;
import com.pingpongx.risk.data.biz.domain.FillDataExecutors;
import com.pingpongx.risk.data.biz.domain.meta.MetaUserService;
import com.pingpongx.risk.data.biz.domain.thirdLog.OcrService;
import com.pingpongx.risk.data.biz.service.RiskConfigService;
import com.pingpongx.risk.data.common.constant.RiskDataConstant;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.annotation.LiteflowMethod;
import com.yomahub.liteflow.enums.LiteFlowMethodEnum;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 统一补数服务
 *
 * @author baiyg
 * @date 2023/12/3 15:46
 */
@Data
@Slf4j
@Service
@RequiredArgsConstructor
@LiteflowComponent("unityFillData")
public class UnityFillDataNode {

    private final OcrService ocrService;

    private final MetaUserService metaUserService;

    private final RiskConfigService riskConfigService;

    /**
     * 补数
     *
     * @param fillDataReq
     * @return
     */
    //普通组件的定义

    public Response<FillDataResponse> fillData(FillDataReq fillDataReq) {

        FillDataContext context = buildFillDataContext(fillDataReq, null);

        FillDataExecutors.execute(context);

        // 补充计算信息
        fillComputeInfo(context);

        return Response.success(new FillDataResponse(context.getData(), context.getUserMaterials()));
    }

    /**
     * 补充计算信息
     *
     * @param context
     */
    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS, nodeId = "unityFillData.fillComputeInfo", nodeName = "补充计算信息")
    public void fillComputeInfo(FillDataContext context) {
        if (context.getBizRiskParams().isKycEvent()) {
            // 比对orc数据和用户材料数据是否不一致
            ocrService.isOcrModified(context.getUserMaterials(), context.getOcrUserMaterials(), context.getData());
        }
        // 把法人nation赋值到企业nation上
        if (context.getUserMaterials() != null
                && context.getUserMaterials().getEnterprise() != null && context.getUserMaterials().getApplicant() != null
                && StringUtils.isBlank(context.getUserMaterials().getEnterprise().getNation())) {
            context.getUserMaterials().getEnterprise().setNation(context.getUserMaterials().getApplicant().getNation());
            context.getData().put(RiskDataConstant.NAME_ENTERPRISE_NATION, context.getUserMaterials().getEnterprise().getNation());
        }
        context.calculateCostTime();
    }

    /**
     * 构建补数上下文
     *
     * @param fillDataReq
     * @param client
     * @return
     */
    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS, nodeId = "unityFillData.buildFillDataContext", nodeName = "构建补数上下文")
    public FillDataContext buildFillDataContext(FillDataReq fillDataReq, Client client) {
        if (fillDataReq == null || fillDataReq.getRiskEventModuleConfig() == null) {
            throw new BusinessException(BizExceptionEnum.PARAMS_LACK, "补数入参不能为空");
        }
        String clientId = fillDataReq.getBizRiskParams().getMetaClientId();
        RiskEventModuleConfig riskEventModuleConfig = fillDataReq.getRiskEventModuleConfig();
        FillDataContext context = new FillDataContext(fillDataReq.getRequestId(), fillDataReq.getBizRiskParams(), riskEventModuleConfig);
        context.setRequestParams(fillDataReq.getRequestParams());
        if (client == null) {
            client = metaUserService.getClientAndCustomerByClientId(clientId);
            client.setBusinessSource(context.getBizRiskParams().getBusiness());
            context.setUserMaterials(UserMaterials.builder().build());
            context.getUserMaterials().setClient(client);
        }
        log.info("clientInfo:{}", JSON.toJSONString(client));

        if (CollectionUtils.isEmpty(riskEventModuleConfig.getMetaMaterials()) && CollectionUtils.isEmpty(riskEventModuleConfig.getThirdLogs())) {
            RiskConfigReq riskConfigReq = new RiskConfigReq();
            riskConfigReq.setEventId(riskEventModuleConfig.getEventId());
            riskConfigReq.setEventCode(riskEventModuleConfig.getEventCode());
            riskConfigReq.setAreaBusinessType(client.getClientType());
            RiskEventModuleConfig queryEventModuleConfig = riskConfigService.queryEventModuleConfig(riskConfigReq);
            if (queryEventModuleConfig == null) {
                log.info("查询补数配置为空，无需补数,clientId:{},riskConfigReq:{}", clientId, JSON.toJSONString(riskConfigReq));
                return context;
//                throw new SystemException(BizExceptionEnum.DATA_MODULE_EMPTY, JSON.toJSONString(riskConfigReq));
            }
            queryEventModuleConfig.setBusinessType(riskEventModuleConfig.getBusinessType());
            context.setRiskEventModuleConfig(queryEventModuleConfig);
        }

        // kyc事件构建用户材料信息
        if (context.getBizRiskParams().isKycEvent() || context.getRiskEventModuleConfig().getMetaMaterials().containsKey(FillDataTypeEnum.CERTIFICATION_MATERIALS.getName()) ||
                "1".equals((String) context.getRiskEventModuleConfig().getExtentMap().getOrDefault(RiskDataConstant.FILL_KYC_DATA, "1"))) {
            buildKycConfig(context, clientId, context.getRiskEventModuleConfig());
        }

        // 入账构建交易信息
        if (context.isInbound()) {
            buildInboundConfig(fillDataReq, context);
        }

        return context;
    }

    /**
     * 构建交易补数配置
     *
     * @param fillDataReq
     * @param context
     */
    private static void buildInboundConfig(FillDataReq fillDataReq, FillDataContext context) {
        log.info("交易补数信息初始化,requestId:{},auditId:{}", context.getRequestId(), context.getBizRiskParams().getAuditId());
        TradeOrderInfo tradeOrderInfo = new TradeOrderInfo();
        Map<String, Object> riskParams = fillDataReq.getRequestParams();
        tradeOrderInfo.setBuyerName(Objects.toString(riskParams.get("buyer_name"), null));
        tradeOrderInfo.setPayerName(Objects.toString(riskParams.get("payer_name"), null));
        tradeOrderInfo.setPayerAddress(Objects.toString(riskParams.get("payer_address"), null));
        tradeOrderInfo.setShippingNo(Objects.toString(riskParams.get("shipping_no"), null));
        tradeOrderInfo.setIsShipped(Objects.toString(riskParams.get("is_shipped"), null));
        tradeOrderInfo.setPayerNameTag1(Objects.toString(riskParams.get("payer_name_tag1"), null));
        context.setTradeOrderInfo(tradeOrderInfo);
        log.info("交易补数信息初始化完成,tradeInfo:{},requestId:{},auditId:{}", JsonUtil.writeValueAsString(tradeOrderInfo), context.getRequestId(), context.getBizRiskParams().getAuditId());
    }

    /**
     * 构建kyc补数配置(主要是参数映射)
     *
     * @param context
     * @param clientId
     * @param riskEventModuleConfig
     */
    private void buildKycConfig(FillDataContext context, String clientId, RiskEventModuleConfig riskEventModuleConfig) {
        log.info("kyc补数信息初始化,requestId:{},metaClientId:{}", context.getRequestId(), context.getBizRiskParams().getMetaClientId());

        String riskDataTemplateCode = riskEventModuleConfig.getTemplateCode();

        log.info("kyc补数模板:{},clientId:{}", riskDataTemplateCode, clientId);

        // 认证材料补数参数转换配置
        if (StringUtils.isBlank(riskDataTemplateCode)) {
            log.info("补数配置-材料补数模板编码为空,clientId:{},riskModuleConfig:{}", clientId, JSON.toJSONString(riskEventModuleConfig));
            return;
        }
        // 如果补数模板不为空审核模板为空，则用补数模板填充审核模板
        if (StringUtils.isNotBlank(riskDataTemplateCode) && StringUtils.isBlank(context.getBizRiskParams().getAuditTemplateCode())) {
            context.getBizRiskParams().setAuditTemplateCode(riskDataTemplateCode);
        }
        List<String> queryModules = new ArrayList<>();
        // 根据补数模板编码查询模板材料，并根据补数配置映射对应的引擎参数
        KycTemplateDetailVO kycTemplateDetailVO = metaUserService.queryTemplateDetailByCode(clientId, riskDataTemplateCode);
        for (KycTemplateModuleDetailVO kycTemplateModuleDetailVO : kycTemplateDetailVO.getModuleList()) {
            queryModules.add(kycTemplateModuleDetailVO.getFieldKey());
        }

        // 查询参数转换配置
        List<RiskEventModuleConfig.RiskDataParam> riskDataParams = riskConfigService.queryDataParams(queryModules);
        if (CollectionUtils.isEmpty(riskDataParams)) {
            log.error("查询补数配置参数失败,clientId:{},riskModuleConfig:{},queryModules:{}", clientId, JSON.toJSONString(riskEventModuleConfig), JsonUtil.writeValueAsString(queryModules));
            throw new SystemException(BizExceptionEnum.DATA_PARAMS_EMPTY, "查询补数配置参数失败");
        }
        Map<String, List<RiskEventModuleConfig.RiskDataParam>> dataConfigs = riskDataParams.stream().collect(Collectors.groupingBy(RiskEventModuleConfig.RiskDataParam::getModule, Collectors.toList()));

        // 把取数模板配置按照risk-data-param转换成引擎配置
        List<RiskEventModuleConfig.RiskDataParam> certificationMaterials = new ArrayList<>();
        for (KycTemplateModuleDetailVO kycTemplateModuleDetailVO : kycTemplateDetailVO.getModuleList()) {
            // 取数模板的模块名称必须和risk_data_param模块名称一致
            String moduleName = kycTemplateModuleDetailVO.getFieldKey();
            queryModules.add(moduleName);
            List<RiskEventModuleConfig.RiskDataParam> dataParams = dataConfigs.get(moduleName);
            if (CollectionUtils.isEmpty(dataParams)) {
                log.info("补数模板{}有配置取数模块{},但风控未配置引擎转换模块", riskEventModuleConfig.getTemplateCode(), moduleName);
                continue;
            }
            Map<String, RiskEventModuleConfig.RiskDataParam> dataParamsMap = dataParams.stream().collect(Collectors.toMap(RiskEventModuleConfig.RiskDataParam::getParamName, Function.identity()));
            List<RiskEventModuleConfig.RiskDataParam> metaMaterials = new ArrayList<>();
            kycTemplateModuleDetailVO.getMaterialList().forEach(kycTemplateMaterialDetailVO -> {
                // 取数模板下的模块下的材料名称必须和risk_data_param材料名称一致
                RiskEventModuleConfig.RiskDataParam dataParam = dataParamsMap.get(kycTemplateMaterialDetailVO.getFieldKey());
                if (dataParam != null) {
                    RiskEventModuleConfig.RiskDataParam materials = RiskEventModuleConfig.RiskDataParam.newInstance();
                    materials.setModuleCode(kycTemplateModuleDetailVO.getKycCodePre());
                    materials.setParamCode(kycTemplateMaterialDetailVO.getMetaKycCode());
                    materials.setParamName(kycTemplateMaterialDetailVO.getFieldKey());
                    materials.setEngineParamName(dataParam.getEngineParamName());
                    materials.setModule(dataParam.getModule());
                    metaMaterials.add(materials);
                }
            });
            certificationMaterials.addAll(metaMaterials);
        }

        riskEventModuleConfig.getMetaMaterials().put(FillDataTypeEnum.CERTIFICATION_MATERIALS.getName(), certificationMaterials);

        // 默认补数逻辑
        if (CollectionUtils.isNotEmpty(riskEventModuleConfig.getThirdLogs())) {
            if (CollectionUtils.isNotEmpty(riskEventModuleConfig.getThirdLogs().get(FillDataModuleEnum.QCC_ECI_INFO_VERIFY.getName()))) {
                // 添加qcc关联企业查询
                riskEventModuleConfig.getThirdLogs().put(FillDataModuleEnum.QCC_RELATED_ECI_INFO_VERIFY.getName(), riskEventModuleConfig.getThirdLogs().get(FillDataModuleEnum.QCC_ECI_INFO_VERIFY.getName()));
            }
            if (CollectionUtils.isNotEmpty(riskEventModuleConfig.getThirdLogs().get(FillDataModuleEnum.BH_ENTERPRISE_RISK_INFO.getName()))) {
                // 添加百行关联企业查询
                riskEventModuleConfig.getThirdLogs().put(FillDataModuleEnum.BH_ENTERPRISE_RELATED_RISK_INFO.getName(), riskEventModuleConfig.getThirdLogs().get(FillDataModuleEnum.BH_ENTERPRISE_RISK_INFO.getName()));
            }
        }
    }

}
