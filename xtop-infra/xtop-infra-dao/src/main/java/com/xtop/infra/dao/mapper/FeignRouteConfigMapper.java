package com.xtop.infra.dao.mapper;
import com.xtop.infra.model.entity.FeignFieldMapping;
import com.xtop.infra.model.entity.FeignRouteConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FeignRouteConfigMapper {
    @Select("SELECT * FROM t_feign_route_config WHERE route_code = #{routeCode} AND status = 1 LIMIT 1")
    FeignRouteConfig selectByRouteCode(@Param("routeCode") String routeCode);

    @Select("SELECT * FROM t_feign_field_mapping WHERE route_code = #{routeCode} AND field_type = #{fieldType} AND status = 1 ORDER BY sort_order")
    List<FeignFieldMapping> selectFieldsByRouteAndType(@Param("routeCode") String routeCode,
                                                       @Param("fieldType") String fieldType);
}
