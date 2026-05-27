package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.dto.ConsultationVO;
import com.hospital.entity.Consultation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConsultationMapper extends BaseMapper<Consultation> {

    @Select("<script>" +
            "SELECT c.id, c.patient_id AS patientId, c.doctor_id AS doctorId, " +
            "c.type, c.symptom_desc AS symptomDesc, c.status, " +
            "c.evaluation_score AS evaluationScore, c.evaluation_content AS evaluationContent, " +
            "c.created_at AS createdAt, c.updated_at AS updatedAt, " +
            "p.real_name AS patientName, d.real_name AS doctorName " +
            "FROM consultation c " +
            "LEFT JOIN sys_user p ON c.patient_id = p.id " +
            "LEFT JOIN sys_user d ON c.doctor_id = d.id " +
            "WHERE 1=1 " +
            "<if test='userType == \"PATIENT\"'> AND c.patient_id = #{userId} </if> " +
            "<if test='userType == \"DOCTOR\"'> AND c.doctor_id = #{userId} </if> " +
            "<if test='status != null and status != \"\"'> AND c.status = #{status} </if> " +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<ConsultationVO> getConsultationList(
            @Param("userId") Long userId,
            @Param("userType") String userType,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("pageSize") Integer pageSize);

    @Select("<script>" +
            "SELECT COUNT(c.id) " +
            "FROM consultation c " +
            "WHERE 1=1 " +
            "<if test='userType == \"PATIENT\"'> AND c.patient_id = #{userId} </if> " +
            "<if test='userType == \"DOCTOR\"'> AND c.doctor_id = #{userId} </if> " +
            "<if test='status != null and status != \"\"'> AND c.status = #{status} </if> " +
            "</script>")
    long getConsultationCount(
            @Param("userId") Long userId,
            @Param("userType") String userType,
            @Param("status") String status);
}
