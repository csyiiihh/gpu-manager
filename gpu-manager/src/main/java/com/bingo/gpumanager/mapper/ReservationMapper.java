package com.bingo.gpumanager.mapper;

import com.bingo.gpumanager.entity.Reservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.bingo.gpumanager.dto.ReservationDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationMapper {

    @Select("""
            SELECT COUNT(*)
            FROM reservation
            WHERE server_id = #{serverId}
              AND status = 'ACTIVE'
              AND start_time < #{endTime}
              AND end_time > #{startTime}
            """)
    int countConflict(
            Long serverId,
            java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime
    );

    @Insert("""
            INSERT INTO reservation
            (user_id, server_id, start_time, end_time, status)
            VALUES
            (#{userId}, #{serverId}, #{startTime}, #{endTime}, #{status})
            """)
    int insert(Reservation reservation);

    @Select("""
        SELECT
            id,
            user_id AS userId,
            server_id AS serverId,
            start_time AS startTime,
            end_time AS endTime,
            status,
            create_time AS createTime
        FROM reservation
        WHERE user_id = #{userId}
        ORDER BY start_time DESC
        """)
    List<Reservation> findByUserId(Long userId);

    @Select("""
        SELECT
            id,
            user_id AS userId,
            server_id AS serverId,
            start_time AS startTime,
            end_time AS endTime,
            status,
            create_time AS createTime
        FROM reservation
        WHERE id = #{id}
        """)
    Reservation findById(Long id);

    @Update("""
        UPDATE reservation
        SET status = 'CANCELLED'
        WHERE id = #{id}
        """)
    int cancelById(Long id);

    @Select("""
        SELECT id
        FROM gpu_server
        WHERE id = #{serverId}
        FOR UPDATE
        """)
    Long lockServer(Long serverId);

    @Select("""
        SELECT
            id,
            user_id AS userId,
            server_id AS serverId,
            start_time AS startTime,
            end_time AS endTime,
            status,
            create_time AS createTime
        FROM reservation
        ORDER BY start_time DESC
        """)
    List<Reservation> findAll();

    @Select("""
        SELECT
            r.id,
            r.user_id AS userId,
            u.username AS username,
            u.name AS name,

            r.server_id AS serverId,
            g.server_name AS serverName,
            g.gpu_model AS gpuModel,

            r.start_time AS startTime,
            r.end_time AS endTime,
            r.status,
            r.create_time AS createTime

        FROM reservation r

        JOIN user u
            ON r.user_id = u.id

        JOIN gpu_server g
            ON r.server_id = g.id

        ORDER BY r.start_time DESC
        """)
    List<ReservationDTO> findAllDetail();

    @Select("""
        SELECT
            r.id,
            r.user_id AS userId,
            u.username AS username,
            u.name AS name,

            r.server_id AS serverId,
            g.server_name AS serverName,
            g.gpu_model AS gpuModel,

            r.start_time AS startTime,
            r.end_time AS endTime,
            r.status,
            r.create_time AS createTime

        FROM reservation r

        JOIN user u
            ON r.user_id = u.id

        JOIN gpu_server g
            ON r.server_id = g.id

        WHERE r.user_id = #{userId}

        ORDER BY r.start_time DESC
        """)
    List<ReservationDTO> findDetailByUserId(Long userId);


    @Select("""
        <script>
        SELECT COUNT(*)

        FROM reservation r

        JOIN user u
            ON r.user_id = u.id

        WHERE 1 = 1

        <if test="status != null and status != ''">
            AND r.status = #{status}
        </if>

        <if test="username != null and username != ''">
            AND u.username LIKE CONCAT('%', #{username}, '%')
        </if>

        </script>
        """)
    long countReservations(
            @Param("status") String status,
            @Param("username") String username
    );


    @Select("""
        <script>

        SELECT
            r.id,
            r.user_id AS userId,
            u.username AS username,
            u.name AS name,

            r.server_id AS serverId,
            g.server_name AS serverName,
            g.gpu_model AS gpuModel,

            r.start_time AS startTime,
            r.end_time AS endTime,
            r.status,
            r.create_time AS createTime

        FROM reservation r

        JOIN user u
            ON r.user_id = u.id

        JOIN gpu_server g
            ON r.server_id = g.id

        WHERE 1 = 1

        <if test="status != null and status != ''">
            AND r.status = #{status}
        </if>

        <if test="username != null and username != ''">
            AND u.username LIKE CONCAT('%', #{username}, '%')
        </if>

        ORDER BY r.start_time DESC

        LIMIT #{offset}, #{size}

        </script>
        """)
    List<ReservationDTO> findPage(
            @Param("status") String status,
            @Param("username") String username,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );
}