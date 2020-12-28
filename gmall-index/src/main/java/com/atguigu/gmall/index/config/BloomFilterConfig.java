package com.atguigu.gmall.index.config;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.google.common.hash.BloomFilter;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Configuration
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    private static final String KEY_PREFIX = "index:cates:[";

    @Bean
    public RBloomFilter<String> getBloomFilter() {
        RBloomFilter<String> bloomFilter = this.redissonClient.getBloomFilter("index:cates:bloom");
        bloomFilter.tryInit(10000, 0.03);

        // 查询数据库中的数据，存入布隆过滤器中
        ResponseVo<List<CategoryEntity>> categoriesVo = this.gmallPmsClient.queryCategoriesByParentId(0L);
        List<CategoryEntity> categoryEntities = categoriesVo.getData();
        if ( !CollectionUtils.isEmpty(categoryEntities)) {
            categoryEntities.forEach( categoryEntity -> {
                bloomFilter.add(KEY_PREFIX + categoryEntity.getId() + "]");
            });
        }

        return bloomFilter;
    }


    /**
     * 定时刷新布隆过滤器
     */
    @Scheduled
    public void testFlushBloom(){
        RBloomFilter<String> bloomFilter = this.redissonClient.getBloomFilter("index:cates:bloom");
        bloomFilter.tryInit(10000, 0.003);
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsClient.queryCategoriesByParentId(0l);
        List<CategoryEntity> categories = listResponseVo.getData();
        if (!CollectionUtils.isEmpty(categories)){
            categories.forEach(categoryEntity -> {
                bloomFilter.add(KEY_PREFIX + categoryEntity.getId() + "]");
            });
        }
    }
}
