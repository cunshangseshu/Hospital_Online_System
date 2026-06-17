#!/bin/bash
# Manually rewrite that problematic method in ConsultationServiceImpl using awk
awk '
/public PageResult<ConsultationVO> getMyConsultations/ {
  inMethod=1
  print
  next
}
inMethod && /PageResult<ConsultationVO> pageResult = new PageResult<>\(\);/ {
  print "        int offset = (pageNum - 1) * pageSize;"
  print "        List<ConsultationVO> voList = baseMapper.getConsultationList(userId, userType, status, offset, pageSize);"
  print "        long total = baseMapper.getConsultationCount(userId, userType, status);"
  print ""
  inMethod=0
  print
  next
}
inMethod && /pageResult.setTotal\(resultPage.getTotal\(\)\);/ {
  print "        pageResult.setTotal(total);"
  next
}
inMethod {
  # skip adding existing lines wrapper...
  next
}
!inMethod { print }
' /sessions/quirky-friendly-cerf/mnt/ajax的作业/hospital-management-system/src/main/java/com/hospital/service/impl/ConsultationServiceImpl.java > /sessions/quirky-friendly-cerf/mnt/ajax的作业/hospital-management-system/tmp.java
mv /sessions/quirky-friendly-cerf/mnt/ajax的作业/hospital-management-system/tmp.java /sessions/quirky-friendly-cerf/mnt/ajax的作业/hospital-management-system/src/main/java/com/hospital/service/impl/ConsultationServiceImpl.java
