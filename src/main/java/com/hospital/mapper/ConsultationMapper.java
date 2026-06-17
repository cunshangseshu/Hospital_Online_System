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
            "SELECT c.id, c.consultation_no AS consultationNo, c.patient_id AS patientId, c.doctor_id AS doctorId, " +
            "c.dept_id AS deptId, c.type, c.symptom_description AS symptomDescription, c.status, " +
            "c.doctor_note AS doctorNote, c.started_at AS startedAt, c.created_at AS createdAt, " +
            "p.real_name AS patientName, du.real_name AS doctorName, doc.title AS doctorTitle, dept.dept_name AS deptName " +
            "FROM consultation c " +
            "LEFT JOIN sys_user p ON c.patient_id = p.id " +
            "LEFT JOIN doctor doc ON c.doctor_id = doc.id " +
            "LEFT JOIN sys_user du ON doc.user_id = du.id " +
            "LEFT JOIN department dept ON c.dept_id = dept.id " +
            "WHERE 1=1 " +
            "<if test='userType == \"PATIENT\"'> AND c.patient_id = #{userId} </if> " +
            "<if test='userType == \"DOCTOR\"'> AND doc.user_id = #{userId} </if> " +
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
            "LEFT JOIN doctor doc ON c.doctor_id = doc.id " +
            "WHERE 1=1 " +
            "<if test='userType == \"PATIENT\"'> AND c.patient_id = #{userId} </if> " +
            "<if test='userType == \"DOCTOR\"'> AND doc.user_id = #{userId} </if> " +
            "<if test='status != null and status != \"\"'> AND c.status = #{status} </if> " +
            "</script>")
    long getConsultationCount(
            @Param("userId") Long userId,
            @Param("userType") String userType,
            @Param("status") String status);
}
