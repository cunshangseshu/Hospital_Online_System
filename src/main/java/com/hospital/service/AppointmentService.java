package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.dto.AppointmentRequest;
import com.hospital.dto.AppointmentListItemVO;
import com.hospital.dto.PageResult;
import com.hospital.entity.Appointment;
import com.hospital.entity.Schedule;
import java.time.LocalDate;
import java.util.List;

/**
 * 预约服务接口
 */
public interface AppointmentService extends IService<Appointment> {

    /**
     * 创建预约
     */
    Appointment createAppointment(AppointmentRequest request);

    /**
     * 取消预约
     */
    boolean cancelAppointment(Long appointmentId, String reason);

    /**
     * 查询患者的预约列表(不分页)
     */
    List<Appointment> getByPatientId(Long patientId);

    /**
     * 查询我的预约列表(分页+筛选+搜索) - 用于"我的预约"页面
     * @param patientId 患者ID
     * @param status 状态筛选(PENDING/CONFIRMED/COMPLETED/CANCELLED/EXPIRED)
     * @param keyword 搜索关键词(预约号或医生姓名)
     * @param pageNum 页码
     * @param pageSize 每页大小
     */
    PageResult<AppointmentListItemVO> getMyAppointments(Long patientId, String status, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 查询医生的预约列表
     */
    List<Appointment> getByDoctorId(Long doctorId, LocalDate date);

    /**
     * 根据预约号查询
     */
    Appointment getByAppointmentNo(String appointmentNo);

    /**
     * 获取预约详情(包含医生和科室信息)
     */
    AppointmentListItemVO getAppointmentDetail(Long id);

    /**
     * 改签预约
     */
    boolean rescheduleAppointment(Long oldAppointmentId, Long newScheduleId, String newAppointmentDate, String newTimeSlot);

    /**
     * 获取可改签的号源列表
     */
    List<Schedule> getRescheduleSchedules(Long doctorId, String startDate, String endDate);
}