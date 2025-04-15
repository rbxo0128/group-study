<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.groupstudy.model.mapper.ChatMessageMapper">

    <select id="findById" resultType="com.example.groupstudy.model.entity.ChatMessage">
        SELECT * FROM chat_messages WHERE message_id = #{messageId}
    </select>

    <select id="findByGroupId" resultType="com.example.groupstudy.model.entity.ChatMessage">
        SELECT * FROM chat_messages
        WHERE group_id = #{groupId}
        ORDER BY sent_at ASC
    </select>

    <select id="findByGroupIdWithLimit" resultType="com.example.groupstudy.model.entity.ChatMessage">
        SELECT * FROM chat_messages
        WHERE group_id = #{groupId}
        ORDER BY sent_at DESC
        LIMIT #{limit}
    </select>

    <insert id="insert" parameterType="com.example.groupstudy.model.entity.ChatMessage" useGeneratedKeys="true" keyProperty="messageId">
        INSERT INTO chat_messages (group_id, user_id, message, sent_at)
        VALUES (#{groupId}, #{userId}, #{message}, #{sentAt})
    </insert>

    <delete id="delete">
        DELETE FROM chat_messages WHERE message_id = #{messageId}
    </delete>

    <delete id="deleteByGroupId">
        DELETE FROM chat_messages WHERE group_id = #{groupId}
    </delete>

</mapper>