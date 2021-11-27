package com.atguigu.gmall0401.list.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0401.bean.SkuLsInfo;
import com.atguigu.gmall0401.bean.SkuLsParams;
import com.atguigu.gmall0401.bean.SkuLsResult;
import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
    JestClient jestClient;
    @Autowired
    RedisUtil redisUtil;

    public static final String ES_INDEX="gmall0401_sku_info";

    public static final String ES_TYPE="doc";

    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {
        // 保存数据
        Index.Builder indexBuilder = new Index.Builder(skuLsInfo);
        System.out.println("skuLsInfo"+skuLsInfo.toString());
        System.out.println("indexBuilder:"+indexBuilder.toString());
        System.out.println("skuLsInfo.getId():"+skuLsInfo.getId());
        indexBuilder.index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId());


        Index index = indexBuilder.build();
        System.out.println("index.toString():"+index.toString());


        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("success execute");
    }

    @Override
    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams) {
        SkuLsResult skuLsResult = new SkuLsResult();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1.1 query bool must match

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //关键字查询，
        if (skuLsParams.getKeyword() != null){
            //4
            //高亮
            searchSourceBuilder.highlight(new HighlightBuilder().field("skuName").preTags("<span style='color:red'>").postTags("</span>"));
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
        }



        //1.2 query bool filter 三级分类
        if (skuLsParams.getCatalog3Id() != null){
            boolQueryBuilder.filter(new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id()));
        }

        //1.2 query bool filter 平台属性
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
            for (String id :skuLsParams.getValueId()
            ) {
                boolQueryBuilder.filter(new TermQueryBuilder("skuAttrValueList.valueId",id));
            }
        }



        searchSourceBuilder.query(boolQueryBuilder);

        //2
        searchSourceBuilder.from((skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize());
        //3
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //5

        TermsBuilder aggsBulider = AggregationBuilders.terms("groupby_value_id").field("skuAttrValueList.valueId").size(1000);
        searchSourceBuilder.aggregation(aggsBulider);
        //6
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);


        System.out.println("====================\n"+searchSourceBuilder+"\n=========================");
        Search.Builder searchBuilder = new Search.Builder(searchSourceBuilder.toString());

        Search search = searchBuilder.addIndex(ES_INDEX).addType(ES_TYPE).build();


        try {
            SearchResult searchResult = jestClient.execute(search);
            //将查询的商品列表封装到 javaBean
            List<SkuLsInfo> skuLsInfoList = new ArrayList<>();

            List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit:hits
                 ) {
                SkuLsInfo skuLsInfo = hit.source;
                System.out.println("====================\n"+skuLsInfo+"\n=========================");
                String skuName = hit.highlight.get("skuName").get(0);
                System.out.println("====================\n"+skuName+"\n=========================");
                skuLsInfo.setSkuName(skuName);

                skuLsInfoList.add(skuLsInfo);
            }
            skuLsResult.setSkuLsInfoList(skuLsInfoList);
            
            Long total = searchResult.getTotal();
            skuLsResult.setTotal(total);

            long totalPages = (total + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize();
            skuLsResult.setTotalPages(totalPages);

            List<String> attrValueIdList = new ArrayList<>();
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_value_id").getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                attrValueIdList.add(bucket.getKey());
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);

        } catch (IOException e) {
            e.printStackTrace();
        }



        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        String hotScoreKey = "skuid:" + skuId + ":hotScore";
        //设置key, type string ,  key: skuId:41:hotScore  ,value:  hotScore
        Long hotScore = jedis.incr(hotScoreKey);

        System.out.println("====================\n"+"hotScore: \n"+hotScore+"\n=========================");

        if (hotScore%10 == 0){
            updateHotScoreEs(skuId,hotScore);

        }

        jedis.close();


    }

    public void updateHotScoreEs(String skuId, Long hotScore){
        String updateString = "{\n" +
                "  \n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":"+hotScore+"\n" +
                "  }\n" +
                "  \n" +
                "}";
        System.out.println("====================\n"+"updateString: \n"+updateString+"\n=========================");

        Update update = new Update.Builder(updateString).index(ES_INDEX).type(ES_TYPE).id(skuId).build();
        System.out.println("====================\n"+"updateString: \n"+"\n=========================");

        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
