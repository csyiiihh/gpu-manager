package com.bingo.gpumanager.mapper;

import com.bingo.gpumanager.entity.GpuServer;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface GpuServerMapper {

    @Select("""
            SELECT
                id,
                server_name AS serverName,
                ip_address AS ipAddress,
                gpu_model AS gpuModel,
                gpu_count AS gpuCount,
                status,
                description,
                create_time AS createTime
            FROM gpu_server
            """)
    List<GpuServer> findAll();


    @Select("""
        SELECT
            id,
            server_name AS serverName,
            ip_address AS ipAddress,
            gpu_model AS gpuModel,
            gpu_count AS gpuCount,
            status,
            description,
            create_time AS createTime
        FROM gpu_server
        WHERE id = #{id}
        """)
    GpuServer findById(Long id);


    @Insert("""
        INSERT INTO gpu_server
        (server_name, ip_address, gpu_model, gpu_count, status, description)
        VALUES
        (#{serverName}, #{ipAddress}, #{gpuModel}, #{gpuCount}, #{status}, #{description})
        """)
    int insert(GpuServer gpuServer);

    @Update("""
        UPDATE gpu_server
        SET
            server_name = #{serverName},
            ip_address = #{ipAddress},
            gpu_model = #{gpuModel},
            gpu_count = #{gpuCount},
            status = #{status},
            description = #{description}
        WHERE id = #{id}
        """)
    int update(GpuServer gpuServer);

    @Delete("""
        DELETE FROM gpu_server
        WHERE id = #{id}
        """)
    int deleteById(Long id);
}