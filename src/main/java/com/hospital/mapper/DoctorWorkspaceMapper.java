package com.hospital.mapper;

import com.hospital.dto.DoctorBaseInfoVO;
import com.hospital.dto.ScheduleItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生工作台聚合查询 Mapper
 * 强制要求使用原生 SQL 和 JOIN 查询
 */
@Mapper
public interface DoctorWorkspaceMapper {

    /**
     * 查询医生基础信息与科室名称
     */
    @Select("SELECT u.id, u.real_name AS realName, doc.title, d.dept_name AS departmentName, u.phone, " +
            "CASE WHEN u.status = 1 THEN '在职' ELSE '离职' END AS status " +
            "FROM sys_user u " +
            "LEFT JOIN doctor doc ON u.id = doc.user_id " +
            "LEFT JOIN department d ON doc.dept_id = d.id " +
            "WHERE u.id = #{doctorUserId} AND u.role = 'DOCTOR'")
    DoctorBaseInfoVO getDoctorBaseInfo(@Param("doctorUserId") Long doctorUserId);

    /**
     * 查询今日待接诊数量
     */
    @Select("SELECT COUNT(*) FROM consultation " +
            "WHERE doctor_id = (SELECT id FROM doctor WHERE user_id = #{doctorUserId} AND deleted = 0 LIMIT 1) " +
            "AND DATE(created_at) = #{today} " +
            "AND status = 0")
    Integer getPendingCount(@Param("doctorUserId") Long doctorUserId, @Param("today") LocalDate today);

    /**
     * 查询今日已接诊数量
     */
    @Select("SELECT COUNT(*) FROM consultation " +
            "WHERE doctor_id = (SELECT id FROM doctor WHERE user_id = #{doctorUserId} AND deleted = 0 LIMIT 1) " +
            "AND DATE(created_at) = #{today} " +
            "AND status IN (1, 2)")
    Integer getCompletedCount(@Param("doctorUserId") Long doctorUserId, @Param("today") LocalDate today);

    /**
     * 查询排班列表及其挂号情况
     * bookedSlots: 用 total_slots 减去 available_slots
     */
    @Select("SELECT s.work_date AS workDate, s.time_slot AS timeSlot, " +
            "s.total_slots AS totalSlots, (s.total_slots - s.available_slots) AS bookedSlots, " +
            "s.available_slots AS availableSlots, s.status " +
            "FROM schedule s " +
            "WHERE s.doctor_id = (SELECT id FROM doctor WHERE user_id = #{doctorUserId} AND deleted = 0 LIMIT 1) " +
            "AND s.work_date >= #{queryDate} " +
            "ORDER BY s.work_date ASC, s.time_slot DESC")
    List<ScheduleItemVO> getSchedules(@Param("doctorUserId") Long doctorUserId, @Param("queryDate") LocalDate queryDate);
}
