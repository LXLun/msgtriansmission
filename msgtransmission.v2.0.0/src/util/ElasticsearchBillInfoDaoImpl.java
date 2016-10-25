package com.gooagoo.bill.service.core.impl.dao.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.gooagoo.alonedeploy.common.ConstantDeployType;
import com.gooagoo.common.log.GooagooLog;
import com.gooagoo.common.spring.configurer.GagShortNameFileResource;
import com.gooagoo.entity.bill.BillInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Repository("elasticsearchBillInfoDao")
public class ElasticsearchBillInfoDaoImpl extends BillServiceDaoAbstract
{
    public static final String ESFileName = "elasticsearch.properties";
    private static ElasticsearchTemplate esTemplate;
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyyMMddHHmmssSSS").disableHtmlEscaping().create();

    static
    {
        InputStream in = null;
        String path = GagShortNameFileResource.RESOURCEDIR + "/" + ESFileName;
        try
        {
            in = new FileInputStream(path);
            Properties props = new Properties();
            props.load(in);

            String esServerIp = props.getProperty("es.server.ip").trim();
            int esServerPort = Integer.parseInt(props.getProperty("es.server.port").trim());
            String esClusterName = props.getProperty("es.cluster.name").trim();

            Settings settings = Settings.settingsBuilder().put("cluster.name", esClusterName).put("client.transport.sniff", true).build();

            String[] esNodeIpList = esServerIp.split(",");
            InetSocketTransportAddress[] esNodeList = new InetSocketTransportAddress[esNodeIpList.length];
            for (int i = 0; i < esNodeIpList.length; i++)
            {
                esNodeList[i] = new InetSocketTransportAddress(InetAddress.getByName(esNodeIpList[i]), esServerPort);
            }
            Client client = TransportClient.builder().settings(settings).build().addTransportAddresses(esNodeList);
            esTemplate = new ElasticsearchTemplate(client);
        }
        catch (FileNotFoundException e)
        {
            if (ConstantDeployType.getDeployType() == ConstantDeployType.PLATFORM_DEPLOYMENT)
            {
                GooagooLog.error(path + "路径下不存在ES配置文件异常", e);
            }
        }
        catch (Exception e)
        {
            if (ConstantDeployType.getDeployType() == ConstantDeployType.PLATFORM_DEPLOYMENT)
            {
                GooagooLog.error("从" + path + "路径下读取ES配置文件异常", e);
            }
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    GooagooLog.error("从" + path + "路径下读取ES配置文件关闭流异常", e);
                }
            }
        }
    }

    @Override
    public Map<String, Object> selectBillByMutiCondition(String terminalNumber, String[] shopId, String[] shopEntityId, String billFileName, String[] saleTime, String[] interceptTime, String[] cTime, String[] billType, String[] billSource, String[] modifyType, String orderType, String orderWay, Integer pageIndex, Integer pageSize)
    {
        SortOrder defaultOrder = SortOrder.DESC;

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (StringUtils.hasText(billFileName))
        {
            boolQuery.must(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("billfileName", billFileName)).should(QueryBuilders.matchQuery("billfileNameHis", billFileName)));
        }
        if (shopEntityId != null && shopEntityId.length > 0)
        {
            boolQuery.must(QueryBuilders.termsQuery("shopEntityId", shopEntityId));
        }
        if (shopId != null && shopId.length > 0)
        {
            boolQuery.must(QueryBuilders.termsQuery("shopId", shopId));
        }
        if (StringUtils.hasText(terminalNumber))
        {
            boolQuery.must(QueryBuilders.matchQuery("terminalNumber", terminalNumber));
        }
        if (billType != null && billType.length > 0)
        {
            boolQuery.must(QueryBuilders.termsQuery("billType", billType));
        }
        if (billSource != null && billSource.length > 0)
        {
            boolQuery.must(QueryBuilders.termsQuery("billSource", billSource));
        }
        if (modifyType != null && modifyType.length > 0)
        {
            boolQuery.must(QueryBuilders.termsQuery("modifyType", modifyType));
        }

        if (saleTime != null && saleTime.length >= 2)
        {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("saleTime");
            if (saleTime[0] != null)
            {
                rangeQuery.gte(saleTime[0]);
            }
            if (saleTime[1] != null)
            {
                rangeQuery.lte(saleTime[1]);
            }
            boolQuery.must(rangeQuery);
        }
        if (interceptTime != null && interceptTime.length >= 2)
        {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("interceptTime");
            if (interceptTime[0] != null)
            {
                rangeQuery.gte(interceptTime[0]);
            }
            if (interceptTime[1] != null)
            {
                rangeQuery.lte(interceptTime[1]);
            }
            boolQuery.must(rangeQuery);
        }
        if (cTime != null && cTime.length >= 2)
        {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("cTimeStamp");
            if (cTime[0] != null)
            {
                rangeQuery.gte(cTime[0]);
            }
            if (cTime[1] != null)
            {
                rangeQuery.lte(cTime[1]);
            }
            boolQuery.must(rangeQuery);
        }

        if ("asc".equals(orderWay))
        {
            defaultOrder = SortOrder.ASC;
        }
        SortBuilder sortBuilder = SortBuilders.fieldSort(orderType).order(defaultOrder);
        SearchQuery query = new NativeSearchQueryBuilder().withQuery(boolQuery).withIndices("bills").withTypes("billinfo").withSort(sortBuilder).withPageable(new PageRequest(pageIndex - 1, pageSize)).build();
        Map<String, Object> result = this.doQuery(query);
        return result;
    }

    @Override
    public BillInfo selectBillInfoByBillId(String billId)
    {
        SearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("_id", billId)).withIndices("bills").withTypes("billinfo").build();
        Map<String, Object> result = this.doQuery(query);
        List<BillInfo> results = (List<BillInfo>) result.get("bills");
        if (results == null || results.isEmpty())
        {
            return null;
        }
        return results.get(0);
    }

    @Override
    public BillInfo selectBillInfoByTsuuid(String tsuuid)
    {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.matchQuery("tsuuid", tsuuid));
        boolQuery.mustNot(QueryBuilders.matchQuery("billType", "1001"));
        SearchQuery query = new NativeSearchQueryBuilder().withQuery(boolQuery).withIndices("bills").withTypes("billinfo").build();
        Map<String, Object> result = this.doQuery(query);
        List<BillInfo> results = (List<BillInfo>) result.get("bills");
        if (results == null || results.isEmpty())
        {
            return null;
        }
        return results.get(0);
    }

    private Map<String, Object> doQuery(SearchQuery query)
    {
        Map<String, Object> result = this.esTemplate.query(query, new ResultsExtractor<Map<String, Object>>()
        {
            @Override
            public Map<String, Object> extract(SearchResponse response)
            {
                long totalHits = response.getHits().totalHits();
                Map<String, Object> map = new HashMap<String, Object>();
                List<BillInfo> results = new ArrayList<BillInfo>();
                SearchHits hits = response.getHits();
                for (SearchHit hit : hits.getHits())
                {
                    String source = hit.getSourceAsString();
                    if (!StringUtils.hasText(source))
                    {
                        continue;
                    }
                    try
                    {
                        BillInfo billInfo = gson.fromJson(source, BillInfo.class);
                        billInfo.setId(hit.getId());
                        results.add(billInfo);
                    }
                    catch (Exception e)
                    {
                        GooagooLog.error("解析source时发生异常.cause by=" + e.getMessage() + ",source=" + source, e);
                    }
                }
                if (results.isEmpty())
                {
                    results = null;
                }
                map.put("total", totalHits);
                map.put("bills", results);
                return map;
            }
        });
        return result;
    }
}
