package com.bingo.gpumanager.mapper;

import com.bingo.gpumanager.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper{
    @Select("""
            SELECT
                id,
                username,
                password,
                name,
                role,
                create_time AS createTime
            FROM user
            WHERE username = #{username}
            """)
    User findByUsername(String username);

    @Insert("""
        INSERT INTO user
        (username, password, name, role)
        VALUES
        (#{username}, #{password}, #{name}, #{role})
        """)
    int insert(User user);
}